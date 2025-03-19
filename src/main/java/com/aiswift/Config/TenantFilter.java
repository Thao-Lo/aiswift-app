package com.aiswift.Config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aiswift.Global.Entity.Tenant;
import com.aiswift.Global.Service.TenantService;
import com.aiswift.MultiTenancy.TenantContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class TenantFilter extends OncePerRequestFilter{

	private final ObjectProvider<TenantService> tenantServiceProvider;
    private static final Logger logger = LoggerFactory.getLogger(TenantFilter.class);
   
	@Autowired
	public TenantFilter(ObjectProvider<TenantService> tenantServiceProvider) {
		this.tenantServiceProvider = tenantServiceProvider;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		System.out.println("I am in tenant filter");
		
		try {
			TenantService tenantService = tenantServiceProvider.getIfAvailable();

            if (tenantService == null) {
                logger.error("TenantService is not available. Defaulting to default db.");
                TenantContext.setCurrentTenant("default");
                TenantContext.setCurrentUserRole("DEVELOPER");
                filterChain.doFilter(request, response);
                return;
            }

            String shopName = request.getHeader("shop-name");
            String userGlobalRole = request.getHeader("global-user");
          

            logger.info("Filter processing shop: {}", shopName);
            
            if (userGlobalRole !=null) {
        		TenantContext.setCurrentUserRole(userGlobalRole);
            }

            if (shopName == null) {
                logger.warn("Shop-name header missing");
                TenantContext.setCurrentTenant("default");
            } else {
        		// shop is specified
        		Tenant tenant = tenantService.getTenantByShopId(shopName);
                if (tenant != null && tenant.getDbName() != null) {
                    TenantContext.setCurrentTenant(tenant.getDbName());
                    logger.info("Setting DB to: {} in thread: {}", tenant.getDbName(), Thread.currentThread().getId());
                } else {
                    logger.warn("Tenant not found for shop: {}, using default", shopName);
                    TenantContext.setCurrentTenant("default");
                }
                 logger.info("Setting DB to: {} in thread: {}", tenant.getDbName(), Thread.currentThread().getId());
        
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
	}
}
