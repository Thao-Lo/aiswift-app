package com.aiswift.Global.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiswift.Global.Service.RoutingDBTestService;
import com.aiswift.MultiTenancy.TenantContext;

import jakarta.servlet.http.HttpServletRequest;



@RestController
@RequestMapping("/tenant/test")
public class RoutingTestController {
	
	@Autowired
	private RoutingDBTestService testService;
	
	@GetMapping("/welcome")
	public Map<String, Object> getWelcomeMessage(HttpServletRequest request,Authentication authentication) {
		String message = testService.getWelcomeMessage();
		String email = null;
		String jwt = null;
		String dbName = TenantContext.getCurrentTenant();
		List<String> authorities = new ArrayList<>();
		
		String authorizationHeader = request.getHeader("Authorization");

		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
		    jwt = authorizationHeader.substring(7); // Remove "Bearer " prefix
		    
		}
		
		
	        
        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            email = userDetails.getUsername();
            authorities = userDetails.getAuthorities().stream().map(auth -> auth.getAuthority()).collect(Collectors.toList());
        }
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("jwt", jwt);
        response.put("email", email);
        response.put("authorities", authorities);
        response.put("db", dbName);
        
        return response; //Spring automatically converts to JSON
		
		
//        return testService.getWelcomeMessage();

	}
	
	
}
