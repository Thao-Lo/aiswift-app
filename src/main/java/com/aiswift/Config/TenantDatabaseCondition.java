package com.aiswift.Config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.aiswift.MultiTenancy.TenantContext;


//Define a condition class
	public class TenantDatabaseCondition implements Condition {
	    @Override
	    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
	        String currentTenant = TenantContext.getCurrentTenant();
	        return !"global_multi_tenant".equals(currentTenant) && !"default".equals(currentTenant);
	    }
	}
