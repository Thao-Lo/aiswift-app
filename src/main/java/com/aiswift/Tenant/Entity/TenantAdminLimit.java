package com.aiswift.Tenant.Entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor	
@AllArgsConstructor
@Entity
@Table(name = "tenant_admin_limit")
public class TenantAdminLimit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@Column(name = "id")	
	private int id;
	
	@Column(name = "database_name", nullable = false, length = 100)
	private String databaseName;

	@Column(name = "current_admin_count", nullable = false, length = 100)
	private String currentAdminCount;

	@Column(name = "max_admin_count", nullable = false, length = 100)
	private String maxAdminCount;

	@Column(name = "updated_by")
	private String updatedBy;

	@Column(name = "created_at")
	@CreationTimestamp
	private LocalDateTime createdAt;
}
