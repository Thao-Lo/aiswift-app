package com.aiswift.Global.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.aiswift.Global.Entity.Payment;
import com.aiswift.Global.Entity.ProrataDetail;

@Service
public class ProrataDetailService {

	//Calculate the prorata amount for a single transaction
	public BigDecimal calculatePlanAdditionalfee(int count, int remainingDays, BigDecimal cost) {
		//using divide + RoundingMode... to avoid 3.33333
		BigDecimal costPerAdditional = cost.multiply(BigDecimal.valueOf(remainingDays))
				.divide(BigDecimal.valueOf(30),2, RoundingMode.HALF_UP);		
		
		return costPerAdditional.multiply(BigDecimal.valueOf(count));
	}
	
	//create new prorata object
	public ProrataDetail createProrataDetail(Payment payment, int paymentTypeId, BigDecimal amount, int quantity, BigDecimal unitPrice, int remainingDays) {
		ProrataDetail prorataDetail = new ProrataDetail();
		prorataDetail.setPayment(payment);
		prorataDetail.setPaymentTypeId(paymentTypeId);
		prorataDetail.setAmount(amount);
		prorataDetail.setQuantity(quantity);
		prorataDetail.setUnitPrice(unitPrice);
		prorataDetail.setDaysRemaning(remainingDays);
	    
	    return prorataDetail;
	}
	
}

