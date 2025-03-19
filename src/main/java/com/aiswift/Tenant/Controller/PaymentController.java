package com.aiswift.Tenant.Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Tenant.Entity.Order;
import com.aiswift.Tenant.Entity.PayPal;
import com.aiswift.Tenant.Service.OrderService;
import com.aiswift.Tenant.Service.PaypalService;
import com.aiswift.Tenant.Service.RefundService;
import com.aiswift.DTO.Tenant.PaypalPaymentResponse;
import com.google.zxing.WriterException;
import com.paypal.base.rest.PayPalRESTException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Conditional(TenantDatabaseCondition.class) // Only create for tenant databases
@RestController
@RequestMapping("/api/tenant/payment")
public class PaymentController {
	
	@Autowired
	PaypalService paypalService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	RefundService refundService;
	
	

	@GetMapping("/paypal/{orderId}")
	public ResponseEntity<String> createPaypalPaymentWithQRCode(@RequestParam(name="email",required=true) String paypalEmail,@RequestParam(name="totalAmount",required=true)  BigDecimal totalPayment,@RequestParam(name="currency",required=true)  String currency, @PathVariable Long orderId) throws WriterException, IOException, PayPalRESTException{
		byte[] qrCodeBytes= paypalService.createPaymentWithQRCode(paypalEmail, totalPayment, currency, orderId);
		String base64Image = Base64.getEncoder().encodeToString(qrCodeBytes);
		return ResponseEntity.ok(base64Image);
		
	}
	
	@GetMapping("/paypal/confirm")
	public ResponseEntity<Order> handlePaypalResponse(@RequestParam(name="token",required=true)String token, @RequestParam(name="paypal-email",required=true) String paypalEmail){
		PayPal paypal = paypalService.findByPaypalEmail(paypalEmail);
		PaypalPaymentResponse response = paypalService.captureOrderByToken(token, paypal);
		String paymentStatus = response.getStatus();
		// Safely extract order ID
        if (response.getPurchaseUnits() == null || response.getPurchaseUnits().isEmpty()) {
            throw new IllegalArgumentException("No purchase units found in PayPal response");
        }
		if("COMPLETED".equals(paymentStatus)) {
			String paypalOrderId = response.getId();
			String orderId = response.getPurchaseUnits().get(0).getReference_id();
			Order order = orderService.getOrderById(Long.parseLong(orderId));
			order = orderService.saveOrderPaidByPaypal(order,paypalOrderId);
			return ResponseEntity.ok(order);
			
		}
		   // Handle non-completed status
		   throw new IllegalStateException("Payment not completed. Status: " + paymentStatus);
	}
	
	@GetMapping("/cancel")
	public ResponseEntity<Map<String, Object>> handlePaymentCancellation(
	        @RequestParam(required = false) String orderId,
	        @RequestParam(required = false) BigDecimal amount) {
	    
	    log.info("Payment cancellation API request received. Order ID: {}, Amount: {}", orderId, amount);
	    Map<String, Object> response = new HashMap<>();
	    
	    // If we have an order ID, update the order status
	    if (orderId != null) {
	        Long orderIdLong = Long.parseLong(orderId);
	        Order order = orderService.getOrderById(orderIdLong);
	        orderService.cancelOrder(order);
	        log.info("Order {} status updated to VOIDED after payment cancellation", orderId);
	        
	        response.put("orderId", orderId);
	        response.put("amount", amount);
	        response.put("status", "CANCELED");
	        response.put("message", "Payment was canceled. Order has been voided.");
	    } else {
	        response.put("status", "CANCELED");
	        response.put("message", "Payment was canceled. No order ID was provided.");
	    }
	    
	    return ResponseEntity.ok(response);
	}
	
	@PostMapping("/cash/refund-request")
	public ResponseEntity<Order>requestRefund(@RequestParam(name="orderId",required=true)Long orderId){
		Order order = refundService.requestRefund(orderId);
		return ResponseEntity.ok(order);
	}
	
	@PostMapping("/cash/refund-approved-process")
	public ResponseEntity<Order>processRefund(@RequestParam(name="orderId",required=true)Long orderId){
		Order order = refundService.processRefund(orderId,true);
		return ResponseEntity.ok(order);
	}
	
	
}
