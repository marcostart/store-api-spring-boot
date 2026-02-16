package marcostar.project.store_project.services;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import marcostar.project.store_project.dtos.order.OrderRequest;
import marcostar.project.store_project.dtos.order.OrderResponse;
import marcostar.project.store_project.dtos.order.OrderStatusRequest;
import marcostar.project.store_project.dtos.order.OrderUpdateRequest;

public interface OrderService {
	OrderResponse createOrder(OrderRequest request);
	Page<OrderResponse> getOrdersForUser(UUID userId, Pageable pageable);
	Page<OrderResponse> getAllOrders(Pageable pageable);
	Page<OrderResponse> getMyOrders(Pageable pageable);
	OrderResponse getOrderById(UUID id);
	OrderResponse updateOrder(UUID id, OrderUpdateRequest request);
	OrderResponse updateOrderStatus(UUID id, OrderStatusRequest request);
	void deleteOrder(UUID id);
}