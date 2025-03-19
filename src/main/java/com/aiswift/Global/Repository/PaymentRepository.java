package com.aiswift.Global.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aiswift.Global.Entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
	Optional<Payment> findByPaymentIntentId(String id);
}
