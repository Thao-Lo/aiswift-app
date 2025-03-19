package com.aiswift.Tenant.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.context.annotation.Conditional;

import com.aiswift.Config.TenantDatabaseCondition;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="order_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(exclude= {"product","order","size"})
@Conditional(TenantDatabaseCondition.class)  // Only create for tenant databases
public class OrderDetail  {

@EmbeddedId
OrderDetailKey id;

@ManyToOne
@JsonBackReference(value = "product-details")
@MapsId("productId") // attribute of OrderDetailKey
@JoinColumn(name="product_id")
private Product product;

@ManyToOne
@JsonBackReference(value = "order-details")
@MapsId("orderId") // attribute of OrderDetailKey
@JoinColumn(name="order_id")
private Order order;

private int quantity;

private BigDecimal price;

private BigDecimal subtotal;

@ManyToOne
@JoinColumn(name="size_id")
@JsonBackReference(value = "size-details")
private Size size;

@Column(name="created_by")	
private String createdBy; //userId

@Column(name="created_at")
private LocalDateTime createdAt;

}
