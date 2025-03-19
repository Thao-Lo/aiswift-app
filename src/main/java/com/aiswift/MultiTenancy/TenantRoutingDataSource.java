package com.aiswift.MultiTenancy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.zaxxer.hikari.HikariDataSource;

@Component
@Lazy
public class TenantRoutingDataSource extends AbstractRoutingDataSource{
	
	@Autowired
	private DataSourceUtil dataSourceUtil;
	
	private final Map<Object, Object> dataSourceMap = new ConcurrentHashMap<>();
	private final Map<Object, Long> lastUsedTime = new ConcurrentHashMap<>();
	
	public TenantRoutingDataSource(DataSourceUtil dataSourceUtil) {
		this.dataSourceUtil = dataSourceUtil;
		setTargetDataSources(new HashMap<>());
		setDefaultTargetDataSource(dataSourceUtil.createDataSource("global_multi_tenant"));
		afterPropertiesSet();
	}
		
	@Override
	protected Object determineCurrentLookupKey() {	
		System.out.println("I am in TenantRouting DS, determining the lookup key on threadID: "+ Thread.currentThread().getId());
		String tenant = TenantContext.getCurrentTenant();
        System.out.println("Routing to database: " + tenant);

		return tenant != null ? tenant : "default";
	}

	@Override
	protected DataSource determineTargetDataSource() {
		String tenant = (String) determineCurrentLookupKey();

		dataSourceMap.computeIfAbsent(tenant, key -> {
			System.out.println("[Lazy Init] Creating DataSource for tenant: " + tenant);
			lastUsedTime.put(tenant, System.currentTimeMillis()); // save time when the connection starts
			return dataSourceUtil.createDataSource(tenant);
		});

		lastUsedTime.put(tenant, System.currentTimeMillis()); // update connection time to the new time
		return (DataSource) dataSourceMap.get(tenant);
	}

	@Scheduled(fixedRate = 10 * 60 * 1000) // 10 mins
	public void removeUnsedDataSources() {
		long now = System.currentTimeMillis();
		long threshold = 30 * 60 * 1000; // 30 mins

		for (Object tenant : new HashSet<>(dataSourceMap.keySet())) { // copy tenant key set to new hashSet,
																		// avoid ConcurrentModificationException
			if (now - lastUsedTime.getOrDefault(tenant, 0L) > threshold) {
				((HikariDataSource) dataSourceMap.remove(tenant)).close(); // remove then return HikariDataSource
																			// instance, then close
				lastUsedTime.remove(tenant);
			}
		}

	}
    public void addDataSource(String databaseName, DataSource dataSource) {
    	System.out.println("I am adding dataSource name: "+ databaseName);
    	if(!dataSourceMap.containsKey(databaseName)) {
    		this.dataSourceMap.put(databaseName, dataSource);
			super.setTargetDataSources(dataSourceMap);
			super.afterPropertiesSet(); // Reload data sources dynamically
    	}      
    }
    
//    @Override
//    public void afterPropertiesSet() {
//        // Set a default tenant if none is set
//        if (TenantContext.getCurrentTenant() == null) {
//        	TenantContext.setCurrentTenant("default");
//        }
//        super.afterPropertiesSet();
//    }
}
