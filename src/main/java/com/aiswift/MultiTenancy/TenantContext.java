 package com.aiswift.MultiTenancy;


public class TenantContext {

	private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
	private static final ThreadLocal<String> CURRENT_ROLE = new ThreadLocal<>();

	public static String getCurrentTenant() {
		String tenant = CURRENT_TENANT.get();
		return tenant;
	}
	public static void setCurrentTenant(String databaseName) {
		CURRENT_TENANT.set(databaseName);
	}
	
	public static String getCurrentUserRole() {
		String role = CURRENT_ROLE.get();
		return role;
	}
	public static void setCurrentUserRole(String userRole) {
		CURRENT_ROLE.set(userRole);
	}
	
	public static void clear() {
		CURRENT_TENANT.remove();
		CURRENT_ROLE.remove();
	}
	
	}
