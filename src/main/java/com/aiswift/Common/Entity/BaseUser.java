package com.aiswift.Common.Entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="first_name",nullable = false, unique = false, length = 45)
	private String firstName;

	@Column(name="last_name",nullable = false, unique = false, length = 45)
	private String lastName;
	
	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(nullable = true, length = 255)
	private String password;	
	
	@Column(name = "created_by", nullable = false)
	private String createdBy;
	
	@Column(name = "updated_by")
	private String updatedBy;
	
	@Column(name = "created_at")
	@CreationTimestamp
	private LocalDateTime createdAt;
	
	@Column(name = "updated_at")
	@UpdateTimestamp
	private LocalDateTime updatedAt;
	
	@Column(name="reset_token")
	private String resetToken;
	
	@Column(name="reset_token_expiry")
	private LocalDateTime resetTokenExpiry;
	
}
