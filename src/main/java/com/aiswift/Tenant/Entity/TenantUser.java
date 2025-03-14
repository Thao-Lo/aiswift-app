package com.aiswift.Tenant.Entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.aiswift.Enum.Status;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "users")
public class TenantUser {

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

	@ManyToOne
	@JoinColumn(name = "role_id") //have column role_id in users
	private Role role;
	
	@Enumerated(EnumType.STRING)
	@Column
	private Status status;

	@Column(name = "created_at")
	@CreationTimestamp
	private LocalDateTime createdAt;

	
}
