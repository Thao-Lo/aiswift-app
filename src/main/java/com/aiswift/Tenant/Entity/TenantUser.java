package com.aiswift.Tenant.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true) //This ensures that two TenantUser instances are considered equal only if their inherited (id, name, etc.) and specific (role) fields match.
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class TenantUser extends User {
	private static final long serialVersionUID = 1L;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id") //have column role_id in users
	private Role role;
	
	
}
