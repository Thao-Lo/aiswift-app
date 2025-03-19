package com.aiswift.Tenant.Controller;


import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Tenant.Entity.Product;
import com.aiswift.Tenant.Service.ProductService;
import com.aiswift.dto.Tenant.ProductDto;

@Conditional(TenantDatabaseCondition.class)  // Only create for tenant databases
@RestController
@RequestMapping("/api/tenant/product")
public class ProductController {
	
	@Autowired
	ProductService productService;
	
	@GetMapping
	public ResponseEntity<List<Product>> getAllProducts(){
		List<Product> products = productService.findAllProducts();
		return ResponseEntity.ok(products);
	}
	
	@GetMapping("/{productId}")
	public ResponseEntity<Product> getProductById(@PathVariable Long productId){
		
		Product product =  productService.getProductById(productId);
		return ResponseEntity.ok(product);

	}
	
	@GetMapping("/category/{categoryId}")
	public ResponseEntity<List<Product>> getAllProductsByCategory(@PathVariable Long categoryId) {
	   List<Product> products = productService.findProductByCategoryId(categoryId);
	    return ResponseEntity.ok(products);
	}
	
	@PostMapping
	public ResponseEntity<Product> createProduct(
	   @RequestParam("productName") String name,
	   @RequestParam("price") BigDecimal price,
	   @RequestParam("categoryId") Long categoryId,
	   @RequestParam(name="imageData",required=false) MultipartFile file
	) {
	   Product savedProduct = productService.addNewProduct(name, price, categoryId, file);
	   return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
	}
	
	 @PutMapping("/prices") // update product prices in batches
	   public ResponseEntity<Void> updatePrices(@RequestBody List<Product> products) {
	       productService.updatePrices(products);
	       return ResponseEntity.ok().build();
	   }
	 @PutMapping("/{productId}")
	 public ResponseEntity<Product> updateProduct(
	     @PathVariable Long productId,
	     @ModelAttribute ProductDto productDto
	 ){
	     Product updatedProduct = productService.updateProduct(productDto, productId);
	     return ResponseEntity.ok(updatedProduct);
	 }
	  @DeleteMapping("/{productId}")
	   public ResponseEntity<Void> safeDeleteProduct(@PathVariable Long productId) {
	       productService.safeDeleteProduct(productId);
	       return ResponseEntity.ok().build();
	   }
	
}
