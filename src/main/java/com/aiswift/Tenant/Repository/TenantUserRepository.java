package com.aiswift.Tenant.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aiswift.Tenant.Entity.TenantUser;


@Repository
public interface TenantUserRepository extends JpaRepository<TenantUser, Long>{
	Optional<TenantUser> findByEmail(String email);

//	TenantUser findByEmailWithPermissions(String email);
}
