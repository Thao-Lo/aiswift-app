package com.aiswift.Tenant.Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.aiswift.Enum.OrderStatus;
import com.aiswift.dto.Tenant.OrderStatusDto;

public class StatusHistorySerializer {

	public static String serializeStatusHistory(List<OrderStatusDto> orderStatusChanges) {
		// Convert to compact JSON or compressed string
		return orderStatusChanges.stream().map(change -> change.getStatus() + ":" + change.getTimestamp()+":" + change.getAction())
				.collect(Collectors.joining(";"));
	}

	public static List<OrderStatusDto> deserializeStatusHistory(String compressedHistory) {
		if (compressedHistory == null) {
			return new ArrayList<>();
		}

		return Arrays.stream(compressedHistory.split(";")).map(entry -> {
			String[] parts = entry.split(":");
			return new OrderStatusDto(OrderStatus.valueOf(parts[0]), LocalDateTime.parse(parts[1]),parts.length > 2 ? parts[2] : null);
		}).collect(Collectors.toList());
	}
}
