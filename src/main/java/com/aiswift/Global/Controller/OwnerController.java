package com.aiswift.Global.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Entity.Tenant;
import com.aiswift.Global.Service.OwnerService;
import com.aiswift.Global.Service.TenantService;
import com.aiswift.Tenant.DTO.UserResponse;
import com.aiswift.Tenant.Entity.TenantUser;
import com.aiswift.Tenant.Service.UserService;
import com.aiswift.dto.Global.TenantResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class OwnerController {

	@Autowired
	private OwnerService ownerService;
	
	@Autowired
	private TenantService tenantService;
	
	@Autowired
	private UserService userService;
	
	@PreAuthorize("hasRole('OWNER')")
	@GetMapping("/owner/tenant-list")
	public ResponseEntity<Object> getTenantList(Principal principal){
		Owner owner = ownerService.getOwnerByEmail(principal.getName());
				
		List<Tenant> tenants = tenantService.getTenantsByOwnerId(owner.getId());
		List<TenantResponse> tenantResponse
						= tenants.stream()
						.map(tenant -> new TenantResponse(
							tenant.getId(),
							tenant.getName(),
							tenant.getDbName(),
							tenant.getStatus().toString()						
						))
						.collect(Collectors.toList());
		
		return new ResponseEntity<>(Map.of(
				"message", "Successfully get Tenant List", 
				"TenantList", tenantResponse), HttpStatus.OK);
	}
	
	@PreAuthorize("hasAnyRole('OWNER','ADMIN')")
	@GetMapping("/admin/user-list")
	public ResponseEntity<Object> getUserList(){
		List<TenantUser> users = userService.getAllUsers();
		
		List<UserResponse> userResponse 
						= users.stream()
						.map(user -> new UserResponse(
							user.getId(),
							user.getFirstName(),
							user.getEmail(), 
							List.of(user.getRole().getName())
						))
						.collect(Collectors.toList());		
		
		return new ResponseEntity<>(Map.of(
				"message", "Successfully get User List", 
				"UserList", userResponse ), HttpStatus.OK);
	}
}
