package com.aiswift.Global.Controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aiswift.Enum.StripePaymentType;
import com.aiswift.Global.DTO.PaymentIntentDTO;
import com.aiswift.Global.DTO.SubPlanRequest;
import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Entity.Payment;
import com.aiswift.Global.Entity.SubPlanDetail;
import com.aiswift.Global.Entity.SubscriptionPlan;
import com.aiswift.Global.Service.OwnerService;
import com.aiswift.Global.Service.PaymentService;
import com.aiswift.Global.Service.StripeService;
import com.aiswift.Global.Service.SubPlanDetailService;
import com.aiswift.Global.Service.SubscriptionPlanService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/owner")
public class PlanPaymentController {
	@Autowired
	private SubscriptionPlanService subPlanService;
	@Autowired
	private SubPlanDetailService subPlanDetailService;

	@Autowired
	private OwnerService ownerService;

	@Autowired
	private StripeService stripeService;

	@Autowired
	private PaymentService paymentService;

	private final static Logger logger = LoggerFactory.getLogger(PlanPaymentController.class);

	@PostMapping("/create-first-plan-payment")
	public ResponseEntity<Object> createFirstPlanPayment(@Valid @RequestBody SubPlanRequest request,
			Principal principal) {
		logger.info("Starting first plan payment creation for user: {}", principal.getName());

		// TODO: Check owner's current plan before proceeding (implement later)

		// Retrieve owner details
		Owner owner = ownerService.getOwnerByEmail(principal.getName());

		// Calculate total subscription plan fee
		SubscriptionPlan plan = subPlanService.getPlanById(request.getSubPlanId());

		// calculate total plan fee
		BigDecimal totalPlanFee = subPlanService.calculatePlanFee(request.getAdditionalAdminCount(),
				request.getAdditionalTenantCount(), plan);

		// Create Stripe PaymentIntent
		PaymentIntentDTO paymentIntentDTO = stripeService.createPaymentIntent(totalPlanFee, principal.getName(),
				StripePaymentType.SUBSCRIPTION.name(), 1);

		// Save initial subscription plan details
		SubPlanDetail planDetail = subPlanDetailService.saveInitialPlanDetail(request, owner, plan);

		// Save initial payment record
		paymentService.saveInitPayment(owner, planDetail, totalPlanFee, paymentIntentDTO.getId(), request, plan);

		logger.info("Successfully created first plan payment for user: {}", principal.getName());
		return new ResponseEntity<>(Map.of("PaymentIntentDTO", paymentIntentDTO), HttpStatus.OK);

	}

	@PostMapping("/create-monthly-plan-payment")
	public ResponseEntity<Object> createMonthlyPlanPayment(Principal principal) {
	    logger.info("Starting monthly plan payment creation for user: {}", principal.getName());

		// Retrieve owner details with subscription plan details
		Owner owner = ownerService.getOwnerWithSubPlanDetails(principal.getName());

		SubPlanDetail planDetail = subPlanDetailService.getLatestPlanDetailByOwner(owner);
		
		 // Get the subscription plan associated with the owner
		SubscriptionPlan plan = planDetail.getSubscriptionPlan();

		// Calculate total plan fee for the monthly payment
		BigDecimal totalPlanFee = subPlanService.calculatePlanFee(planDetail.getAdditionalAdminCount(),
				planDetail.getAdditionalTenantCount(), plan);

		// Create Stripe PaymentIntent
		PaymentIntentDTO paymentIntentDTO = stripeService.createPaymentIntent(totalPlanFee, principal.getName(),
				StripePaymentType.MONTHLY_PAYMENT.name(), 1); // count: 1 ea

		// Save payment record and payment details
		paymentService.saveMonthlyPayment(owner, planDetail, totalPlanFee, paymentIntentDTO.getId(), plan);
		
		logger.info("Successfully created monthly plan payment for user: {}", principal.getName());
		return new ResponseEntity<>(Map.of("PaymentIntentDTO", paymentIntentDTO), HttpStatus.OK);
	}

