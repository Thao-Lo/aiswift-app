package com.aiswift.Global.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment", schema = "global_multi_tenant")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	private BigDecimal amount;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private PaymentStatus paymentStatus;
	
	@Column(name = "payment_intent_id", nullable = false)
	private String paymentIntentId;
	
	@Column(name = "created_at", nullable = false)
	@CreationTimestamp
	private LocalDateTime createdAt;
	
	
	@Column(name = "updated_at")
	@UpdateTimestamp
	private LocalDateTime updatedAt;
	
	@JsonIgnore
	@JoinColumn(name = "owner_id",  nullable = false)
	@ManyToOne
	private Owner owner;
	
	@JsonIgnore
	@JoinColumn(name = "subscription_plan_detail_id",  nullable = false)
	@ManyToOne
	private SubPlanDetail subPlanDetail;
	
	@JsonIgnore
	@OneToMany(mappedBy = "payment") //on delete restrict
	private List<PaymentDetail> paymentDetails = new ArrayList<>();
	
	@JsonIgnore
	@OneToMany(mappedBy = "payment") //on delete restrict
	private List<ProrataDetail> prorataDetail = new ArrayList<>();
	
	public enum PaymentStatus{
		PENDING, SUCCESS, FAILED
	}
}
