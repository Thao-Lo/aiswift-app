package com.aiswift.Tenant.Controller;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
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
import com.aiswift.Tenant.Entity.Product;
import com.aiswift.Tenant.Entity.Size;
import com.aiswift.Tenant.Service.ProductService;
import com.aiswift.Tenant.Service.SizeService;

@Conditional(TenantDatabaseCondition.class) // Only create for tenant databases
@RestController
@RequestMapping("/api/tenant/product/size")
public class SizeController {

	@Autowired
	SizeService sizeService;

	@Autowired
	ProductService productService;

	@GetMapping("/{sizeId}")
	public ResponseEntity<Size> getSizeById(@PathVariable Long sizeId) {
		Size size = sizeService.getSizeById(sizeId);
		return ResponseEntity.ok(size);
	}

	@GetMapping("/{productId}")
	public ResponseEntity<List<Size>> getAllSizesByProductId(@PathVariable Long productId) {
		List<Size> sizes = sizeService.getSizesByProductId(productId);
		return ResponseEntity.ok(sizes);
	}

	@PostMapping("/{productId}")
	public ResponseEntity<Size> addSizeToProduct(@PathVariable Long productId, @RequestParam(name = "name") String name,
			@RequestParam(name = "priceDifference", defaultValue = "0.0") BigDecimal priceDifference) {

		// find the product
		Product product = productService.getProductById(productId);
		// calculate the size price
		BigDecimal sizePrice = product.getPrice().add(priceDifference);
		// add size
		Size size = sizeService.addSize(name, productId, sizePrice);
		// Return the created size
		return ResponseEntity.status(HttpStatus.CREATED).body(size);
	}

	@PutMapping("{sizeId}")
	public ResponseEntity<Size> updateSize(@RequestParam(required = true, name = "name") String sizeName,
			@RequestParam(required = true, name = "id") Long sizeId,
			@RequestParam(required = true, name = "price") BigDecimal price) {

		Size updatedSize = sizeService.updateSize(sizeId, sizeName, price);
		return ResponseEntity.ok(updatedSize);

	}

	@DeleteMapping("/{sizeId}")
	public ResponseEntity<Void> deleteSize(@PathVariable Long sizeId) {
	   sizeService.deleteSize(sizeId);
	   return ResponseEntity.noContent().build();
	}
}
