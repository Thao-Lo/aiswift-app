package com.aiswift.Tenant.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import com.aiswift.Global.Entity.Owner;
import org.springframework.context.annotation.Conditional;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Enum.OrderStatus;
import com.aiswift.Enum.RefundStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@EntityListeners(OrderEntityListener.class)
@Entity
@Table(name = "orders")
@Conditional(TenantDatabaseCondition.class) // Only create for tenant databases
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING) // if not specify, default will be ordinal 0,1,2
	private OrderStatus status;

	private String paypalId;

	private Long tenantId;

	private LocalDateTime date;// created_at date

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "total_price", precision = 10, scale = 2)
	private BigDecimal totalPrice;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true // Remove order details no longer
																					// associated
	)
	@JsonManagedReference
	private Set<OrderDetail> orderDetails;

	@ManyToOne(fetch = FetchType.LAZY) // default type is eager
	@JoinColumn(name = "user_id")
	@JsonBackReference
	@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "userType")
	@JsonSubTypes({ @JsonSubTypes.Type(value = TenantUser.class, name = "TENANT_USER"),
			@JsonSubTypes.Type(value = Owner.class, name = "OWNER") })
	private User user;

	@Column(columnDefinition = "TEXT")
	private String customerInfo;

	@Column(name = "order_status_history", columnDefinition = "TEXT")
    private String compressedStatusHistory;

	@Enumerated(EnumType.STRING)
	private RefundStatus refundStatus;

	@Column(name = "refund_requested_at")
	private LocalDateTime refundRequestedAt;
	
	@Column(name = "refund_processed_at")
	private LocalDateTime refundProcessedAt;

	public int getItemCount() {
		return this.orderDetails.size();
	}

}
