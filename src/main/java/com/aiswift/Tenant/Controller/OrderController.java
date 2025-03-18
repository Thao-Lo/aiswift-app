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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.aiswift.Config.TenantDatabaseCondition;
import com.aiswift.Enum.OrderStatus;
import com.aiswift.Tenant.Entity.Order;
import com.aiswift.Tenant.Service.OrderDetailService;
import com.aiswift.Tenant.Service.OrderService;
import com.aiswift.dto.Tenant.OrderDetailDto;

@Conditional(TenantDatabaseCondition.class) // Only create for tenant databases
@RestController
@RequestMapping("/api/tenant/order")
public class OrderController {
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	OrderDetailService orderDetailService;
	
	@GetMapping
	public ResponseEntity<List<Order>> getAllOrders(){
		List<Order>orders =  orderService.getAllOrders();
		return ResponseEntity.ok(orders);
		
	}
	
	@GetMapping("/{orderId}")
	public ResponseEntity<Order> getOrderById(@PathVariable Long orderId){
		Order order =  orderService.getOrderById(orderId);
		return ResponseEntity.ok(order);
		
	}
	
	@PutMapping("/status/{orderId}")
	public ResponseEntity<Order>updateOrderStatus(@PathVariable Long orderId,@RequestParam(name="orderStatus",required=false) OrderStatus status){
		Order order = orderService.getOrderById(orderId);
		order.setStatus(status);
		order = orderService.updateOrderStatusHistory(order,null);
		return ResponseEntity.ok(order);
	}
	
	@PutMapping("/{orderId}")
	public ResponseEntity<Order>updateOrder(@PathVariable Long orderId, @RequestBody List<OrderDetailDto> orderDetailDtos){
		Order order = orderService.getOrderById(orderId);
		order = orderService.updateOrderWithItems(order, orderDetailDtos);
		return ResponseEntity.ok(order);
		
	}
	
	@PostMapping
	public ResponseEntity<Order>createOrder(@RequestBody List<OrderDetailDto> orderDetailDtos){
		Order order = orderService.createOrderWithItems(orderDetailDtos);
		return ResponseEntity.ok(order);
	}
	
	@DeleteMapping("/{orderId}")
	public ResponseEntity<Order>cancelOrder(@PathVariable Long orderId){
		Order order = orderService.getOrderById(orderId);
		order = orderService.cancelOrder(order);
		return ResponseEntity.ok(order);
	}
	

}
