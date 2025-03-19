package com.aiswift.Tenant.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
	private long id;
	private String firstName;
	private String email;
	private List<String> role;
}
