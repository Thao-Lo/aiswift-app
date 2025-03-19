package com.aiswift.Global.Service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aiswift.Global.Entity.Payment;
import com.aiswift.Global.Entity.PaymentDetail;
import com.aiswift.Global.Entity.SubscriptionPlan;
import com.aiswift.Global.Repository.PaymentDetailRepository;

@Service
public class PaymentDetailService {
	@Autowired
	private PaymentDetailRepository paymentDetailRepository;
	
	public PaymentDetail createPaymentDetail(Payment payment, int paymentTypeId, BigDecimal amount, int quantity, BigDecimal unitPrice) {
	    PaymentDetail paymentDetail = new PaymentDetail();
	    paymentDetail.setPayment(payment);
	    paymentDetail.setPaymentTypeId(paymentTypeId);
	    paymentDetail.setAmount(amount);
	    paymentDetail.setQuantity(quantity);
	    paymentDetail.setUnitPrice(unitPrice);
	    return paymentDetail;
	}
	
	//base plan 	
	public void saveBasePlanPaymentDetail(Payment payment, SubscriptionPlan plan) {
		 PaymentDetail  paymentDetail = new  PaymentDetail();
		 
		 paymentDetail.setPayment(payment);
		 paymentDetail.setPaymentTypeId(1);
		 paymentDetail.setAmount(plan.getBaseCost());
		 paymentDetail.setQuantity(1);
		 paymentDetail.setUnitPrice(plan.getBaseCost());
		 
		 paymentDetailRepository.save(paymentDetail);
	}
	
	//extra admin 	
	public void saveAdditionalAdminPaymentDetail(Payment payment, SubscriptionPlan subPlan, int additionalAdmin) {
		 PaymentDetail  paymentDetail = new  PaymentDetail();
		 BigDecimal totalAmount = subPlan.getAdditionalAmindFee().multiply(BigDecimal.valueOf(additionalAdmin));
		 
		 paymentDetail.setPayment(payment);
		 paymentDetail.setPaymentTypeId(2);
		 paymentDetail.setAmount(totalAmount);
		 paymentDetail.setQuantity(additionalAdmin);
		 paymentDetail.setUnitPrice(subPlan.getAdditionalAmindFee());
		 
		 paymentDetailRepository.save(paymentDetail);
	}
	
	//extra tenant 	
	public void saveAdditionalTenantPaymentDetail(Payment payment, SubscriptionPlan subPlan, int additionalTenant) {
		 PaymentDetail  paymentDetail = new  PaymentDetail();
		 BigDecimal totalAmount = subPlan.getAdditionalTenantFee().multiply(BigDecimal.valueOf(additionalTenant));
		 
		 paymentDetail.setPayment(payment);
		 paymentDetail.setPaymentTypeId(3);
		 paymentDetail.setAmount(totalAmount);
		 paymentDetail.setQuantity(additionalTenant);
		 paymentDetail.setUnitPrice(subPlan.getAdditionalTenantFee());
		 
		 paymentDetailRepository.save(paymentDetail);
	}
}