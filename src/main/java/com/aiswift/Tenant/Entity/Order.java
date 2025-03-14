package com.aiswift.Tenant.Entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Conditional;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Enum.OrderStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "orders")
@Conditional(TenantDatabaseCondition.class)  // Only create for tenant databases
public class Order implements Serializable {

	private static final long serialVersionUID = 1L;

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
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(name = "total_price", precision = 10, scale = 2)
	private BigDecimal totalPrice;

	@OneToMany(mappedBy = "order") // name of object Order in OrderDetail
	@JsonManagedReference
	private Set<OrderDetail> orderDetails;

	@ManyToOne(fetch = FetchType.LAZY) // default type is eager
	@JoinColumn(name = "user_id")
	@JsonBackReference
	private TenantUser user;
	
	@Column(columnDefinition = "TEXT")
	private String customerInfo;
	
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderStatusHistory> statusHistory = new ArrayList<>();
	
	public int getItemCount() {
		return this.orderDetails.size();
	}

}
