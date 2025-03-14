package com.aiswift.Global.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiswift.Global.Service.CustomUserDetailsService;
import com.aiswift.MultiTenancy.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/auth")
public class AuthController {
	
	@Autowired
	JwtUtil jwtUtil;
	
	@Autowired
	CustomUserDetailsService userDetailsService;

	@PostMapping("/refresh")
	public ResponseEntity<String> refreshToken(HttpServletRequest request,HttpServletResponse response) {
	    // Extract refresh token from cookies
	    String refreshToken = extractTokenFromCookies(request, "refresh_token");

	    if (refreshToken == null) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token not found");
	    }

	    try {
	        // Validate refresh token
	        String username = jwtUtil.extractUsername(refreshToken);
	        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

	        if (!jwtUtil.validateToken(refreshToken, userDetails)) {
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
	        }

	        // Generate new access token
	        String newAccessToken = jwtUtil.generateToken(userDetails);

	        // Set new access token as HTTP-only cookie
	        Cookie accessCookie = new Cookie("access_token", newAccessToken);
	        accessCookie.setHttpOnly(true);
	        accessCookie.setPath("/");
	        accessCookie.setMaxAge(3600*24); // 24 hours - 1 day

	        response.addCookie(accessCookie);

	        return ResponseEntity.ok("Token refreshed successfully");

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error processing refresh token");
	    }
	}
	
	private String extractTokenFromCookies(HttpServletRequest request, String cookieName) {
	    if (request.getCookies() != null) {
	        for (Cookie cookie : request.getCookies()) {
	            if (cookie.getName().equals(cookieName)) {
	                return cookie.getValue();
	            }
	        }
	    }
	    return null; // Return null if no cookie is found
	}

}
