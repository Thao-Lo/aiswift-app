package com.aiswift.dto;

import java.util.List;

import com.aiswift.Global.Entity.Tenant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerResponse {
	Long id; 
	String name;
	String status;
	List<Tenant>tenants;
	
}
