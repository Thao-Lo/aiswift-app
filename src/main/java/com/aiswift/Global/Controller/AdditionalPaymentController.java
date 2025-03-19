package com.aiswift.Global.Controller;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aiswift.Enum.AdditionalType;
import com.aiswift.Global.DTO.PaymentIntentDTO;
import com.aiswift.Global.DTO.PlanAdditionalPaymentRequest;
import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Entity.SubPlanDetail;
import com.aiswift.Global.Entity.SubscriptionPlan;
import com.aiswift.Global.Service.OwnerService;
import com.aiswift.Global.Service.PaymentService;
import com.aiswift.Global.Service.ProrataDetailService;
import com.aiswift.Global.Service.StripeService;
import com.aiswift.Global.Service.SubPlanDetailService;
import com.stripe.exception.StripeException;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


@RestController
@RequestMapping("/api/owner")
public class AdditionalPaymentController {
	@Autowired
	private SubPlanDetailService subPlanDetailService;

	@Autowired
	private OwnerService ownerService;

	@Autowired
	private StripeService stripeService;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private ProrataDetailService prorataDetailService;

	private final static Logger logger = LoggerFactory.getLogger(AdditionalPaymentController.class);	

	@PostMapping("/add-additional")
	public ResponseEntity<Object> createPlanPayment(
			@RequestParam @NotNull @Pattern(regexp = "ADMIN|TENANT") String additionalType,
			@RequestParam @Min(1) int count,
			Principal principal) {
		try {			
			System.out.println("hello additional");
			// from owner 
			Owner owner = ownerService.getOwnerWithSubPlanDetails(principal.getName());
			
			//get their last plan details
			SubPlanDetail planDetail = subPlanDetailService.getLatestPlanDetailByOwner(owner);

			// to get latest subscription-plan
			SubscriptionPlan plan = planDetail.getSubscriptionPlan();
			
			//prorata remaining days
			LocalDate now = LocalDate.now();
			Period period = Period.between(now, planDetail.getNextBillingDate().toLocalDate());
			int remainingDays = period.getDays();

			//method also check type: ADMIN or TENANT
			BigDecimal amount = calculateAmount(additionalType, count, remainingDays, plan);
			// create stripe paymentIntent
			PaymentIntentDTO paymentIntentDTO = createPaymentIntent(amount, principal.getName(), additionalType, count);			
			
			// Payment and ProrataDetail
			PlanAdditionalPaymentRequest request = new PlanAdditionalPaymentRequest();
			request.setOwner(owner);
			request.setPlan(plan);
			request.setPlanDetail(planDetail);
			request.setAdditionalType(additionalType);
			request.setCount(count);
			request.setRemainingDays(remainingDays);
			request.setAmount(amount);
			request.setPaymentIntentId(paymentIntentDTO.getId());
			
			paymentService.savePlanAdditionalPayment(request);
			
			logger.info("New Prorata PaymentIntent created");
			return new ResponseEntity<>(Map.of("PaymentIntentDTO", paymentIntentDTO), HttpStatus.OK);

		} catch (StripeException e) {
			return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);		
		}
	}
	
	//using extra fee, calculate with remaining days
	private BigDecimal calculateAmount(String additionalType, int count, int remainingDays, SubscriptionPlan plan) {
		   BigDecimal amount = BigDecimal.ZERO;

			if (AdditionalType.ADMIN.name().equals(additionalType)) {
				 amount = prorataDetailService.calculatePlanAdditionalfee(count, remainingDays,
						plan.getAdditionalAmindFee());
			}
			if (AdditionalType.TENANT.name().equals(additionalType)) {
				 amount = prorataDetailService.calculatePlanAdditionalfee(count, remainingDays,
						plan.getAdditionalTenantFee());
			}
			return amount;
		   
	}
	//create paymentIntent, send to Stripe, and will be stored in Payment
	private PaymentIntentDTO createPaymentIntent(BigDecimal amount,String email, String additionalType, int count) throws StripeException {
		   return stripeService.createPaymentIntent(amount, email, "ADDITIONAL_" + additionalType, count);		
	}	
}

