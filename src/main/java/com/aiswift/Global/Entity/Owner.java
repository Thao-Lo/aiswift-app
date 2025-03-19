package com.aiswift.Global.Entity;

import java.util.ArrayList;
import java.util.List;

import com.aiswift.Common.Entity.BaseUser;
import com.aiswift.Enum.Status;
import com.aiswift.Tenant.Entity.Order;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
//@EqualsAndHashCode (callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "owners", schema = "global_multi_tenant")
public class Owner extends BaseUser {
	@Enumerated(EnumType.STRING)
	@Column
	private Status status = Status.ACTIVE;	
	
	@OneToMany (mappedBy = "owner", cascade = CascadeType.ALL) //owner lowecase, field in Tenant
	@JsonIgnore
	List<Tenant> tenants = new ArrayList<Tenant>();
	
	@Transient
    private List<Order> orders = new ArrayList<>();
	
	@JsonIgnore
	@OneToMany(mappedBy = "owner", fetch = FetchType.LAZY) //On delete restrict
	private List<SubPlanDetail> subPlanDetails = new ArrayList<>();
	
	@JsonIgnore
	@OneToMany(mappedBy = "owner", fetch = FetchType.LAZY) //On delete restrict
	private List<Payment> payments = new ArrayList<>();
	
	@JsonIgnore
	@OneToMany (mappedBy = "owner", fetch = FetchType.LAZY) 	
	List<TenantActivityLog> tenantActivityLogs = new ArrayList<>();
}

