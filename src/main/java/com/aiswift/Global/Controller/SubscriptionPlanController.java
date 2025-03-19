package com.aiswift.Global.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aiswift.Global.Entity.SubscriptionPlan;
import com.aiswift.Global.Service.SubscriptionPlanService;

@RestController
@RequestMapping("/api/owner")
public class SubscriptionPlanController {
	@Autowired
	private SubscriptionPlanService subPlanService;

	@GetMapping("/subscription-plans")
	public ResponseEntity<Object> getAllSubcriptionPlans() {	
			List<SubscriptionPlan> plans = subPlanService.getSubscriptionPlanList();
			return ResponseEntity.ok(plans);
	}
}
