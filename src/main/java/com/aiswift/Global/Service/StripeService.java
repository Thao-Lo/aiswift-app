package com.aiswift.Global.Service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aiswift.Global.DTO.PaymentIntentDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.PaymentIntentCreateParams;

@Service
public class StripeService {
	private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

	@Value("${stripe.api.global-secret-key}")
	private String globalApiKey;

	public String getStripeApiKey() {
		return globalApiKey;
	}

	public RequestOptions createRequestOptions() {
		String apiKey = getStripeApiKey();
		return RequestOptions.builder().setApiKey(apiKey).build();
	}

	public PaymentIntentDTO createPaymentIntent(BigDecimal amount, String email, String paymentType, int count) {
		try {
			RequestOptions requestOptions = createRequestOptions();

			// removes the decimal part if there are only trailing .00
			long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValueExact();
			
			PaymentIntent paymentIntent = PaymentIntent.create(PaymentIntentCreateParams.builder()
					.setAmount(amountInCents).setCurrency("aud").setReceiptEmail(email)
					.putMetadata("payment_type", paymentType).putMetadata("count", String.valueOf(count))
					.setAutomaticPaymentMethods( // TESTING WITHOUT USING FE // No redirect
							PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true)
									.setAllowRedirects(
											PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
									.build())
					.build(), requestOptions);
			
			logger.info("New PaymentIntent created with Id: " + paymentIntent.getId() );
			
			return new PaymentIntentDTO(paymentIntent);
			
		} catch (StripeException e) {
			throw new RuntimeException("Stripe error: " + e.getMessage());			
		}		
	}

}
