package com.aiswift.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.aiswift.Common.Service.CustomUserDetailsService;





@Configuration
public class SecurityConfig {
	
	@Autowired
	CustomUserDetailsService customUserDetailsService;
	
	@Autowired
	private TenantFilter tenantFilter;
	
	@Autowired
	private JwtFilter jwtFilter;
	
	@Autowired CustomUsernamePasswordAuthenticationFilter loginFilter;
	
	
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		
		
		http
	     // Tenant filter runs before JWT filter
        .addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class)
		
		// JWT filter runs after tenant filter but before authentication
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
   
		.authorizeHttpRequests(
				(requests) -> requests
				.requestMatchers("/api/login").permitAll()
				.requestMatchers("/api/user/profile").hasAnyRole("GUEST", "ADMIN")
				.requestMatchers("/api/admin/**").hasRole("ADMIN")
				.requestMatchers("/api/owner/**").hasRole("OWNER")
				.requestMatchers("/api/tenant/**").hasAnyRole("STAFF_LEVEL_1", "STAFF_LEVEL_2","ADMIN","SUPERVISOR")
				.anyRequest().authenticated()) 
		// logout is managed on client's side : remove JWT, redirect user to the login page
		//can add server-side validation later (blacklist) .. to stop validate jwt after logout
				.sessionManagement(session -> session
			            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				        )
				.csrf(csrf -> csrf.disable());
		
		return http.build();
		
	}
	
	

}
