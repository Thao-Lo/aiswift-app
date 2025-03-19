package com.aiswift.Global.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aiswift.Global.Entity.SubscriptionPlan;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Integer>{
	
}