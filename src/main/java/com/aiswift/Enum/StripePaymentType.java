package com.aiswift.Enum;


public enum StripePaymentType {
	SUBSCRIPTION, ADDITIONAL_ADMIN, ADDITIONAL_TENANT, MONTHLY_PAYMENT;

	public static StripePaymentType fromString(String type) {
		try {
			return StripePaymentType.valueOf(type.toUpperCase()); // check input with enum
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Invalid payment type: " + type);
		}
	}
}

