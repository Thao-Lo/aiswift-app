package com.aiswift.Enum;

import org.springframework.context.annotation.Conditional;

import com.aiswift.Config.TenantDatabaseCondition;


@Conditional(TenantDatabaseCondition.class)  // Only create for tenant databases
public enum OrderStatus {
	CREATED, COMPLETED,CANCELLED,REFUNDED
}
