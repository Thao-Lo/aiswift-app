package com.aiswift.DTO.Tenant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailDto {

	private Long productId;
	private Integer quantity;
	private Long sizeId;
}