	@PostMapping("/payment-retry")
	public ResponseEntity<Object> retryPayment(@RequestParam String oldPaymentIntentId, Principal principal) {
		try {
			Owner owner = ownerService.getOwnerWithSubPlanDetails(principal.getName());
			SubPlanDetail planDetail = subPlanDetailService.getLatestPlanDetailByOwner(owner);

			Payment payment = paymentService.getPaymentByPaymentIntentId(oldPaymentIntentId);

			RequestOptions requestOptions = stripeService.createRequestOptions();
			PaymentIntent paymentIntent = PaymentIntent.retrieve(oldPaymentIntentId, requestOptions);

			// get payment type and count from metadata
			String paymentTypeStr = paymentIntent.getMetadata().get("payment_type");
			String countStr = paymentIntent.getMetadata().get("count");

			if (paymentTypeStr == null && countStr == null) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing metadata fields.");
			}
			// parse to enum and int
			StripePaymentType paymentType = StripePaymentType.fromString(paymentTypeStr);
			int count = Integer.parseInt(countStr);

			switch (paymentIntent.getStatus()) {
			// NO ACTION
			case "succeeded":
				if (!payment.getPaymentStatus().equals(Payment.PaymentStatus.SUCCESS)) {
					paymentService.changePaymentStatusToSuccess(payment.getId());
					if (planDetail.getStatus() != SubPlanDetail.PlanDetailStatus.ACTIVE) {
						subPlanDetailService.activateSubscriptionPlan(planDetail.getId());
					}
				}
				// check if still pending then change to success
				return ResponseEntity.ok(Map.of("message", "Payment already succeeded"));

			// just for 3D
			case "requires_action":
				return ResponseEntity.ok(Map.of("message", "Additional authentication required"));

			// case failed, card in valid, amount insufficient
			case "failed":
			case "requires_payment_method":
				// handle 3 different payment types: subscription, xtra admin, xtra tenant
				PaymentIntentDTO newPaymentIntentDTO = createNewPaymentIntent(payment, principal, paymentType, count);
				;

				if (newPaymentIntentDTO == null) {
					return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to create new PaymentIntent.");
				}

				paymentService.changePaymentStatusToFailed(payment.getId(), newPaymentIntentDTO.getId());
				return ResponseEntity
						.ok(Map.of("message", "New PaymentIntent created", "newPaymentIntentDTO", newPaymentIntentDTO));

			// FRAUD
			case "canceled":
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(Map.of("error", "Payment cancelled, select plan and start again."));

			// LOADING
			default:
				return ResponseEntity.ok(Map.of("message", "Payment is still processing."));
			}

		} catch (StripeException e) {
			throw new RuntimeException("Stripe error: " + e.getMessage());
		}
	}

	@GetMapping("/get-payment-status")
	public ResponseEntity<Object> getPaymentStatus(@RequestParam String paymentIntentId, Principal principal) {
		try {
			PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
			return ResponseEntity.ok(Map.of("paymentStatus", paymentIntent.getStatus()));
		} catch (StripeException e) {
			throw new RuntimeException("Stripe error: " + e.getMessage());
		}

	}

	private PaymentIntentDTO createNewPaymentIntent(Payment payment, Principal principal, StripePaymentType paymentType,
			int count) throws StripeException {
		switch (paymentType) {
		case SUBSCRIPTION:
			return stripeService.createPaymentIntent(payment.getAmount(), principal.getName(),
					StripePaymentType.SUBSCRIPTION.name(), count);
		case MONTHLY_PAYMENT:
			return stripeService.createPaymentIntent(payment.getAmount(), principal.getName(),
					StripePaymentType.MONTHLY_PAYMENT.name(), count);
		case ADDITIONAL_ADMIN:
			return stripeService.createPaymentIntent(payment.getAmount(), principal.getName(),
					StripePaymentType.ADDITIONAL_ADMIN.name(), count);
		case ADDITIONAL_TENANT:
			return stripeService.createPaymentIntent(payment.getAmount(), principal.getName(),
					StripePaymentType.ADDITIONAL_TENANT.name(), count);
		default:
			return null;
		}
	}
}
