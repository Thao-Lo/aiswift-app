 package com.aiswift.MultiTenancy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantContext {
	private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);

	private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
	private static final ThreadLocal<String> CURRENT_ROLE = new ThreadLocal<>();

	public static String getCurrentTenant() {
		String tenant = CURRENT_TENANT.get();
        System.out.println("I am in TenantContext, getting current tenant: " + tenant + "thread ID:"+ Thread.currentThread().getId());
		return tenant;
	}
	public static void setCurrentTenant(String databaseName) {
		System.out.println("I am in TenantContext, setting current tenant "+ databaseName+ "threadID: "+ Thread.currentThread().getId());
		CURRENT_TENANT.set(databaseName);
	}
	
	public static String getCurrentUserRole() {
		String role = CURRENT_ROLE.get();
		return role;
	}
	public static void setCurrentUserRole(String userRole) {
		System.out.println("setting user role: "+ userRole);
		CURRENT_ROLE.set(userRole);
	}
	
	public static void clear() {
		CURRENT_TENANT.remove();
		CURRENT_ROLE.remove();
	}
	
	}
