package com.aiswift.Global.Entity;

import com.aiswift.Common.Entity.BaseUser;
import com.aiswift.Enum.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode (callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "developers", schema = "global_multi_tenant")
public class Developer extends BaseUser{

	@Enumerated(EnumType.STRING)
	@Column
	private Role role = Role.ADMIN;

}
