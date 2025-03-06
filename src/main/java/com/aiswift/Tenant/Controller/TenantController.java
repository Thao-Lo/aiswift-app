package com.aiswift.Tenant.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aiswift.Tenant.Service.TenantService;


@RestController
@RequestMapping("api/global")
public class TenantController {
	@Autowired
	private TenantService tenantService;
	
	@PostMapping("/create-new-tenant")
	public ResponseEntity<Object> createNewTenant(@RequestParam String shopName) {
		try {
			Long ownerId = (long) 1;
			tenantService.createNewTenant(shopName, ownerId);
			return new ResponseEntity<>(Map.of("message", "Successully add new Tenant"), HttpStatus.OK);
			
		}catch (RuntimeException e){
			return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.BAD_REQUEST);
		}
		
	}
}
