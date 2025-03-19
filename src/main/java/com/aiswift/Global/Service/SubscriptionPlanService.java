package com.aiswift.Global.Service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aiswift.Exception.NoDataFoundException;
import com.aiswift.Global.Entity.SubscriptionPlan;
import com.aiswift.Global.Repository.SubscriptionPlanRepository;

@Service
public class SubscriptionPlanService {
	@Autowired
	private SubscriptionPlanRepository subPlanRepository;

	public List<SubscriptionPlan> getSubscriptionPlanList() {
		List<SubscriptionPlan> plans = subPlanRepository.findAll();
		if (plans.isEmpty()) {
			throw new NoDataFoundException("No subscription plans available");
		}
		return plans;
	}
	
	public SubscriptionPlan getPlanById(int id) {
		return subPlanRepository.findById(id)
				.orElseThrow(() -> new NoDataFoundException("No plan found."));				
	}
	
	//calculate fee for 1st time subscribe the plan 
	public BigDecimal calculatePlanFee(int additionalAdmin, int additionalTenant, SubscriptionPlan plan) {				
		BigDecimal baseCost = plan.getBaseCost();
		BigDecimal adminFee = BigDecimal.valueOf(Math.max(additionalAdmin, 0)).multiply(plan.getAdditionalAmindFee());
		BigDecimal tenantFee = BigDecimal.valueOf(Math.max(additionalTenant, 0)).multiply(plan.getAdditionalTenantFee());
		
		return baseCost.add(adminFee).add(tenantFee);
	}

}
