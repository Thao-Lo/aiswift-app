package com.aiswift.Tenant.Service;

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
import com.aiswift.Tenant.Repository.OrderDetailRepository;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Conditional(TenantDatabaseCondition.class) // Only create for tenant databases
@Service
public class OrderDetailService {

	private OrderDetailRepository orderDetailRepository;

	@Autowired
	public OrderDetailService( OrderDetailRepository orderDetailRepository) {
		this.orderDetailRepository = orderDetailRepository;
	}

	public void updateOrCreateOrderDetail(Product product, int quantity, Size size,Order order) {
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
				.filter(od -> od.getProduct().getId().equals(product.getId())
						&& (size == null || od.getSize() != null && od.getSize().getId().equals(size.getId())))
				.findFirst().orElse(null);
		// if the matchedItem is not found
		if (matchedItem == null) {

			// Create OrderDetailKey
			OrderDetailKey orderDetailKey = new OrderDetailKey(order.getId(), product.getId());

			// build orderDetail
			OrderDetail orderDetail = OrderDetail.builder().id(orderDetailKey) // Set the composite key
					.product(product).quantity(quantity).price(size != null ? size.getSizePrice() : product.getPrice())
					.order(order).createdBy(order.getUser().getEmail()).size(size)
					.createdAt(LocalDateTime.now()).build();
			// Add the new OrderDetail
			order.getOrderDetails().add(orderDetail);
			System.out.println("added orderDetail: " + orderDetail.getProduct().getName());
		} else {
			//if the matchedItem is found
			//update the quantity
			Integer updatedQuantity = quantity;

			matchedItem.setQuantity(updatedQuantity);
			matchedItem.setSize(size);
			matchedItem.setPrice(size != null ? size.getSizePrice() : product.getPrice());
			log.info("Updated orderDetail: " + matchedItem.getProduct().getName()
					+ (size != null ? " with size " + size.getName() : " without size") + ", new quantity: "
					+ quantity);

		}

	}

	public Order removeOrderDetail(Product product, Long sizeId,Order order) {
		Set<OrderDetail> orderDetails = order.getOrderDetails();
	    if (orderDetails != null && !orderDetails.isEmpty()) {
	        OrderDetail orderDetailToRemove = orderDetails.stream().filter(
	            od -> od.getProduct().getId().equals(product.getId()) && 
	                 ((sizeId == null && od.getSize() == null) || 
	                  (sizeId != null && od.getSize() != null && od.getSize().getId().equals(sizeId))))
	            .findAny()
	            .orElse(null);

	        if (orderDetailToRemove != null) {
	            orderDetails.remove(orderDetailToRemove);
	         
	        }
	    }
	    return order;

	}

	public OrderDetail save(OrderDetail orderDetail) {
		// TODO Auto-generated method stub
		return orderDetailRepository.save(orderDetail);
	}
	
	

}
