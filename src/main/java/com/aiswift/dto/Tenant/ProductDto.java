package com.aiswift.dto.Tenant;

import org.springframework.context.annotation.Conditional;
import org.springframework.web.multipart.MultipartFile;

import com.aiswift.Config.TenantDatabaseCondition;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Conditional(TenantDatabaseCondition.class)  // Only create for tenant databases
public class ProductDto {

	String name;
	String description;
	MultipartFile imageData;
}
