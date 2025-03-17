package com.aiswift.Config;


import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.aiswift.MultiTenancy.DataSourceUtil;
import com.aiswift.MultiTenancy.TenantRoutingDataSource;

import jakarta.persistence.EntityManagerFactory;


@Configuration
@DependsOn("globalEntityManagerFactory") // Đảm bảo Global DB khởi tạo trước
@EnableJpaRepositories(
basePackages = "com.aiswift.Tenant.Repository",
entityManagerFactoryRef = "tenantEntityManagerFactory",
transactionManagerRef = "tenantTransactionManager")
public class MultiTenantDataSourceConfig {

	@Autowired
	private DataSourceUtil dataSourceUtil;
	private static final Logger logger = LoggerFactory.getLogger(MultiTenantDataSourceConfig.class);
	
	@Primary // Ensure this is the main DataSource used
	@Bean
	public TenantRoutingDataSource multiTenantDataSource() {
		TenantRoutingDataSource tenantRoutingDataSource = new TenantRoutingDataSource();
//		Map<Object, Object> dataSourceMap = new HashMap<>();
//
//		List<Tenant> tenants = tenantService.getAllTenant();
//		
//		if (tenants.isEmpty()) {
//			System.out.print("No tenant found");
//		} else {
//			for (Tenant tenant : tenants) {
//				System.out.println(" I am in Multitenant DS Config, adding tenants"+tenant.getDbName());
//				dataSourceMap.put(tenant.getDbName(), dataSourceUtil.createDataSource(tenant.getDbName()));
//			}
//		}
//		
//		DataSource defaultDataSource = dataSourceUtil.createDataSource("global_multi_tenant");
//		dataSourceMap.put("default", defaultDataSource);
//
//		tenantRoutingDataSource.setTargetDataSources(dataSourceMap);
//		tenantRoutingDataSource.setDefaultTargetDataSource(defaultDataSource);
//		tenantRoutingDataSource.afterPropertiesSet();
//
//		System.out.println("added default DS"+ defaultDataSource);
		//add default DataSource
		tenantRoutingDataSource.addDataSource("default", dataSourceUtil.createDataSource("global_multi_tenant"));
				
		return tenantRoutingDataSource;
	}

	@Bean(name = "tenantEntityManagerFactory")
	@DependsOn("multiTenantDataSource") 
	 public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory(
	            EntityManagerFactoryBuilder builder,
	            @Qualifier("multiTenantDataSource") DataSource multiTenantDataSource) {
	        logger.info("Creating tenant entity manager factory");
	        return builder
	            .dataSource(multiTenantDataSource)  // Use the injected parameter
	            .packages("com.aiswift.Tenant.Entity")
	            .persistenceUnit("tenant")  
	            .build();
	    }

	@Bean(name = "tenantTransactionManager")
	public PlatformTransactionManager tenantTransactionManager(
			@Qualifier("tenantEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
			System.out.println("I am in tenant transaction manager");
		return new JpaTransactionManager(entityManagerFactory);
	}
	
	
}
