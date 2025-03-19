package com.aiswift.Tenant.DTO;

import java.time.LocalDateTime;

import com.aiswift.Enum.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderStatusDto {

	private OrderStatus status;
    private LocalDateTime timestamp;
    private String action;
}
