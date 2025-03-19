package com.aiswift.Global.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiswift.Exception.NoDataFoundException;
import com.aiswift.Global.DTO.TenantLogDTO;
import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Entity.SubPlanDetail;
import com.aiswift.Global.Entity.Tenant;
import com.aiswift.Global.Repository.SubPlanDetailRepository;
import com.aiswift.Global.Repository.TenantRepository;
import com.aiswift.MultiTenancy.DataSourceUtil;
import com.aiswift.MultiTenancy.TenantRoutingDataSource;

@Service
public class TenantService {
	@Autowired
	private TenantRepository tenantRepository;
	@Autowired
	private DataSourceUtil dataSourceUtil;

	@Autowired
	@Qualifier("globalDataSource")
	private DataSource globalDataSource;

	@Autowired
	@Lazy
	private TenantRoutingDataSource tenantRoutingDataSource;

	@Autowired
	private TenantActivityLogService tenantActivityLogService;

	@Autowired
	private SubPlanDetailRepository subPlanDetailRepository;
	
	private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);
	
	public TenantService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Tenant> getAllTenant() {
		return tenantRepository.findAll();
	}

	public Tenant getTenantByShopId(String shopName) {
		return tenantRepository.findByName(shopName)
				.orElseThrow(() -> new NoDataFoundException("No Tenant found with shop name: " + shopName));
	}

	public Tenant getTenantByDatabaseName(String dbName) {
		return tenantRepository.findByDbName(dbName)
				.orElseThrow(() -> new NoDataFoundException("No Tenant found with database name: " + dbName));
	}

	public List<Tenant> getTenantsByOwnerId(Long ownerId) {
		List<Tenant> tenants = tenantRepository.findByOwnerId(ownerId);
		return tenants.isEmpty() ? Collections.emptyList() : tenants;
	}

	public Tenant getTenantById(Long id) {
		return tenantRepository.findById(id)
				.orElseThrow(() -> new NoDataFoundException(String.format("No tenant found with Id: %d", id)));
	}

	@Transactional(transactionManager = "globalTransactionManager") // CHECK rollback
	public void saveTenantToGlobalDB(String shopName, Long ownerId, String databaseName) {
		logger.info("Saving tenant '{}' to global database '{}'", shopName, databaseName);
		String sqlQuery = "INSERT INTO tenants (owner_id, name, db_name, status) VALUES (?, ?, ?, ?)";
		try {
			jdbcTemplate.update(sqlQuery, ownerId, shopName, databaseName, "ACTIVE");
			logger.info("Global DB: successfully saved tenant with shop name: {} ", shopName);
		}catch(Exception e) {
			 throw new RuntimeException("Error inserting tenant into Global DB: " + e.getMessage());
		}

	}

	public void createNewTenant(String shopName, Long ownerId) {
		String databaseName = shopName.toLowerCase() + "_db";
		// Step 1: Save Tenant Info to Global Database
		saveTenantToGlobalDB(shopName, ownerId, databaseName);

		try (Connection connection = globalDataSource.getConnection()) {
			connection.setAutoCommit(false);
			Statement statement = connection.createStatement();

			// Step 2: Create New Tenant Database
			statement.execute("CREATE DATABASE " + databaseName);
			connection.commit();

			// Step 3: Connect to New Tenant DB and Run Flyway Migration
			DataSource tenantDataSource = dataSourceUtil.createDataSource(databaseName);
			Flyway flyway = Flyway.configure()
					.dataSource(tenantDataSource)
					.locations("classpath:db/migration/tenant")
					.baselineOnMigrate(false).load();
			try {
				flyway.migrate();
			} catch (Exception e) {
				statement.execute("DROP DATABASE " + databaseName);
				throw new RuntimeException("Flyway migration failed for " + databaseName + ": " + e.getMessage());
			}
			// Step 4: Insert initial data after Flyway migration succeeds
			insertTenantAdminLimit(databaseName, tenantDataSource);

		} catch (SQLException e) {
			throw new RuntimeException("Error when creating new tenant database: " + e.getMessage());
		}
	}
	private void insertTenantAdminLimit(String databaseName, DataSource tenantDataSource) {
		try(Connection connection = tenantDataSource.getConnection()){
			connection.setAutoCommit(false);
			
			String insertTenantAdminLimit = "INSERT INTO tenant_admin_limit (database_name, max_admin_count) VALUES (?, ?)";
			try(PreparedStatement preSt = connection.prepareStatement(insertTenantAdminLimit)){
				//JDBC uses 1-based index, not 0-based
				preSt.setString(1, databaseName);
				preSt.setInt(2, 1);	
				preSt.executeUpdate();
			}
			connection.commit();	
			
		}catch(SQLException e) {
			throw new RuntimeException("Faild insert tenant admin limit " + e.getMessage());
		}
	}
	
	private void updateTenantAdminLimit(String databaseName, DataSource tenantDataSource, int count) {
		try(Connection connection = tenantDataSource.getConnection()){
			connection.setAutoCommit(false);
			
			String updateMaxAdmin = "UPDATE tenant_admin_limit SET max_admin_count = ? WHERE database_name = ? ";
			try(PreparedStatement preSt = connection.prepareStatement(updateMaxAdmin)){
				//JDBC uses 1-based index, not 0-based				
				preSt.setInt(1, count);	
				preSt.setString(2, databaseName);
				preSt.executeUpdate();
			}
			connection.commit();	
			
		}catch(SQLException e) {
			throw new RuntimeException("Faild insert tenant admin limit " + e.getMessage());
		}
	}
	
	@Transactional(transactionManager = "globalTransactionManager")
	public void updateAdminCount(Long tenantId, int count, Owner owner, SubPlanDetail planDetail) {
		Tenant tenant = getTenantById(tenantId);
		
		tenant.setMaxAdminCount(tenant.getMaxAdminCount() + count); //only update total, not created admin
		tenantRepository.save(tenant);
		
		DataSource tenantDataSource = dataSourceUtil.createDataSource(tenant.getDbName());
		updateTenantAdminLimit(tenant.getDbName(),tenantDataSource, tenant.getMaxAdminCount());
		
		planDetail.setAllocatedAdditionalAdmin(count);
		subPlanDetailRepository.save(planDetail);
		
		TenantLogDTO tenantLogDTO = new TenantLogDTO();
		tenantLogDTO.setOwner(owner);	
		tenantLogDTO.setTenantId(tenantId);	
		tenantLogDTO.setOldValue(String.valueOf(tenant.getMaxAdminCount() - count));
		tenantLogDTO.setNewValue(String.valueOf(tenant.getMaxAdminCount()));
		tenantLogDTO.setActionTypeId(3); // ALLOCATE_ADDITION_ADMIN
		tenantLogDTO.setMessage(String.format("Allocate additional Admin: %d", count));
		
		tenantActivityLogService.createTenantActivityLog(tenantLogDTO);	
	}

	
	
	
	
}
