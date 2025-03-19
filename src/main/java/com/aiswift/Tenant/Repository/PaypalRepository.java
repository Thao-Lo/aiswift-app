package com.aiswift.Tenant.Repository;

import java.util.List;

import org.springframework.context.annotation.Conditional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Tenant.Entity.PayPal;

@Conditional(TenantDatabaseCondition.class)  // Only create for tenant databases
@Repository
public interface PaypalRepository extends JpaRepository<PayPal,Long> {

	List<PayPal> findByTenantId(Long tenantId);

	PayPal findByPaypalEmail(String email);

}
