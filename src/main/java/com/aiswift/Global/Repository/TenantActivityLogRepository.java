package com.aiswift.Global.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aiswift.Global.Entity.TenantActivityLog;

public interface TenantActivityLogRepository extends JpaRepository<TenantActivityLog, Long>{
	
	List<TenantActivityLog> findByOwnerId(long ownerId);
	
}
