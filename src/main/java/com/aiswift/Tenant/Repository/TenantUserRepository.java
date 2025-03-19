package com.aiswift.Tenant.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aiswift.Tenant.Entity.TenantUser;



@Repository
public interface TenantUserRepository extends JpaRepository<TenantUser, Long>{
	
	@Query("SELECT u FROM TenantUser u JOIN FETCH u.role r JOIN FETCH r.permissions WHERE u.email = :email")
	TenantUser findByEmailWithPermissions(@Param("email") String email);
	
	@Query("SELECT u FROM TenantUser u JOIN FETCH u.role r WHERE u.email = :email")
	Optional<TenantUser> findByEmail(@Param("email") String email);
	
	boolean existsByEmail(String email);
	
	Optional<TenantUser> findByResetToken(String token);
}
