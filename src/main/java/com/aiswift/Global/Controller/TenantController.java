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

import com.aiswift.Exception.UnExpectedStatusException;
import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Entity.SubPlanDetail;
import com.aiswift.Global.Entity.SubPlanDetail.PlanDetailStatus;
import com.aiswift.Global.Entity.Tenant;
import com.aiswift.Global.Service.OwnerService;
import com.aiswift.Global.Service.SubPlanDetailService;
import com.aiswift.Global.Service.TenantService;


@RestController
@RequestMapping("/api/owner")
public class TenantController {
	@Autowired
	private TenantService tenantService;
	
	@Autowired
	private OwnerService ownerService;
	
	@Autowired
	private SubPlanDetailService subPlanDetailService;
	
	@PostMapping("/create-new-tenant")
	public ResponseEntity<Object> createNewTenant(@RequestParam String shopName, Principal principal) {
		
			Owner owner = ownerService.getOwnerWithSubPlanDetails(principal.getName());	
			SubPlanDetail planDetail = subPlanDetailService.getLatestPlanDetailByOwner(owner);
			
			//check plan ACTIVATION
			if(planDetail.getStatus() != PlanDetailStatus.ACTIVE) {
				throw new UnExpectedStatusException(String.format("Your plan is: %s", planDetail.getStatus()));				
			}
			
			//check active vs max_tenant_count
			if(planDetail.getActiveTenantCount() == planDetail.getMaxTenant()) {
				throw new RuntimeException(String.format("Cannot create more tenant, %d over %d Max Tenants",
						planDetail.getActiveTenantCount(), planDetail.getMaxTenant()));				
			}
			//create new DB, flyway migrate, insert data to tenant_admin_limit
			tenantService.createNewTenant(shopName, owner.getId());
			
			Tenant tenant = tenantService.getTenantByDatabaseName(shopName + "_db");
			
			//update active_tenant_count in planDetail and update tenant_activity_log
			subPlanDetailService.updateActiveTenantCount(planDetail.getId(), owner, tenant);		
			
			return new ResponseEntity<>(Map.of("message", "Successully add new Tenant"), HttpStatus.OK);		
		
	}
}
