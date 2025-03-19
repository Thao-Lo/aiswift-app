package com.aiswift.Global.Entity;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tenant_activity_log", schema = "global_multi_tenant")
public class TenantActivityLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="old_value")
	private String oldValue;
	
	@Column(name="new_value")
	private String newValue;
	
	@JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
	private Map<String, Object> detail = new HashMap<>();
	//(Map.of("message" , String.format("Allocate additional Admin: %d", count)))
	
	@Column(name="created_at", nullable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;
		
	@Column(name="tenant_id")
	private long tenantId;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name="owner_id", nullable = false)
	private Owner owner;
	
	/*REMOVE RELATIONSHIP, JUST STORE ID*/
	@Column(name="action_type_id", nullable = false )
	private int actionTypeId;

}
