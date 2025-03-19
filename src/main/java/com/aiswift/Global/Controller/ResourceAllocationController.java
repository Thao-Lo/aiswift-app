package com.aiswift.Global.Controller;
import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Entity.SubPlanDetail;
import com.aiswift.Global.Service.OwnerService;
import com.aiswift.Global.Service.SubPlanDetailService;
import com.aiswift.Global.Service.TenantService;

import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/owner")
public class ResourceAllocationController {
	@Autowired
	private SubPlanDetailService subPlanDetailService;

	@Autowired
	private OwnerService ownerService;
	
	@Autowired
	private TenantService tenantService;

	@PostMapping("/allocate-admin")
	public ResponseEntity<Object> allocatedAdmin(@RequestParam @Min(1) int count, @RequestParam  @Min(1) Long tenantId,
			Principal principal) {
		
			// get owner
			Owner owner = ownerService.getOwnerWithSubPlanDetails(principal.getName());
			// get plan details
			SubPlanDetail planDetail = subPlanDetailService.getLatestPlanDetailByOwner(owner);
			
			// check allocated admin
			int availableAllocatedAdmin = planDetail.getAdditionalAdminCount()
					- planDetail.getAllocatedAdditionalAdmin();
			
			if (availableAllocatedAdmin <= 0) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "No available additional Admin to allocate"));
			}
			if(count > availableAllocatedAdmin) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Only %d available Admin can be allocated".formatted(availableAllocatedAdmin)));
			}
						
			// allocate -> update tenant table
			// tenant-log
			tenantService.updateAdminCount(tenantId, count, owner, planDetail);	
			
			return ResponseEntity.ok(Map.of("message", "Allocated admin successfully."));
		
	}
}
