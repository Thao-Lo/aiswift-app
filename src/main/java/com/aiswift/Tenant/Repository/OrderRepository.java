package com.aiswift.Tenant.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aiswift.Tenant.Entity.Order;

public interface OrderRepository extends JpaRepository<Order,Long> {


	List<Order> findByUserId(Long id);

}
