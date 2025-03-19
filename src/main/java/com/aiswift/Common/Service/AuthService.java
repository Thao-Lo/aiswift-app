package com.aiswift.Common.Service;

import java.security.Principal;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
	public String getCurrentUserRole(Principal principal) {
		return SecurityContextHolder.getContext().getAuthentication()
				.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.filter(role -> role.startsWith("ROLE_"))
				.map(role -> role.substring(5))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Role not found."));
	}
}
