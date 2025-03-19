package com.aiswift.Common.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
	private String firstName;
	private String lastName;
	
	@NotEmpty(message = "Email is required.")
	@Pattern(regexp = "^[\\w-.]+@[\\w-]+\\.[a-zA-Z]{2,}$", message = "Email should be valid.")  	
	private String email;
	
	@NotEmpty(message = "User Type is required.")
	private String userType;
}
