package com.aiswift.Global.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aiswift.Global.Entity.Tenant;


@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
	Tenant findByName(String name);
	List<Tenant> findByOwnerId(Long ownerId);
	Tenant findByDbName(String dbName);
}
