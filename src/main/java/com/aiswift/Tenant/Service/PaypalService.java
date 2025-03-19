package com.aiswift.Tenant.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.aiswift.Tenant.Entity.PayPal;
import com.aiswift.Tenant.Repository.PaypalRepository;
import com.aiswift.DTO.Tenant.AccessTokenResponse;
import com.aiswift.DTO.Tenant.PaypalPaymentResponse;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.paypal.base.rest.PayPalRESTException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaypalService {

	@Autowired
	PaypalRepository paypalRepository;

	private final RestTemplate restTemplate = new RestTemplate();
	private static final String PAYPAL_BASE_URL = "https://api-m.sandbox.paypal.com";

	public List<PayPal> findByTenantId(Long tenantId) {
		return paypalRepository.findByTenantId(tenantId);
	}

	public PayPal findByPaypalEmail(String email) {
		return paypalRepository.findByPaypalEmail(email);
	}

	 public byte[] createPaymentWithQRCode(String paypalEmail, BigDecimal totalPayment, String currency, Long orderId) throws WriterException, IOException, PayPalRESTException{
	    	log.info("I am at createOrderWithQRCode method");
	    	PaypalPaymentResponse paymentResponse = createPayment(paypalEmail,totalPayment,currency,orderId);
	    	if(paymentResponse == null) {
	    		throw new RuntimeException("Fail to create PayPal order");
	    	}
	    	String approvalUrl = getApprovalLink(paymentResponse.getLinks());
	    	log.info("approval link: " + approvalUrl);
	    	
	    	return generateQRCode(approvalUrl,200,200);
	    }
	public PaypalPaymentResponse createPayment(String paypalEmail, BigDecimal totalPayment, String currency, Long orderId)
			throws PayPalRESTException {

		PayPal paypal = findByPaypalEmail(paypalEmail);

		final String PAYPAL_CHECKOUT_URL = PAYPAL_BASE_URL + "/v2/checkout/orders";
		log.info("I am at createPayment method");

		// headers of the request
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(getAccessToken(paypal));
		log.info("access token: " + getAccessToken(paypal));
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> orderRequest = buildOrderRequest(totalPayment,currency,paypal,orderId.toString());

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(orderRequest, headers);
		// capture the response after exchange request:
		ResponseEntity<PaypalPaymentResponse> response = restTemplate.exchange(PAYPAL_CHECKOUT_URL, HttpMethod.POST,
				request, PaypalPaymentResponse.class);
		return response.getBody();

	}
	
	 public PaypalPaymentResponse captureOrderByToken(String token, PayPal paypal) {
	        final String PAYPAL_CAPTURE_URL = PAYPAL_BASE_URL + "/v2/checkout/orders/" + token + "/capture";
	        HttpHeaders headers = new HttpHeaders();
	        headers.setBearerAuth(getAccessToken(paypal));
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        HttpEntity<String> request = new HttpEntity<>(headers);
	        ResponseEntity<PaypalPaymentResponse> response = restTemplate.exchange(
	                PAYPAL_CAPTURE_URL,
	                HttpMethod.POST,
	                request,
	                PaypalPaymentResponse.class
	        );

	        if (response.getStatusCode() == HttpStatus.CREATED) {
	            return response.getBody();
	        } else {
	            throw new RuntimeException("Failed to capture PayPal order");
	        }
	    }
	
	
//	PRIVATE METHODS

	private String getAccessToken(PayPal paypal) {

		log.info("I am at getAccessToken method");
		final String PAYPAL_OAUTH_URL = PAYPAL_BASE_URL + "/v1/oauth2/token";
		// headers of the request
		HttpHeaders headers = new HttpHeaders();
		headers.setBasicAuth(paypal.getClientId(), paypal.getClientSecret());
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// new request with the defined-headers
		HttpEntity<String> request = new HttpEntity<String>("grant_type=client_credentials", headers);

		/*
		 * send the post request to PAYPAL_OAUTH_URL with expected response format
		 * defined by AccessTokenResponse class, capture the response after exchange
		 * request:
		 */

		ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(PAYPAL_OAUTH_URL, HttpMethod.POST, request,
				AccessTokenResponse.class);

		log.info("Response Status: {}", response.getStatusCode());
		log.info("Response Headers: {}", response.getHeaders());
		// check the response body
		log.info("Response Body: {}", response.getBody());

		if (response.getStatusCode().is2xxSuccessful()) {
			AccessTokenResponse tokenResponse = response.getBody();

			if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
				throw new IllegalArgumentException("Invalid PayPal access token");
			}

			return tokenResponse.getAccessToken();
		} else {
			throw new IllegalArgumentException("PayPal authentication failed");
		}

	}
	
	private Map<String, Object> buildOrderRequest(BigDecimal totalPayment, String currency, PayPal paypal,String orderId) {
		Map<String,Object> orderRequest = new HashMap<>();
		
		// Set intent to capture payment
		orderRequest.put("intent", "CAPTURE");
		
		  // Purchase units (payment details)
		Map<String,Object> purchaseUnit = new HashMap<>();
		Map<String,Object> amount = new HashMap<>();
		
		 // Amount details
		amount.put("currency_code", currency);
		amount.put("value", totalPayment.toString());
		purchaseUnit.put("amount", amount);
		
		// Payee (recipient) details
	    Map<String, Object> payee = new HashMap<>();
	    payee.put("email_address", paypal.getPaypalEmail());
	    purchaseUnit.put("payee", payee);
	 // Add reference ID for the order
	    purchaseUnit.put("reference_id", orderId);
	    
		orderRequest.put("purchase_units", Arrays.asList(purchaseUnit));
		
		// Add return_url and cancel_url with query parameters for PayPal redirection
	    Map<String, String> applicationContext = new HashMap<>();
	    applicationContext.put("return_url", "http://localhost:8080/tenant/payment/confirm?orderId=" + orderId);
	    applicationContext.put("cancel_url", "http://localhost:8080/tenant/payment/cancel?orderId=" + orderId + "&amount=" + totalPayment);
	    
	    orderRequest.put("application_context", applicationContext);
		
		return orderRequest;
	}
	
	private byte[] generateQRCode(String text, int width, int height) throws WriterException, IOException{
    	QRCodeWriter qrCodeWriter = new QRCodeWriter();
    	BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE,width,height); //writerException can happen here
    	
    	ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
    	MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
    	return pngOutputStream.toByteArray();

    }
    
    private String getApprovalLink(List<PaypalPaymentResponse.Link>links) {
    	for(PaypalPaymentResponse.Link link : links) {
    		if("approve".equalsIgnoreCase(link.getRel())) {
    			return link.getHref();
    		}
    	}
    	throw new RuntimeException("Approval link not found");
    }
}
