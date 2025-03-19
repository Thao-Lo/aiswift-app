package com.aiswift.Global.DTO;

import com.stripe.model.PaymentIntent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentIntentDTO {
	private String id;
	private String clientSecret;
	private Long amount;
	private String currency;
	private String status;

	// Constructor
	public PaymentIntentDTO(PaymentIntent paymentIntent) {
		this.id = paymentIntent.getId();
		this.clientSecret = paymentIntent.getClientSecret();
		this.amount = paymentIntent.getAmount();
		this.currency = paymentIntent.getCurrency();
		this.status = paymentIntent.getStatus();
	}
}