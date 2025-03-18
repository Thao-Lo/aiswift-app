package com.aiswift.Tenant.Entity;

import java.util.ArrayList;
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
@Table(name = "categories")
@Conditional(TenantDatabaseCondition.class)  // Only create for tenant databases
public class Category {
	
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

private String name;

@OneToMany(mappedBy = "category", fetch = FetchType.EAGER)
@JsonManagedReference
private List<Product>products;

@ManyToOne
@JsonBackReference
@JoinColumn(name = "parent_id")
private Category parent;

@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
@JsonManagedReference
private List<Category> subcategories = new ArrayList<>();

@Column(nullable = false)
private int level;



}