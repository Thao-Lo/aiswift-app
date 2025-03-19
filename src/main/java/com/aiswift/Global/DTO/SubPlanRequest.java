package com.aiswift.Global.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubPlanRequest {
	
	private int subPlanId;	
	
	@JsonProperty(defaultValue = "0")
	@Min(value = 0, message ="Additional admin count cannot be negative")
	private int additionalAdminCount;
	
	@JsonProperty(defaultValue = "0")
	@Min(value = 0, message ="Additional tenant count cannot be negative")
	private int additionalTenantCount;
}
