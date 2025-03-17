package com.aiswift.Tenant.Entity;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.aiswift.Enum.OrderStatus;
import com.aiswift.Global.Entity.Tenant;
import com.aiswift.Global.Service.TenantService;
import com.aiswift.MultiTenancy.TenantContext;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Component
public class OrderEntityListener {
	
	@Autowired
	TenantService tenantService;
	
	@PrePersist
    public void prePersist(Order order) {
        // Set current tenant ID
        String currentDbName = TenantContext.getCurrentTenant();
        if (currentDbName != null) {
        	Tenant tenant = tenantService.getTenantByDatabaseName(currentDbName);
            order.setTenantId(tenant.getId());
        }

        // Set current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
        	User currentUser = (User)authentication.getPrincipal();
        
            order.setUser(currentUser);
        }

        if (order.getCreatedAt() == null) {
            order.setCreatedAt(LocalDateTime.now());
        }
        
        if (order.getStatus() == null) {
			order.setStatus(OrderStatus.CREATED);
		}
    }
	
	 @PreUpdate
	    public void preUpdate(Order order) {
	        order.setUpdatedAt(LocalDateTime.now());
	    }

}
