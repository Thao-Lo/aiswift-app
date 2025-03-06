package com.aiswift.MultiTenancy;
//package multi_tenant.db.navigation.Utils;
//
//import javax.sql.DataSource;
//
//import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class MultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl<Object> {
//
//	private static final long serialVersionUID = 1L;
//	@Autowired
//	private TenantRoutingDataSource tenantRoutingDataSource;
//	@Override
//	public DataSource selectAnyDataSource() {
//		// TODO Auto-generated method stub
//		return tenantRoutingDataSource.getDefaultDataSource();
//
//	}
//
//	@Override
//	public DataSource selectDataSource(Object tenantIdentifier) {
//		// TODO Auto-generated method stub
//		String currentTenant = TenantContext.getCurrentTenant();
//		DataSource dataSource =  tenantRoutingDataSource.getDataSource(currentTenant);
//		return dataSource;
//		
//	}
//
//}
