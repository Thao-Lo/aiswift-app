package com.aiswift.Global.Entity;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscription_plan", schema = "global_multi_tenant")
public class SubscriptionPlan {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "tenant_limit", nullable = false)
	private int tenantLimit;

	@Column(name = "admin_limit_per_tenant", nullable = false) 
	private int adminLimitPerTenant;

	@Column(name = "base_cost", precision = 10, scale = 2, nullable = false)
	private BigDecimal baseCost;
	
	@Column(name = "additional_tenant_fee", precision = 10, scale = 2, nullable = false)
	private BigDecimal additionalTenantFee;
	
	@Column(name = "additional_admin_fee", precision = 10, scale = 2, nullable = false)
	private BigDecimal additionalAmindFee;
	
	@Column(name = "billing_cycle", nullable = false)
	private String billingCycle;
	
	@JsonIgnore
	@OneToMany(mappedBy = "subscriptionPlan", cascade = CascadeType.ALL)
	private List<SubPlanDetail> subPlanDetails = new ArrayList<>();
}
