package com.aiswift.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManagerFactory;

@Configuration
public class DynamicEntityManagerConfig {
	
	@Autowired
    private MultiTenantDataSourceConfig multiTenantDataSourceConfig;

    @Autowired
    private DataSourceConfig globalDataSourceConfig;

    @Bean
    public EntityManagerFactorySelector entityManagerFactorySelector() {
        return new EntityManagerFactorySelector();
    }

    public class EntityManagerFactorySelector {
        public EntityManagerFactory determineEntityManagerFactory(String tenantId) {
        	
        	System.out.println(" I am determining Entity Manager Factory with tenantId: "+ tenantId);
            // If no tenant is specified, use global EntityManagerFactory
        	
            if (tenantId == null || tenantId.isEmpty()) {
                return globalEntityManagerFactory();
            }

            // Use tenant-specific EntityManagerFactory
            return tenantEntityManagerFactory();
        }
        
        private EntityManagerFactory globalEntityManagerFactory() {
            return globalDataSourceConfig
                .globalEntityManagerFactory(
                    globalDataSourceConfig.entityManagerFactoryBuilder(), 
                    globalDataSourceConfig.globalDataSource()
                ).getObject();
        }

        private EntityManagerFactory tenantEntityManagerFactory() {
            return multiTenantDataSourceConfig
                .tenantEntityManagerFactory(
                    globalDataSourceConfig.entityManagerFactoryBuilder(), 
                    multiTenantDataSourceConfig.multiTenantDataSource()
                ).getObject();
        }
    }
}
