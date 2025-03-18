package com.aiswift.Tenant.Entity;
import org.springframework.context.annotation.Conditional;
import com.aiswift.Config.TenantDatabaseCondition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(OrderEntityListener.class)
@Conditional(TenantDatabaseCondition.class) // Only create for tenant databases
@Entity
@Table(name = "paypal_configs")
public class PayPal {
    @Id
    private String id;
    
    @Column(name = "tenant_id")
    private Long tenantId;
    
    @Column(name = "client_id")
    private String clientId;
    
    @Column(name = "client_secret")
    private String clientSecret;
    
    @Column(name = "paypal_email")
    private String paypalEmail;
    
    @Column(name = "webhook_id")
    private String webhookId;
    
    @Column(name = "mode")
    private String mode; // sandbox or live

}

