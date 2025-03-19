package com.aiswift.Tenant.Service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Enum.OrderStatus;
import com.aiswift.Enum.RefundStatus;
import com.aiswift.Tenant.Entity.Order;
import com.aiswift.Tenant.Repository.OrderRepository;

@Conditional(TenantDatabaseCondition.class) // Only create for tenant databases
@Service
public class RefundService {

	@Autowired
	OrderService orderService;

	@Autowired
	OrderRepository orderRepository;

	@Transactional("tenantTransactionManager")
	public Order requestRefund(Long orderId) {
		Order order = orderService.getOrderById(orderId);

		// Check if refund can be requested
		validateRefundRequest(order);

		// Update refund status
		if (order.getStatus() == OrderStatus.VOIDED) {
			order.setRefundStatus(RefundStatus.AUTO_APPROVED);
		} else if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
			order.setRefundStatus(RefundStatus.PENDING_APPROVAL);
		}

		// Set refund request timestamp
		order.setRefundRequestedAt(LocalDateTime.now());

		// Add to compressed status history
		orderService.updateOrderStatusHistory(order, "Refund Requested");

		return orderRepository.save(order);
	}
	
	@Transactional("tenantTransactionManager")
    public Order processRefund(Long orderId, boolean approve) {
        Order order = orderService.getOrderById(orderId);

        // Validate refund processing
        validateRefundProcessing(order);

        if (approve) {
            // Approve refund
            order.setRefundStatus(RefundStatus.APPROVED);
            order.setRefundProcessedAt(LocalDateTime.now());

            // refund processing logic
            processPaymentRefund(order);

            // Add to compressed status history
            orderService.updateOrderStatusHistory(order, "Refund Approved");
        } else {
            // Reject refund
            order.setRefundStatus(RefundStatus.REJECTED);
            
            // Add to compressed status history
            orderService.updateOrderStatusHistory(order, "Refund Rejected");
        }

        return orderRepository.save(order);
    }

	private void validateRefundRequest(Order order) {
		if (order.getRefundStatus() != RefundStatus.NOT_REQUESTED && order.getRefundStatus() !=null) {
			throw new IllegalStateException("Refund already processed or in progress");
		}

		if (order.getStatus() != OrderStatus.DELIVERED ||order.getStatus() != OrderStatus.SHIPPED) { 
			// canceled before processing, or refund requested before order is shipped/delivered
			throw new IllegalStateException("Refund not allowed for current order status: " + order.getStatus());
		}
	}
	
	private void validateRefundProcessing(Order order) {
        if (order.getRefundStatus() != RefundStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("No refund pending approval");
        }
    }
	
	 private Order processPaymentRefund(Order order) {
	        // Integrate with payment gateway/cash
	        // Process actual refund
         order.setStatus(OrderStatus.REFUNDED);
         return orderRepository.save(order);

	 }
	
	
}
