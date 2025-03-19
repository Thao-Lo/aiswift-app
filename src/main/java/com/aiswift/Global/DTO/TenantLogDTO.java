package com.aiswift.Global.DTO;

import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Entity.SubPlanDetail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TenantLogDTO {
	private Owner owner;
	private long tenantId;
	private SubPlanDetail planDetail;
	private String oldValue;	
	private String newValue;
	private int actionTypeId;
	private String message;
}
