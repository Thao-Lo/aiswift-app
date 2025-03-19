package com.aiswift.Tenant.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Enum.OrderStatus;
import com.aiswift.Global.Entity.Tenant;
import com.aiswift.Global.Service.TenantService;
import com.aiswift.MultiTenancy.TenantContext;
import com.aiswift.Tenant.Entity.Order;
import com.aiswift.Tenant.Entity.OrderDetail;
import com.aiswift.Tenant.Entity.Product;
import com.aiswift.Tenant.Entity.Size;
import com.aiswift.Tenant.Entity.StatusHistorySerializer;
import com.aiswift.Tenant.Entity.TenantUser;
import com.aiswift.Tenant.Repository.OrderRepository;
import com.aiswift.dto.Global.CustomUserDetails;
import com.aiswift.dto.Tenant.OrderDetailDto;
import com.aiswift.dto.Tenant.OrderStatusDto;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Conditional(TenantDatabaseCondition.class) // Only create for tenant databases
@Service
public class OrderService {

	private OrderRepository orderRepository;
	private ProductService productService;
	private SizeService sizeService;
	private OrderDetailService orderDetailService;
	private TenantService tenantService;

	@Autowired
	public OrderService(ProductService productService, SizeService sizeService, OrderRepository orderRepository,
			OrderDetailService orderDetailService, TenantService tenantService, UserService userService) {
		this.productService = productService;
		this.sizeService = sizeService;
		this.orderRepository = orderRepository;
		this.orderDetailService = orderDetailService;
		this.tenantService = tenantService;
	}

