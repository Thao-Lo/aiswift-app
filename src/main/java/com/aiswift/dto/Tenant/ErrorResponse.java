package com.aiswift.dto.Tenant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
	
	 private int status;
     private String message;
     private long timestamp;
}
