package com.aiswift.Tenant.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Tenant.Entity.Order;
import com.aiswift.Tenant.Entity.OrderDetail;
import com.aiswift.Tenant.Entity.OrderDetailKey;
import com.aiswift.Tenant.Entity.Product;
import com.aiswift.Tenant.Entity.Size;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Conditional(TenantDatabaseCondition.class) // Only create for tenant databases
@Service
public class OrderDetailService {

	@Autowired
	ProductService productService;

	@Autowired
	SizeService sizeService;

	public OrderDetail createOrderDetail(Long productId, int quantity, Long sizeId, Order order) {
		Product product = productService.getProductById(productId);
		Size size = sizeService.getSizeById(sizeId);
		// Create OrderDetailKey
		OrderDetailKey orderDetailKey = new OrderDetailKey(order.getId(), productId);

		// build orderDetail
		OrderDetail orderDetail = OrderDetail.builder().id(orderDetailKey) // Set the composite key
				.product(product).quantity(quantity).price(size != null ? size.getSizePrice() : product.getPrice())
				.order(order).createdBy(order.getUser().getEmail()).size(size).createdAt(LocalDateTime.now()).build();
		BigDecimal subTotal = calculateOrderDetailTotal(orderDetail);
		orderDetail.setSubtotal(subTotal);
		System.out.println("added orderDetail: " + orderDetail.getProduct().getName());
		return orderDetail;
	}

	public OrderDetail updateOrderDetail(OrderDetail existingOrderDetail, Long productId, int quantity, Long sizeId) {

		Product product = productService.getProductById(productId);
		Size size = sizeService.getSizeById(sizeId);
		// Update the quantity
		existingOrderDetail.setQuantity(quantity);
		existingOrderDetail.setSize(size);
		existingOrderDetail.setPrice(size != null ? size.getSizePrice() : product.getPrice());

		BigDecimal subTotal = calculateOrderDetailTotal(existingOrderDetail);
		existingOrderDetail.setSubtotal(subTotal);
		log.info("Updated OrderDetail: " + existingOrderDetail.getProduct().getName()
				+ (size != null ? " with size " + size.getName() : " without size") + ", new quantity: " + quantity);

		return existingOrderDetail;

	}

	private BigDecimal calculateOrderDetailTotal(OrderDetail orderDetail) {
		return orderDetail.getPrice().multiply(BigDecimal.valueOf(orderDetail.getQuantity()));
	}

	public Order updateOrCreateOrderDetail(Long productId, int quantity, Long sizeId, Order order) {

		Set<OrderDetail> orderDetails = order.getOrderDetails();
		// ensure orderDetails is not null
		if (orderDetails == null) {
			orderDetails = new HashSet<>();
			order.setOrderDetails(orderDetails);
		}
		/*
		 * find the existing orderDetail in orderDetails that contains the selected
		 * product with the selected size
		 */
		OrderDetail matchedItem = orderDetails.stream()
				.filter(od -> od.getProduct().getId().equals(productId)
						&& (sizeId == null || od.getSize() != null && od.getSize().getId().equals(sizeId)))
				.findFirst().orElse(null);
		// if the matchedItem is not found
		if (matchedItem == null) {
			OrderDetail newOrderDetail = createOrderDetail(productId, quantity, sizeId, order);
			orderDetails.add(newOrderDetail);

		} else {
			// if the matchedItem is found
			// update the quantity
			matchedItem = updateOrderDetail(matchedItem, productId, quantity, sizeId);

		}
		return order;

	}

	public Order removeOrderDetailFromOrder(Long productId, Long sizeId, Order order) {
		Product product = productService.getProductById(productId);
		Set<OrderDetail> orderDetails = order.getOrderDetails();
		if (orderDetails != null && !orderDetails.isEmpty()) {
			OrderDetail orderDetailToRemove = orderDetails.stream().filter(
					od -> od.getProduct().getId().equals(product.getId()) && ((sizeId == null && od.getSize() == null)
							|| (sizeId != null && od.getSize() != null && od.getSize().getId().equals(sizeId))))
					.findAny().orElse(null);

			if (orderDetailToRemove != null) {
				orderDetails.remove(orderDetailToRemove);

			}

		}
		return order;

	}

}
