package com.aiswift.Global.Entity;

import java.util.ArrayList;
import java.util.List;
import com.aiswift.Tenant.Entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
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
@Table(name = "owners", schema = "global_multi_tenant")
public class Owner extends User {

	private static final long serialVersionUID = 1L;
	@OneToMany (mappedBy = "owner", cascade = CascadeType.ALL) //owner lowecase, field in Tenant
	@JsonIgnore
	List<Tenant> tenants = new ArrayList<Tenant>();
	
	
}

