package com.aiswift.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Entity.Tenant;
import com.aiswift.Global.Service.CustomUserDetailsService;
import com.aiswift.Global.Service.OwnerService;
import com.aiswift.Global.Service.TenantService;
import com.aiswift.MultiTenancy.JwtUtil;
import com.aiswift.MultiTenancy.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	JwtUtil jwtUtil;
	
	@Autowired
	OwnerService ownerService;

	@Autowired
	CustomUserDetailsService customUserDetailsService;
	
	@Autowired
	TenantService tenantService;

    private static final Logger logger = LoggerFactory.getLogger(TenantFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		System.out.println("I am in jwt filter");
		final String authorizationHeader = request.getHeader("Authorization");
		String username = null;
		String jwt = null;
		List<SimpleGrantedAuthority> authorities= new ArrayList<>();
		
		Map<String, Object> errorDetails = new HashMap<>();
		//locate the db
		String dbName = TenantContext.getCurrentTenant();
		try {
			System.out.println("I am in jwt filter");
			//decode jwt to get username
			if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
				jwt = authorizationHeader.substring(7);
				//validate jwt here!
				username = jwtUtil.extractUsername(jwt);
				authorities = jwtUtil.extractAuthorities(jwt);
			}
			System.out.println("username found in jwt : "+ username);
			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				System.out.println("username is not null and no user been authenticated yet");
				//update userRole
				if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_OWNER")))  {
					System.out.println("JwtFilter: setting role for user as owner");
					TenantContext.setCurrentUserRole("OWNER");
					TenantContext.setCurrentTenant("global_multi_tenant");
					//validate jwt and bypass authentication
					authenticateUser(username,jwt,request);
					if(!dbName.equals("default") && !dbName.equals("global_multi_tenant")) { //switch back to tenantDB after successful authentication
						Owner owner = ownerService.getOwnerByEmail(username);
    					// check if Owners has ShopName
    					List<Tenant> tenants = tenantService.getTenantsByOwnerId(owner.getId());
    					// Find matching tenant
    	                boolean tenantFound = false;
    					for (Tenant tenant : tenants) {
    						if (tenants!=null && tenant.getDbName() != null) { 
    							//switch to db of tenant
    							TenantContext.setCurrentTenant(tenant.getDbName());
    							tenantFound = true;
    	                        break;
    							
    						}
    					}
    					// No matching tenant found
    	                if (!tenantFound) {
    	                    logger.warn("No matching tenant found for owner: {} and db: {}, using default", 
    	                               username, dbName);
    	                    TenantContext.setCurrentTenant("default");
    	                }
					}
				}else if (authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
			        System.out.println("JwtFilter: setting role for user as developer");
			        if(!dbName.equals("default") && !dbName.equals("global_multi_tenant")) {
			        	errorDetails.put("message", "Authentication Error");
						errorDetails.put("detail", "developer is not allowed to login to tenant");
						response.setStatus(HttpStatus.FORBIDDEN.value());
						response.setContentType(MediaType.APPLICATION_JSON_VALUE);
						ObjectMapper mapper = new ObjectMapper();
						mapper.writeValue(response.getWriter(), errorDetails);
			        }else {
			        	TenantContext.setCurrentUserRole("DEVELOPER");
			        	authenticateUser(username,jwt,request);
			        	System.out.println("Processing request to: " + request.getRequestURI());
			        	
			        }
			    }else {
			    	//normal staff login into tenant
			    	authenticateUser(username,jwt, request);
			    }
			
			}
		} catch (Exception e) {
			System.out.println("caught exception somehow!!");
			errorDetails.put("message", "Authentication Error");
			errorDetails.put("detail", e.getMessage());
			response.setStatus(HttpStatus.FORBIDDEN.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(response.getWriter(), errorDetails);
		}
		// hand off the control to the next filter in the filter chain
		filterChain.doFilter(request, response);

	}
	
	private void authenticateUser(String username,String jwt,HttpServletRequest request) {
		// runtime poly
		UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
		
		System.out.println("JwtFilter:Double check user Roles after updated in tenant context:"+ userDetails.getAuthorities().stream().map(authority -> authority.getAuthority())
    .collect(Collectors.toList()));
		
		// verify if the token is validated
		System.out.println(" validating token");
		if (jwtUtil.validateToken(jwt, userDetails)) {
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
					userDetails, null, userDetails.getAuthorities());
			usernamePasswordAuthenticationToken
					.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			//set userDetail, bypass authentication 
			SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
			
		}
	}
	
}


