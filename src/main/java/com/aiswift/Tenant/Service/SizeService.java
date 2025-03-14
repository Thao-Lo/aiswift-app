package com.aiswift.Tenant.Service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Tenant.Entity.OrderDetail;
import com.aiswift.Tenant.Entity.Product;
import com.aiswift.Tenant.Entity.Size;
import com.aiswift.Tenant.Repository.OrderDetailRepository;
import com.aiswift.Tenant.Repository.ProductRepository;
import com.aiswift.Tenant.Repository.SizeRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;


@Conditional(TenantDatabaseCondition.class)  // Only create for tenant databases
@Service
public class SizeService {

	@Autowired
	private SizeRepository sizeRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private OrderDetailRepository orderDetailRepository;

	public Size getSizesById(Long sizeId) {
		return sizeRepository.findById(sizeId)
				.orElseThrow(() -> new EntityNotFoundException("Size not found with id: " + sizeId));
	}

	public List<Size> getSizesByProductId(Long productId) {
		return sizeRepository.findByProductId(productId);
	}

	public Size addSize(String name, Long productId, BigDecimal finalPrice) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new EntityNotFoundException("Category not found"));

		Size size = new Size();
		size.setName(name);
		size.setProduct(product);
		size.setSizePrice(finalPrice);

		return sizeRepository.save(size);
	}

	public void updateSize(Size size) {
		sizeRepository.save(size);

	}

	@Transactional
	public void deleteSize(Long id) {

		Size size = sizeRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Size not found with id: " + id));
		// Check if the size is associated with any orders
		List<OrderDetail> associatedOrders = orderDetailRepository.findBySizeId(id);
		if (!associatedOrders.isEmpty()) {
			throw new IllegalStateException("Cannot delete size." + size.getName() + " It is associated with "
					+ associatedOrders.size() + " order(s).");
		}
		size.setProduct(null);
		sizeRepository.delete(size);

	}

}
