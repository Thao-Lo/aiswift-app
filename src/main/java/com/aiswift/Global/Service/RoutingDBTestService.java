package com.aiswift.Global.Service;


import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.aiswift.MultiTenancy.TenantContext;
import com.aiswift.Tenant.Service.UserService;



@Service
public class RoutingDBTestService {

 
	 private final JdbcTemplate jdbcTemplate;
	    
	    @Autowired
	    public RoutingDBTestService(@Qualifier("multiTenantDataSource") DataSource dataSource) {
	        System.out.println("Creating JdbcTemplate with multiTenantDataSource");
	        this.jdbcTemplate = new JdbcTemplate(dataSource);
	    }
    @Autowired
    UserService userService;

    public String getWelcomeMessage() {
        try {
            // Ensure the correct tenant database is being used
            String currentTenant = TenantContext.getCurrentTenant();
            System.out.println("current tenant to get welcome message:"+ currentTenant);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication != null ? authentication.getName() : null;
            System.out.println("getting welcome message in  "+ currentTenant);
            if (currentTenant == null || currentTenant.isEmpty()) {
                throw new IllegalStateException("No tenant database selected");
            }
            

            if (currentTenant.equals("default") || currentTenant.equals("global_multi_tenant")) {
            	System.out.println("getting welcome messsage from global DB" + currentTenant);

                // Fallback to database info message
                return jdbcTemplate.queryForObject(
                    "SELECT message FROM db_info LIMIT 1", String.class);

            } else {
            	
            	System.out.println("getting welcome messsage from tenant DB" + currentTenant);
            	// Query for user name
            	String name = userService.getUserByEmail(email).getFirstName();
            		return "Welcome " + name;
	
            }

        } catch (EmptyResultDataAccessException e) {
            return "No welcome message found";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching welcome message: " + e.getMessage();
        }
    }
}
