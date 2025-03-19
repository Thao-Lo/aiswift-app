package com.aiswift.Tenant.Entity;

import java.util.ArrayList;
import java.util.List;

import com.aiswift.Common.Entity.BaseUser;
import com.aiswift.Enum.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
public class TenantUser extends BaseUser {	
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "role_id") //have column role_id in users
	private Role role;
	
	@Transient
    private List<Order> orders = new ArrayList<>();
	

	@Enumerated(EnumType.STRING)
	@Column
	private Status status = Status.ACTIVE;
	
	@PrePersist
	public void setDefaultRoleIfNull() {
		if(this.role == null) {
			this.role = new Role(); //create new Object
			this.role.setId(4); // asign roleId = 4 (staff level 2)
		}
	}
}
