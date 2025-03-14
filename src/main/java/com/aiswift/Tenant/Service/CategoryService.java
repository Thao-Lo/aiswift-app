package com.aiswift.Tenant.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Tenant.Entity.Category;
import com.aiswift.Tenant.Repository.CategoryRepository;
import com.aiswift.Tenant.Repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;


@Conditional(TenantDatabaseCondition.class)  // Only create for tenant databases
@Service
public class CategoryService {
	
	@Autowired
	CategoryRepository categoryRepo;
	
	@Autowired
	ProductRepository productRepo;

	public List<Category> getAllCategory(){
		return categoryRepo.findAll();
	}
	
	public List<Category> getTopLevelCategory(){
		return categoryRepo.findByParentIsNull();
	}
	
	public Optional<Category> getCategoryById(Long categoryId){
		return categoryRepo.findById(categoryId);
	}
	
	public Category addCategory(String name, Long parentId) {
   	 // Check if a category with the same name already exists (case-insensitive)
       if (categoryRepo.existsByNameIgnoreCase(name)) {
           throw new IllegalArgumentException("A category with this name already exists");
       }
       Category category = new Category();
       category.setName(name);

       if (parentId != null) {
           Category parent = getCategoryById(parentId)
               .orElseThrow(() -> new EntityNotFoundException("Parent category not found"));
           category.setParent(parent);
           category.setLevel(parent.getLevel() + 1);
       } else {
           category.setLevel(0);
       }

       return categoryRepo.save(category);
   }
	
	
	public void updateCategory(Category category) {
		 categoryRepo.save(category);
		
	}
	
	@Transactional
	public void deleteCategory(Long id) {
		Category category = getCategoryById(id).orElseThrow(() -> new EntityNotFoundException("Category not found"));
		if(categoryHasProducts(category)) {
			throw new IllegalStateException("Cannot delete category.It or its subcategories have aossicated products");
		}
		
		deleteCategoryRecursively(category);
	
	}

	private boolean categoryHasProducts(Category category) {
		//check if the current category has products
		if(!productRepo.findProductByCategoryId(category.getId()).isEmpty()) {
			return true;
		}
		
		//recursively check subCategories
		for (Category subCategory : category.getSubcategories()) {
			if(categoryHasProducts(subCategory)) {
				return true;
			}
		}
		return false;
	}

	private void deleteCategoryRecursively(Category category) {
		// recursively delete all subCategories
		for(Category subCategory : new ArrayList<>(category.getSubcategories())) { // this is to avoid iterating over a collection and modifying it at the same time -> ConcurrentModificationException
			deleteCategoryRecursively(subCategory);
		}
		//Finally delete the category itself
		categoryRepo.delete(category);
		
	}
}
