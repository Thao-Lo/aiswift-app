package com.aiswift.Global.Controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aiswift.Global.Entity.Developer;
import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Service.DeveloperService;
import com.aiswift.Global.Service.OwnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api")
public class DeveloperController {

	@Autowired
	DeveloperService developerService;

	@Autowired
	OwnerService ownerService;

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/owner-list")
	public ResponseEntity<Object> getOwnerList(Principal principal) {
		System.out.println("I am in developer controller");
	    Optional<Developer> developerOptional = developerService.getDeveloperByEmail(principal.getName());
	    
	    if(developerOptional.isPresent()) {
	        List<Owner> owners = ownerService.findAll();
	        List<OwnerResponse> ownerResponses = owners.stream()
	            .map(owner -> new OwnerResponse(
	                owner.getId(),
	                owner.getFirstName(),
	                owner.getStatus().name(),
	                owner.getTenants()
	            ))
	            .collect(Collectors.toList());
	            
	        return new ResponseEntity<>(Map.of(
	            "message", "Successfully get Owner List", 
	            "ownerList", ownerResponses), 
	            HttpStatus.OK);
	    } else {
	        return new ResponseEntity<>(Map.of(
	            "message", "Developer not found"),
	            HttpStatus.NOT_FOUND);
	    }
	}
	
	
}