	public Order getOrderById(Long orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new EntityNotFoundException("Order not found with Id :" + orderId));
	}

	public List<Order> getAllOrders() {
		return orderRepository.findAll();
	}

	public Order updateOrderStatusHistory(Order order, String action) {
		// Get existing history
		List<OrderStatusDto> statusHistory = StatusHistorySerializer
				.deserializeStatusHistory(order.getCompressedStatusHistory());
		// Add new status change
		statusHistory.add(new OrderStatusDto(order.getStatus(), LocalDateTime.now(), action));
		// Limit to last 10 entries so the string wont get too long
		if (statusHistory.size() > 10) {
			statusHistory = statusHistory.subList(Math.max(0, statusHistory.size() - 10), statusHistory.size());
		}
		// Compress and save
		order.setCompressedStatusHistory(StatusHistorySerializer.serializeStatusHistory(statusHistory));
		return orderRepository.save(order);
	}

	// retrieve all orders created by a specific user
	public List<Order> getAllOrdersByUser(TenantUser user) {
		log.info("Retrieving all orders for user: {}", user.getEmail());
		// Find all orders for this user in the current tenant
		List<Order> orders = orderRepository.findByUserId(user.getEmail());

		log.info("Found {} orders for user: {}", orders.size(), user.getEmail());
		return orders;
	}

	// when user place the order
	@Transactional("tenantTransactionManager")
	public Order createOrderWithItems(List<OrderDetailDto> items) {
		log.info("Creating new order  with {} items", items.size());

		// Create a new order
		Order newOrder = new Order();
		newOrder.setOrderDetails(new HashSet<>());
		newOrder.setTotalPrice(BigDecimal.ZERO);
		newOrder.setStatus(OrderStatus.CREATED);

		// Save the order first to get an ID
		try {
			// Save the order first to get an ID
			log.info("Attempting to save new order");
			Order savedOrder = saveOrder(newOrder);
			log.info("Order saved successfully with ID: {}", savedOrder.getId());

//		Order savedOrder = orderRepository.save(newOrder);
//		System.out.println(savedOrder);
			// Add items to the order
			BigDecimal totalPrice = BigDecimal.ZERO;

			for (OrderDetailDto orderItem : items) {
				// Validate item data
				Product product = productService.getProductById(orderItem.getProductId());
				int quantity = orderItem.getQuantity();
				Long sizeId = orderItem.getSizeId();

				if (quantity <= 0) {
					log.warn("Skipping item with product ID {} - invalid quantity: {}", product.getId(), quantity);
					continue;
				}

				// Find the product in DB by productId
				Product productDB = productService.getProductById(product.getId());
				if (productDB == null) {
					log.warn("Product with ID {} not found, skipping item", product.getId());
					continue;
				}

				// Create order detail
				OrderDetail orderDetail = orderDetailService.createOrderDetail(productDB.getId(), quantity, null,
						savedOrder);
				// Handle size if applicable
				if (!productDB.getSizes().isEmpty() && sizeId != null) {
					Size sizeDB = sizeService.getSizeById(sizeId);
					if (sizeDB != null) {
						orderDetail.setSize(sizeDB);
					} else {
						log.warn("Size with ID {} not found for product ID {}", sizeId, product.getId());
					}
				} else {
					log.info("Product ID {} doesn't have size or no size selected", product.getId());
				}

				savedOrder.getOrderDetails().add(orderDetail);
	            log.info("Added orderDetail: {} - Current order state: {}", product.getName(), savedOrder);
				// Add to total price
				totalPrice = totalPrice.add(orderDetail.getSubtotal());

			}

			// Update the total price
			savedOrder.setTotalPrice(totalPrice);
	        log.info("Final order before save: {}", savedOrder);
			Order finalizedOrder =  saveOrder(savedOrder); // cascade auto updates order detail database
	        log.info("Final order after save: {}", finalizedOrder);
	        return finalizedOrder;

		} catch (Exception e) {
			log.error("Error saving order: ", e);
			throw e;
		}
	}

	@Transactional("tenantTransactionManager")
	public Order updateOrderWithItems(Order order, List<OrderDetailDto> orderDetailDtos) {

		for (OrderDetailDto orderDetailDto : orderDetailDtos) {
			int quantity = orderDetailDto.getQuantity();
			Long productId = orderDetailDto.getProductId();
			Long sizeId = orderDetailDto.getSizeId();

			if (quantity > 0) {
				order = orderDetailService.updateOrCreateOrderDetail(productId, quantity, sizeId, order);

				// quantity = 0 -> remove the order detail
			} else {
				order = orderDetailService.removeOrderDetailFromOrder(orderDetailDto.getProductId(),
						orderDetailDto.getSizeId(), order);
				// this method does check if the orderDetail to be removed exist in order before
				// removal
			}

		}
		return orderRepository.save(order);

	}

	public Order cancelOrder(Order order) {
		// Can cancel only in early stages
		if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PENDING
				&& order.getStatus() != OrderStatus.PAID) {
			throw new IllegalStateException("Cannot cancel order. Current status: " + order.getStatus());
		}
		// Update to VOIDED
		order.setStatus(OrderStatus.VOIDED);
		// Add status history
		order = updateOrderStatusHistory(order, null);

		return order;
	}

	public Order saveOrderPaidByPaypal(Order order, String paypalOrderId) {
		order.setPaypalId(paypalOrderId);
		order.setStatus(OrderStatus.PAID);
		order = updateOrderStatusHistory(order, null);
		return order;
	}

	public Order saveOrder(Order order) {
		// Set tenant ID
		String currentDbName = TenantContext.getCurrentTenant();
		if (currentDbName != null) {
			Tenant tenant = tenantService.getTenantByDatabaseName(currentDbName);
			order.setTenantId(tenant.getId());
		}

		// Set user ID
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			order.setUserId(userDetails.getUsername());
		}

		// Set timestamps
		if (order.getCreatedAt() == null) {
			order.setCreatedAt(LocalDateTime.now());
		}
		order.setUpdatedAt(LocalDateTime.now());

		// Set status
		if (order.getStatus() == null) {
			order.setStatus(OrderStatus.CREATED);
		}

		return orderRepository.save(order);
	}
}
