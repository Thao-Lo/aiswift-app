package com.aiswift.Tenant.Service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Tenant.Entity.Category;
import com.aiswift.Tenant.Repository.CategoryRepository;
import com.aiswift.Tenant.Repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Conditional(TenantDatabaseCondition.class) // Only create for tenant databases
@Service
public class CategoryService {

	@Autowired
	CategoryRepository categoryRepo;

	@Autowired
	ProductRepository productRepo;

	public List<Category> getAllCategory() {
		List<Category> categories = categoryRepo.findAll();

		if (categories.isEmpty()) {
			throw new EntityNotFoundException("No categories found");
		}

		return categories;
	}

	public List<Category> getTopLevelCategory() {
		List<Category> topLevelCategories = categoryRepo.findByParentIsNull();

		if (topLevelCategories.isEmpty()) {
			throw new EntityNotFoundException("No top-level categories found");
		}

		return topLevelCategories;
	}

	public Category getCategoryById(Long categoryId) {
		return categoryRepo.findById(categoryId)
				.orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
	}

	public Category addCategory(String name, Long parentId) {
		// Check if a category with the same name already exists (case-insensitive)
		if (categoryRepo.existsByNameIgnoreCase(name)) {
			throw new IllegalArgumentException("A category with this name already exists");
		}
		Category category = new Category();
		category.setName(name);

		if (parentId != null) {
			Category parent = getCategoryById(parentId);
			category.setParent(parent);
			category.setLevel(parent.getLevel() + 1);
		} else {
			category.setLevel(0);
		}

		return categoryRepo.save(category);
	}

	public Category updateCategory(Long categoryId, String categoryName, Long parentId) {

		// Find existing category
		Category existingCategory = categoryRepo.findById(categoryId)
				.orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));

		// Update category name if provided
		if (categoryName != null && !categoryName.trim().isEmpty()) {
			existingCategory.setName(categoryName);
		}

		// Update parent category if parentId is provided
		if (parentId != null) {
			Category parentCategory = categoryRepo.findById(parentId)
					.orElseThrow(() -> new EntityNotFoundException("Parent category not found with id: " + parentId));
			existingCategory.setParent(parentCategory);
		}

		// Save and return updated category
		return categoryRepo.save(existingCategory);

	}

	@Transactional
	public void deleteCategory(Long id) {
		Category category = getCategoryById(id);
		if (categoryHasProducts(category)) {
			throw new IllegalStateException("Cannot delete category.It or its subcategories have aossicated products");
		}

		deleteCategoryRecursively(category);

	}

	private boolean categoryHasProducts(Category category) {
		// check if the current category has products
		if (!productRepo.findProductByCategoryId(category.getId()).isEmpty()) {
			return true;
		}

		// recursively check subCategories
		for (Category subCategory : category.getSubcategories()) {
			if (categoryHasProducts(subCategory)) {
				return true;
			}
		}
		return false;
	}

	private void deleteCategoryRecursively(Category category) {
		/* recursively delete all subCategories
		 * new copy of subcategories list is to avoid iterating over a collection and modifying it at the same time or ConcurrentModificationException																		
		 */
		for (Category subCategory : new ArrayList<>(category.getSubcategories())) { 
			deleteCategoryRecursively(subCategory);
		}
		// Finally delete the category itself
		categoryRepo.delete(category);

	}
}
