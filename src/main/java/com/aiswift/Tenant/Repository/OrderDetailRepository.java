package com.aiswift.Tenant.Repository;

import java.util.List;

import org.springframework.context.annotation.Conditional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Tenant.Entity.OrderDetail;
import com.aiswift.Tenant.Entity.OrderDetailKey;

@Conditional(TenantDatabaseCondition.class)  // Only create for tenant databases
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailKey> {

	List<OrderDetail> findBySizeId(Long id);

	List<OrderDetail> findByProductId(Long id);

}
