package com.aiswift.Enum;

import org.springframework.context.annotation.Conditional;

import com.aiswift.Config.TenantDatabaseCondition;


@Conditional(TenantDatabaseCondition.class)  // Only create for tenant databases
public enum OrderStatus {
	   CREATED,       // Initial order just created
	    PENDING,       // Order awaiting payment confirmation
	    PAID,          // Payment successful
	    VOIDED, 		// Cancelled order (before processing)
	    PROCESSING,    // Order being prepared
	    SHIPPED,       // Order ready for pickup or delivery (optional)
	    DELIVERED,     // Order received by customer
	    
	    REFUNDED	// Refunded order
	    
}
