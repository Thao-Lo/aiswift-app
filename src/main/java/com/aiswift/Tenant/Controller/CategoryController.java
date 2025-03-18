package com.aiswift.Tenant.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Tenant.Entity.Category;
import com.aiswift.Tenant.Service.CategoryService;

@Conditional(TenantDatabaseCondition.class) // Only create for tenant databases
@RestController
@RequestMapping("/api/tenant/category")
public class CategoryController {

	@Autowired
	CategoryService categoryService;

	@GetMapping
	public ResponseEntity<List<Category>> getAllCategories() {
		List<Category> categories = categoryService.getAllCategory();
		return ResponseEntity.ok(categories);
	}

	@GetMapping("/top-level")
	public ResponseEntity<List<Category>> getTopLevelCategories() {
		List<Category> topLevelCategories = categoryService.getTopLevelCategory();
		return ResponseEntity.ok(topLevelCategories);
	}

	@PostMapping
	public ResponseEntity<Category> addCategory(@RequestParam(required = true, name = "name") String name,
			@RequestParam(required = false, name = "parentId") Long parentId) {
		System.out.println("I am category controller");
		Category category = categoryService.addCategory(name, parentId);
		return ResponseEntity.ok(category);

	}
	
	@PutMapping("/{categoryId}")
	public ResponseEntity<Category> editCategory(@PathVariable Long categoryId,
            @RequestParam(required = true, name = "name") String categoryName,
            @RequestParam(required = false, name = "parentId") Long parentId){
		Category updatedCategory = categoryService.updateCategory(categoryId,categoryName,parentId);
		return ResponseEntity.ok(updatedCategory);
	}
	
	@DeleteMapping("/{categoryId}")
	public ResponseEntity<Void> safeDeleteCategory(@PathVariable Long categoryId){
		   categoryService.deleteCategory(categoryId);
		   return ResponseEntity.noContent().build();
	}
}
