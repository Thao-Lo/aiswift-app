package com.aiswift.Global.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiswift.Global.DTO.OwnerResponse;
import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Service.DeveloperService;
import com.aiswift.Global.Service.OwnerService;


@RestController
@RequestMapping("/api/developer")
public class DeveloperController {

	@Autowired
	DeveloperService developerService;

	@Autowired
	OwnerService ownerService;
	
	private final static Logger logger = LoggerFactory.getLogger(DeveloperController.class);	
	
	@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
	@GetMapping("/owner-list")
	public ResponseEntity<Object> getOwnerList(Principal principal) {
	
	  
	        List<Owner> owners = ownerService.findAll();
	        List<OwnerResponse> ownerResponses = owners.stream()
	            .map(owner -> new OwnerResponse(
	                owner.getId(),
	                owner.getFirstName(),
	                owner.getStatus().name(),
	                owner.getTenants()
	            ))
	            .collect(Collectors.toList());
	    logger.info("Successfully retrieved owner list.");
	    
	        return new ResponseEntity<>(Map.of(
	            "message", "Successfully retrieved the Owner List", 
	            "ownerList", ownerResponses), 
	            HttpStatus.OK);
	   
	}
	
	
}
