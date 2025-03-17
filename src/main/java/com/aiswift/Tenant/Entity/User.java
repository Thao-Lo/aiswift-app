package com.aiswift.Tenant.Entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;

import com.aiswift.Enum.Status;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name="first_name",nullable = false, unique = true, length = 45)
	private String firstName;

	@Column(name="last_name",nullable = false, unique = true, length = 45)
	private String lastName;

	@Column(nullable = false, unique = true, length = 100)
	private String email;
	
	@Column(nullable = false, length = 255)
	private String password;
	
	@Enumerated(EnumType.STRING)
	@Column
	private Status status;
	
	@Column(name = "created_at")
	@CreationTimestamp
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	@UpdateTimestamp
	private LocalDateTime updatedAt;
    // Common user methods
	
	@OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<Order> orders = new ArrayList<>();
}
