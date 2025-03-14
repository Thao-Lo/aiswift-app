package com.aiswift.Tenant.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.context.annotation.Conditional;

import com.aiswift.Config.TenantDatabaseCondition;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data 
@AllArgsConstructor
@NoArgsConstructor
@Entity // for mapping to db 
@ToString(exclude= {"category","orderDetails","sizes"})
@Table(name="products")
@Conditional(TenantDatabaseCondition.class)  // Only create for tenant databases
public class Product {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(name="image_name")
private String imageName;

@Lob
@Column(name="image_data",columnDefinition="LONGBLOB")
private byte[] imageData; // this one can consider to store in separate table? and manage with amazon S3?

@Transient
public String getBase64Image() {
	if(imageData != null && imageData.length > 0) {
		return Base64.getEncoder().encodeToString(imageData);
	}
	return null;
}

@Column(name="created_by")	
private String createdBy;

@Column(name="created_at")
private LocalDateTime createdAt;

@Column(name="Updated_by")
private String UpdatedBy;

@Column(name="Updated_at")
private LocalDateTime updatedAt;



@ManyToOne
@JsonBackReference
@JoinColumn(name = "category_id")
private Category category;

private String name;

private BigDecimal price;

@Column(columnDefinition = "TEXT")
private String description;

@OneToMany(mappedBy = "product") // name of object product in OrderDetail
@JsonManagedReference
private List<OrderDetail>orderDetails;

@OneToMany(mappedBy = "product", cascade = CascadeType.ALL,fetch = FetchType.EAGER) // make sure when I call product.getSize() all related sizes is loaded from database
@JsonManagedReference
private List<Size> sizes = new ArrayList<>();

@PrePersist
protected void onCreate() {
	createdAt = LocalDateTime.now();
}

@PreUpdate 
protected void onUpdate() {
	updatedAt = LocalDateTime.now();
}

}

