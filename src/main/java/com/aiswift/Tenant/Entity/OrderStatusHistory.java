package com.aiswift.Tenant.Entity;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Conditional;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Enum.OrderStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;

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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_status_history")
@Conditional(TenantDatabaseCondition.class)  // Only create for tenant databases
public class OrderStatusHistory {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id")
    @JsonBackReference
    private User updatedBy;
}

