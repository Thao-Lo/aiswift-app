package com.aiswift.Global.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiswift.Enum.AdditionalType;
import com.aiswift.Exception.NoDataFoundException;
import com.aiswift.Global.DTO.PlanAdditionalPaymentRequest;
import com.aiswift.Global.DTO.SubPlanRequest;
import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Entity.Payment;
import com.aiswift.Global.Entity.PaymentDetail;
import com.aiswift.Global.Entity.SubPlanDetail;
import com.aiswift.Global.Entity.SubscriptionPlan;
import com.aiswift.Global.Repository.PaymentDetailRepository;
import com.aiswift.Global.Repository.PaymentRepository;
import com.aiswift.Global.Repository.ProrataDetailRepository;



@Service
public class PaymentService {

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private PaymentDetailService paymentDetailService;

	@Autowired
	private PaymentDetailRepository paymentDetailRepository;

	@Autowired
	private ProrataDetailService prorataDetailService;
	
	@Autowired
	private ProrataDetailRepository prorataDetailRepository;

	public Payment getPaymentById(long id) {
		return paymentRepository.findById(id)
				.orElseThrow(() -> new NoDataFoundException("No payment found for this id: " + id));

	}

	public Payment getPaymentByPaymentIntentId(String id) {
		return paymentRepository.findByPaymentIntentId(id)
				.orElseThrow(() -> new NoDataFoundException("No payment found for this id: " + id));

	}
	// save 1st subscription
	@Transactional(transactionManager = "globalTransactionManager")
	public void saveInitPayment(Owner owner, SubPlanDetail planDetail, BigDecimal amount, String paymentIntentId,
			SubPlanRequest request, SubscriptionPlan plan) {
		// save to payment table
		Payment payment = new Payment();
		payment.setOwner(owner);
		payment.setSubPlanDetail(planDetail);
		payment.setAmount(amount);
		payment.setPaymentIntentId(paymentIntentId);
		payment.setPaymentStatus(Payment.PaymentStatus.PENDING);

		payment = paymentRepository.save(payment);

		List<PaymentDetail> paymentDetails = new ArrayList<>();
		// each payment break down to different type
		paymentDetails
				.add(paymentDetailService.createPaymentDetail(payment, 1, plan.getBaseCost(), 1, plan.getBaseCost()));

		if (request.getAdditionalAdminCount() > 0) {
			BigDecimal additionalAdminAmount = plan.getAdditionalAmindFee()
					.multiply(BigDecimal.valueOf(request.getAdditionalAdminCount()));

			paymentDetails.add(paymentDetailService.createPaymentDetail(payment, 2, additionalAdminAmount,
					request.getAdditionalAdminCount(), plan.getAdditionalAmindFee()));

		}
		if (request.getAdditionalTenantCount() > 0) {
			BigDecimal additionalTenantAmount = plan.getAdditionalTenantFee()
					.multiply(BigDecimal.valueOf(request.getAdditionalTenantCount()));

			paymentDetails.add(paymentDetailService.createPaymentDetail(payment, 3, additionalTenantAmount,
					request.getAdditionalTenantCount(), plan.getAdditionalTenantFee()));
		}
		if (paymentDetails.isEmpty()) {
			throw new IllegalStateException("No payment details found to save.");
		}
		paymentDetailRepository.saveAll(paymentDetails);
	}

	
	// save extra
	@Transactional(transactionManager = "globalTransactionManager")
	public void savePlanAdditionalPayment(PlanAdditionalPaymentRequest request) {
		// save to payment table
		Payment payment = new Payment();
		payment.setOwner(request.getOwner());
		payment.setSubPlanDetail(request.getPlanDetail());
		payment.setAmount(request.getAmount());
		payment.setPaymentIntentId(request.getPaymentIntentId());
		payment.setPaymentStatus(Payment.PaymentStatus.PENDING);

		payment = paymentRepository.save(payment);

		if (AdditionalType.ADMIN.name().equals(request.getAdditionalType())) {
			prorataDetailRepository.save(
					prorataDetailService.createProrataDetail(
							payment, 2, request.getAmount(), request.getCount(), 
							request.getPlan().getAdditionalAmindFee(), request.getRemainingDays()));
			
		}
		if (AdditionalType.TENANT.name().equals(request.getAdditionalType())) {
			prorataDetailRepository.save(
					prorataDetailService.createProrataDetail(
							payment, 3, request.getAmount(), request.getCount(), 
							request.getPlan().getAdditionalTenantFee(), request.getRemainingDays()));
		}		

	}
	
		// save 1st subscription
		@Transactional(transactionManager = "globalTransactionManager")
		public void saveMonthlyPayment(Owner owner, SubPlanDetail planDetail, BigDecimal amount, String paymentIntentId,
				SubscriptionPlan plan) {
			// save to payment table
			Payment payment = new Payment();
			payment.setOwner(owner);
			payment.setSubPlanDetail(planDetail);
			payment.setAmount(amount);
			payment.setPaymentIntentId(paymentIntentId);
			payment.setPaymentStatus(Payment.PaymentStatus.PENDING);

			payment = paymentRepository.save(payment);

			List<PaymentDetail> paymentDetails = new ArrayList<>();
			// each payment break down to different type
			paymentDetails
					.add(paymentDetailService.createPaymentDetail(payment, 1, plan.getBaseCost(), 1, plan.getBaseCost()));
			
			int additionalAdminCount = planDetail.getAdditionalAdminCount();
			int additionalTenantCount = planDetail.getAdditionalTenantCount();
			
			if (additionalAdminCount > 0) {
				BigDecimal additionalAdminFee = plan.getAdditionalAmindFee()
						.multiply(BigDecimal.valueOf(additionalAdminCount));

				paymentDetails.add(paymentDetailService.createPaymentDetail(payment, 2, additionalAdminFee,
						additionalAdminCount, plan.getAdditionalAmindFee()));

			}
			if (additionalTenantCount > 0) {
				BigDecimal additionalTenantFee = plan.getAdditionalTenantFee()
						.multiply(BigDecimal.valueOf(additionalTenantCount));

				paymentDetails.add(paymentDetailService.createPaymentDetail(payment, 3, additionalTenantFee,
						additionalTenantCount, plan.getAdditionalTenantFee()));
			}
			if (paymentDetails.isEmpty()) {
				throw new IllegalStateException("No payment details found to save.");
			}
			paymentDetailRepository.saveAll(paymentDetails);
		}
	
	//change status
	@Transactional(transactionManager = "globalTransactionManager")
	public void changePaymentStatusToSuccess(long paymentId) {
		Payment payment = getPaymentById(paymentId);

		payment.setPaymentStatus(Payment.PaymentStatus.SUCCESS);

		paymentRepository.save(payment);
	}

	// for payment-retry
	@Transactional(transactionManager = "globalTransactionManager")
	public void changePaymentStatusToFailed(long paymentId, String newPaymentIntentId) {
		Payment payment = getPaymentById(paymentId);

		payment.setPaymentIntentId(newPaymentIntentId);
		payment.setPaymentStatus(Payment.PaymentStatus.FAILED);

		paymentRepository.save(payment);
	}
}
