package com.aiswift.Config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import com.aiswift.MultiTenancy.JwtUtil;
import com.aiswift.MultiTenancy.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
    private JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
  
    @Autowired
    public CustomUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil=jwtUtil;
        // Set the authentication manager in the parent class
        setAuthenticationManager(authenticationManager);
        // Set filter URL
        setFilterProcessesUrl("/api/public/login");
    }
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // Handle form-encoded data
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            
            System.out.println(username);
            System.out.println(password);
            
            if (username == null || password == null) {
                throw new AuthenticationServiceException("Username or password not provided");
            }            
            
            // Create authentication token
            UsernamePasswordAuthenticationToken authRequest = 
                new UsernamePasswordAuthenticationToken(username, password);
            System.out.println(authRequest);
            
            return authenticationManager.authenticate(authRequest);
        } catch (AuthenticationException e) {
            // Let Spring Security handle authentication exceptions
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthenticationServiceException("Failed to parse authentication request: " + e.getMessage(), e);
        }
    }
    
    @Override
    protected void successfulAuthentication(HttpServletRequest request, 
                                           HttpServletResponse response,
                                           FilterChain chain, 
                                           Authentication authResult) throws IOException, ServletException {
        // Generate JWT on successful authentication
        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        // Generate JWT access token
        String jwt = jwtUtil.generateToken(userDetails);
        // Generate refresh token
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        
        System.out.println("jwt: "+ jwt);
        
        // Create response body with user info and tokens
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("access_token", jwt);
        responseBody.put("refresh_token", refreshToken);
        
        // Add user information
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", userDetails.getUsername());
        userInfo.put("roles", userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toList()));
        String dbName = TenantContext.getCurrentTenant();
        // Add tenant/shop information if available
        if (dbName != null) {
            userInfo.put("db_name", dbName);
        }
        
        String userRole = TenantContext.getCurrentUserRole();
        if (userRole != null) {
            userInfo.put("user_type", userRole);
        } else {
            userInfo.put("user_type", "USER");
        }
        
        responseBody.put("user", userInfo);
        
        // Set response status and content type
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        
     // Write the response body as JSON
        response.getWriter().write(new ObjectMapper().writeValueAsString(responseBody));
       
        
               
    }
    
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, 
                                             HttpServletResponse response,
                                             AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        
        Map<String, String> errorBody = new HashMap<>();
        errorBody.put("error", "Authentication failed");
        errorBody.put("message", failed.getMessage() != null ? failed.getMessage() : "Invalid credentials");
        
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorBody));
    }
}
    
    
    
