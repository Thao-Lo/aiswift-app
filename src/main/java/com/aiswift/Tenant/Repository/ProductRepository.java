package com.aiswift.Tenant.Repository;

import java.util.List;

import org.springframework.context.annotation.Conditional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Tenant.Entity.Product;


@Conditional(TenantDatabaseCondition.class)  // Only create for tenant databases
public interface ProductRepository extends JpaRepository<Product,Long>{

	List<Product> findProductByCategoryId(Long categoryId);

}
