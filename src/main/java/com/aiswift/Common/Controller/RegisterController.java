package com.aiswift.Common.Controller;

import java.security.Principal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiswift.Common.DTO.RegisterRequest;
import com.aiswift.Common.DTO.ResetPasswordRequest;
import com.aiswift.Common.Service.AuthService;
import com.aiswift.Common.Service.RegisterService;
import com.aiswift.Common.Service.ResetPasswordService;
import com.aiswift.Exception.UnAuthorizedException;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class RegisterController {

	@Autowired
	private RegisterService registerService;

	@Autowired
	private ResetPasswordService resetPasswordService;

	@Autowired
	private AuthService authService;

	private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

	@PostMapping("/developer/register")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN)")
	public ResponseEntity<Object> registerGlobal(@RequestBody RegisterRequest request, Principal principal,
			HttpServletRequest httpRequest) {

		String role = authService.getCurrentUserRole(principal);
		if (role.equals("ADMIN") && !request.getUserType().equalsIgnoreCase("OWNER")) {
			throw new UnAuthorizedException("Admin (Developer) can only register Owner.");			
		}

		registerService.registerNewUser(request, principal, null);
		logger.info("Registered New User.");
		return new ResponseEntity<>(Map.of("message", "Please check your email to reset password."), HttpStatus.OK);
	}

	@PostMapping("/admin/register")
	@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
	public ResponseEntity<Object> registerTenant(@RequestBody RegisterRequest request, Principal principal,
			HttpServletRequest httpRequest) {
		// fe will attach userType:USER to DTO, not showing on screen
		String shopName = httpRequest.getHeader("shop-name");

		if (!"USER".equalsIgnoreCase(request.getUserType())) {
			throw new IllegalArgumentException("Only 'USER' type is allowed for tenant registration.");			
		}

		registerService.registerNewUser(request, principal, shopName);
		return new ResponseEntity<>(Map.of("message", "Please check your email to reset password."), HttpStatus.OK);

	}
	// Public Access
	@PostMapping("/public/reset-password")
	public ResponseEntity<Object> resetPassword(@RequestBody ResetPasswordRequest request,
			HttpServletRequest httpRequest) {
		String shopName = httpRequest.getHeader("shop-name");
		String globalUser = httpRequest.getHeader("global-user");

		resetPasswordService.resetUserPassword(request.getToken(), request.getNewPassword(), shopName, globalUser);
		
		logger.info("New User changed password.");
		return new ResponseEntity<>(Map.of("message", "Successfully reset new password."), HttpStatus.OK);

	}
}
