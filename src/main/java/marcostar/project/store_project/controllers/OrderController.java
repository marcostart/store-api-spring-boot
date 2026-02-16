package marcostar.project.store_project.controllers;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import marcostar.project.store_project.dtos.order.OrderRequest;
import marcostar.project.store_project.dtos.order.OrderResponse;
import marcostar.project.store_project.dtos.order.OrderStatusRequest;
import marcostar.project.store_project.dtos.order.OrderUpdateRequest;
import marcostar.project.store_project.services.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<OrderResponse> createOrder(
			@Valid @RequestBody OrderRequest request) {
		return ResponseEntity.ok(orderService.createOrder(request));
	}

	@GetMapping("/by-user/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<OrderResponse>> getOrders(
			@PathVariable UUID userId,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(orderService.getOrdersForUser(userId, pageable));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<OrderResponse>> getAllOrders(
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(orderService.getAllOrders(pageable));
	}

	@GetMapping("/my-orders")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<Page<OrderResponse>> getMyOrders(
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(orderService.getMyOrders(pageable));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<OrderResponse> getOrder(
			@PathVariable UUID id) {
		return ResponseEntity.ok(orderService.getOrderById(id));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
	public ResponseEntity<OrderResponse> updateOrder(
			@PathVariable UUID id,
			@Valid @RequestBody OrderUpdateRequest request) {
		return ResponseEntity.ok(orderService.updateOrder(id, request));
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<OrderResponse> updateOrderStatus(
			@PathVariable UUID id,
			@Valid @RequestBody OrderStatusRequest request) {
		return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	public ResponseEntity<Void> deleteOrder(
			@PathVariable UUID id) {
		orderService.deleteOrder(id);
		return ResponseEntity.noContent().build();
	}
}