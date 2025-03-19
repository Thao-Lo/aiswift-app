package com.aiswift.Global.Entity;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subscription_plan_detail", schema = "global_multi_tenant")
public class SubPlanDetail {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "active_tenant_count")
	private int activeTenantCount;
	
	@Column(name = "additional_tenant_count")
	private int additionalTenantCount;

	@Column(name = "max_tenant", nullable = false)
	private int maxTenant;

	@Column(name = "allocated_additional_admin")
	private int allocatedAdditionalAdmin;

	@Column(name = "additional_admin_count")
	private int additionalAdminCount;

	@Column(name = "subscription_start")
	@CreationTimestamp
	private LocalDateTime subscriptionStart;

	@Column(name = "next_billing_date")
	private LocalDateTime nextBillingDate;

	@Enumerated(EnumType.STRING)
	private PlanDetailStatus status;

	@JsonIgnore
	@JoinColumn(name = "owner_id", nullable = false)
	@ManyToOne
	private Owner owner;

	@JsonIgnore
	@JoinColumn(name = "subscription_plan_id", nullable = false)
	@ManyToOne
	private SubscriptionPlan subscriptionPlan;

	public enum PlanDetailStatus {
		ACTIVE, INACTIVE, CANCELLED, EXPIRED;
	}
}
