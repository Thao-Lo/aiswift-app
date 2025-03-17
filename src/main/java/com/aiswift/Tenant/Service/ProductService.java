package com.aiswift.Tenant.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Exception.FileProcessingException;
import com.aiswift.Tenant.Entity.Category;
import com.aiswift.Tenant.Entity.Product;
import com.aiswift.Tenant.Entity.Size;
import com.aiswift.Tenant.Repository.CategoryRepository;
import com.aiswift.Tenant.Repository.OrderDetailRepository;
import com.aiswift.Tenant.Repository.ProductRepository;
import com.aiswift.Tenant.Repository.SizeRepository;
import com.aiswift.dto.Tenant.ProductDto;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Conditional(TenantDatabaseCondition.class) // Only create for tenant databases
@Service
public class ProductService {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private SizeRepository sizeRepository;

	@Autowired
	private OrderDetailRepository orderDetailRepository;

	public Product getProductById(Long id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
	}

	public List<Product> findAllProducts() {
		return productRepository.findAll();
	}

	public List<Product> findProductByCategoryId(Long categoryId) {

		return productRepository.findProductByCategoryId(categoryId);
	}

	public Product addNewProduct(String name, BigDecimal price, Long category_id, MultipartFile file) {
		Product savedProduct = null;

		try {

			Product product = new Product();
			product.setName(name);
			product.setPrice(price);
			product.setImageData(file.getBytes());
			product.setImageName(file.getOriginalFilename());
			product.setCreatedBy("Owner");
			Category category = categoryRepository.findById(category_id)
					.orElseThrow(() -> new IllegalArgumentException("Invalid category Id"));
			product.setCategory(category);
			savedProduct = productRepository.save(product);
			log.info("NEW PRODUCT SAVED TO DATABASE: " + product.getName());
		} catch (IOException e) {
			e.printStackTrace();
			log.info("UNABLE TO SAVE PRODUCT TO DATABASE");
			log.error("Error processing image file", e);
			throw new FileProcessingException("Unable to process image file", e);
		}
		return savedProduct;

	}

	@Transactional
	public void updatePrices(List<Product> products) {
		// update prices in batch
		for (Product product : products) {
			Optional<Product> productOptional = productRepository.findById(product.getId());
			if (productOptional.isPresent()) {
				Product productDB = productOptional.get();
				productDB.setPrice(product.getPrice());
				productRepository.save(productDB); // save will update the existing record if ID is not null
			}
		}
	}

	@Transactional
	public Product updateProduct(ProductDto productDto, Long productId){
		try {
			// find product by ID from database
			Product productDB = productRepository.findById(productId)
					.orElseThrow(() -> new EntityNotFoundException("Product not found"));

			if (productDto.getName() != null) {
				productDB.setName(productDto.getName());
			}
			if (productDto.getDescription() != null) {
				if (productDB.getDescription().length() > 65535) { // Adjust based on the column type
					throw new IllegalArgumentException("Description is too long.");
				}
				productDB.setDescription(productDto.getDescription());
			}
			MultipartFile productImageData = productDto.getImageData();
			if (productImageData != null && !productImageData.isEmpty()) {
				productDB.setImageData(productImageData.getBytes());
				productDB.setImageName(productImageData.getOriginalFilename());
			}
			productDB.setUpdatedBy("Thuy");
			return productRepository.save(productDB);
		} catch (IOException e) {
			// Convert IOException to a runtime exception that can be handled by global
			// exception handler
			throw new FileProcessingException("Error processing image file", e);
		}

	}

	public void safeDeleteProduct(Long productId) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new EntityNotFoundException("Product not found"));

		// check if product has been ordered previously
		if (productHasOrderDetails(product)) {
			throw new IllegalStateException("Cannot delete product.It has associated orders");
		}

		// Delete all sizes associated with this product
		for (Size size : new ArrayList<>(product.getSizes())) {
			sizeRepository.delete(size);
		}
		productRepository.delete(product);
	}

	private boolean productHasOrderDetails(Product product) {
		if (!orderDetailRepository.findByProductId(product.getId()).isEmpty()) {
			return true;
		}
		return false;
	}

}
