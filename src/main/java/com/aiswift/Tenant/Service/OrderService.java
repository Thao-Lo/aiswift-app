package com.aiswift.Tenant.Service;


import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Enum.OrderStatus;
import com.aiswift.Global.Entity.Tenant;
import com.aiswift.Tenant.Entity.Order;
import com.aiswift.Tenant.Entity.OrderDetail;
import com.aiswift.Tenant.Entity.Product;
import com.aiswift.Tenant.Entity.Size;
import com.aiswift.Tenant.Entity.TenantUser;
import com.aiswift.Tenant.Repository.OrderRepository;
import com.aiswift.dto.AddItemRequestDto;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Conditional(TenantDatabaseCondition.class) // Only create for tenant databases
@Service
public class OrderService {

	private OrderRepository orderRepository;
	private ProductService productService;
	private SizeService sizeService;
	private OrderDetailService orderDetailService;

	@Autowired
	public OrderService( ProductService productService, SizeService sizeService,OrderRepository orderRepository) {
		this.productService = productService;
		this.sizeService = sizeService;
	}

	//retrieve all orders created by user
	public List<Order> getAllOrdersByUser( TenantUser user) {
	    log.info("Retrieving all orders for user: {}", user.getEmail());
	    // Find all orders for this user in the current tenant
	    List<Order> orders = orderRepository.findByUserId( user.getId());
	    
	    log.info("Found {} orders for user: {}", orders.size(), user.getEmail());
	    return orders;
	}
	
	// when user place the order
	public Order createOrderWithItems(TenantUser user,Tenant currentTenant, List<AddItemRequestDto> items) {
	    log.info("Creating new order for user email: {}  with {} items", user.getEmail(), items.size());
	    
	  
	    
	    // Create a new order
	    Order newOrder = new Order();
	    newOrder.setTenantId(currentTenant.getId());
	    newOrder.setUser(user);
	    newOrder.setOrderDetails(new HashSet<>());
	    newOrder.setTotalPrice(BigDecimal.ZERO);
	    newOrder.setStatus(OrderStatus.CREATED);
	    
	    // Save the order first to get an ID
	    Order savedOrder = orderRepository.save(newOrder);
	    
	    // Add items to the order
	    BigDecimal totalPrice = BigDecimal.ZERO;
	    
	    for (AddItemRequestDto orderItem : items) {
	        // Validate item data
	        Product product = orderItem.getProduct();
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
	        OrderDetail orderDetail = new OrderDetail();
	        orderDetail.setOrder(savedOrder);
	        orderDetail.setProduct(productDB);
	        orderDetail.setQuantity(quantity);
	        orderDetail.setPrice(productDB.getPrice());
	        orderDetail.setSubtotal(productDB.getPrice().multiply(BigDecimal.valueOf(quantity)));	        
	        // Handle size if applicable
	        if (!productDB.getSizes().isEmpty() && sizeId != null) {
	            Size sizeDB = sizeService.getSizesById(sizeId);
	            if (sizeDB != null) {
	                orderDetail.setSize(sizeDB);
	            } else {
	                log.warn("Size with ID {} not found for product ID {}", sizeId, product.getId());
	            }
	        } else {
	            log.info("Product ID {} doesn't have size or no size selected", product.getId());
	        }
	        
	        // Save the order detail
	        OrderDetail savedDetail = orderDetailService.save(orderDetail);
	        savedOrder.getOrderDetails().add(savedDetail);
	        
	        // Add to total price
	        totalPrice = totalPrice.add(orderDetail.getSubtotal());

	    }
	    
	    // Update the total price
	    savedOrder.setTotalPrice(totalPrice);
	    return orderRepository.save(savedOrder);
	}
	
	//note: When user logout, we can select to evict the order cache of that user. 

// DB ORDER RELATED METHOD
}
