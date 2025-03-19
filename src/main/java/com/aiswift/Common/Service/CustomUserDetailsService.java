package com.aiswift.Common.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aiswift.Common.DTO.CustomUserDetails;
import com.aiswift.Enum.Status;

import com.aiswift.Global.Entity.Developer;
import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Repository.DeveloperRepository;
import com.aiswift.Global.Repository.OwnerRepository;
import com.aiswift.MultiTenancy.TenantContext;
import com.aiswift.Tenant.Entity.TenantUser;
import com.aiswift.Tenant.Repository.TenantUserRepository;

@Lazy
@Service
public class CustomUserDetailsService implements UserDetailsService{

	@Autowired
    private OwnerRepository ownerRepository;
	
	@Autowired
	private DeveloperRepository developerRepository;
	
	@Autowired
	private TenantUserRepository tenantUserRepository;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		System.out.println("I am in custom user details service");
		System.out.println("email: "+email);
		// Check header/tenant context to decide which repo to use
        String dbName = TenantContext.getCurrentTenant(); //null
        String userRole = TenantContext.getCurrentUserRole();
        if (dbName.equals("global_multi_tenant") || dbName.equals("default")) {
        	System.out.println("I am in Custom User Details Service, db name is pointing to global");
        	System.out.println("Thread ID: "+ Thread.currentThread().getId() +" userRole "+ userRole);
        	if(userRole.equals("DEVELOPER")) {
        		// Use developer table
        		Developer developer = developerRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Developer Not found: " + email));
        		
        		List<GrantedAuthority> authorities = new ArrayList<>();
        		authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                return new CustomUserDetails(
                		developer.getEmail(),
                		developer.getPassword(),
                		Status.ACTIVE,
                		authorities,
                		dbName
                		);
        	}else if(userRole.equals("OWNER")) {
        		// Use owner table
                Owner owner = ownerRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Owner Not found: " + email)); 
                List<GrantedAuthority> authorities = new ArrayList<>();
        		authorities.add(new SimpleGrantedAuthority("ROLE_OWNER"));
        		authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

                return new CustomUserDetails(
                		owner.getEmail(),
                		owner.getPassword(),
                		Status.ACTIVE,
                		authorities,
                		dbName
                		);
        		
        	}
    	System.out.println("I am in Custom User Details Service, no user found");
    	return null;
        	
        
        }else {
        	System.out.println("I am in Custom user Details Service, dbName is "+ dbName);
        	try {
                TenantUser tenantUser = tenantUserRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Tenant User Not found: " + email));
                List<GrantedAuthority> authorities = new ArrayList<>();
        		authorities.add(new SimpleGrantedAuthority("ROLE_"+tenantUser.getRole().getName()));
                return new CustomUserDetails(
                    tenantUser.getEmail(),
                    tenantUser.getPassword(),
                    tenantUser.getStatus(),
                    authorities,
                    dbName
                );
            } catch (Exception e) {
                System.err.println("Error finding user in tenant " + dbName + ": " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }

        
	}

}
