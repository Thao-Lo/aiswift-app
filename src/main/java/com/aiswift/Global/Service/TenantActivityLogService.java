package com.aiswift.Global.Service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aiswift.Global.DTO.TenantLogDTO;
import com.aiswift.Global.Entity.TenantActivityLog;
import com.aiswift.Global.Repository.TenantActivityLogRepository;

@Service
public class TenantActivityLogService {
	@Autowired
	private TenantActivityLogRepository tenantActivityLogRepository;
	
	public void createTenantActivityLog(TenantLogDTO tenantLogDTO) {
		TenantActivityLog tenantActivityLog = new TenantActivityLog();
		tenantActivityLog.setOwner(tenantLogDTO.getOwner());
		tenantActivityLog.setTenantId(tenantLogDTO.getTenantId());		
		tenantActivityLog.setActionTypeId(tenantLogDTO.getActionTypeId());
		tenantActivityLog.setOldValue(tenantLogDTO.getOldValue());
		tenantActivityLog.setNewValue(tenantLogDTO.getNewValue());
		tenantActivityLog.setDetail(Map.of("message", tenantLogDTO.getMessage()));
		tenantActivityLogRepository.save(tenantActivityLog);
	}
	
}
