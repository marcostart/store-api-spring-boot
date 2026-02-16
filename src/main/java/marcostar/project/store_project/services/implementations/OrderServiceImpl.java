package marcostar.project.store_project.services.implementations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import marcostar.project.store_project.config.security.LoggerUser;
import marcostar.project.store_project.dtos.order.OrderItemRequest;
import marcostar.project.store_project.dtos.order.OrderItemResponse;
import marcostar.project.store_project.dtos.order.OrderRequest;
import marcostar.project.store_project.dtos.order.OrderResponse;
import marcostar.project.store_project.dtos.order.OrderStatusRequest;
import marcostar.project.store_project.dtos.order.OrderUpdateRequest;
import marcostar.project.store_project.entities.CustomerOrder;
import marcostar.project.store_project.entities.OrderItem;
import marcostar.project.store_project.entities.Product;
import marcostar.project.store_project.entities.User;
import marcostar.project.store_project.entities.enums.MovementType;
import marcostar.project.store_project.entities.enums.OrderStatus;
import marcostar.project.store_project.repositories.OrderRepository;
import marcostar.project.store_project.repositories.ProductRepository;
import marcostar.project.store_project.services.OrderService;
import marcostar.project.store_project.services.StockService;
import marcostar.project.store_project.services.UnitConversionService;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{
	private static final String ORDER_NOT_FOUND = "Order not found";
    private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final StockService stockService;
    private final LoggerUser loggerUser;
	private final UnitConversionService unitConversionService;

    @Override
	@Transactional
	public OrderResponse createOrder(OrderRequest request) {
        
		User user = loggerUser.getCurrentUser();

		CustomerOrder order = CustomerOrder.builder()
				.user(user)
				.status(OrderStatus.PENDING)
				.orderNumber(generateOrderNumber())
				.build();

		for (OrderItemRequest itemRequest : request.getItems()) {
			Product product = productRepository.findById(itemRequest.getProductId())
					.orElseThrow(() -> new IllegalArgumentException("Product not found"));
			BigDecimal quantityInProductUnit = resolveQuantityInProductUnit(itemRequest, product);
			if (product.getStock().compareTo(quantityInProductUnit) < 0) {
				throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
			}

			if (quantityInProductUnit.compareTo(BigDecimal.ZERO) <= 0) {
				// throw new IllegalArgumentException("Quantity must be greater than zero for product: " + product.getName());
				continue;
			}

			product.setStock(product.getStock().subtract(quantityInProductUnit));
			productRepository.save(product);
			OrderItem orderItem = OrderItem.builder()
					.order(order)
					.product(product)
					.quantity(quantityInProductUnit)
					.unitPrice(product.getPrice())
					.build();
			order.getItems().add(orderItem);
		}

		CustomerOrder savedOrder = orderRepository.save(order);

		for (OrderItem oi : savedOrder.getItems()) {
			Product p = oi.getProduct();
			stockService.recordMovement(p, oi.getQuantity(), MovementType.OUT, p.getUnit(), "Creating new order Order " + savedOrder.getId());
			productRepository.save(p);
		}

		return toResponse(savedOrder);
	}

    @Override
	public Page<OrderResponse> getOrdersForUser(UUID userId, Pageable pageable) {
		return orderRepository.findByUserId(userId, pageable)
				.map(this::toResponse);
	}

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable){
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    public Page<OrderResponse> getMyOrders(Pageable pageable){
        User user = loggerUser.getCurrentUser();
        return orderRepository.findByUserId(user.getId(), pageable).map(this::toResponse);
    }

    @Override
	public OrderResponse getOrderById(UUID id) {
		CustomerOrder order = orderRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND));
		
		return toResponse(order);
	}

	@Override
	@Transactional
	public OrderResponse updateOrder(UUID id, OrderUpdateRequest request) {
		CustomerOrder order = orderRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND));
        User user = loggerUser.getCurrentUser();
		if (!order.getUser().getId().equals(user.getId())) {
			throw new IllegalArgumentException("Access denied: Only the author can update this order");
		}

		if (request.getItems() != null) {
			for (OrderItem existing : order.getItems()) {
				Product p = existing.getProduct();
				p.setStock(p.getStock().add(existing.getQuantity()));
				stockService.recordMovement(p, existing.getQuantity(), MovementType.IN, p.getUnit(),
						"Order update " + order.getId() + " (revert)");
				productRepository.save(p);
			}

			order.getItems().clear();

			for (OrderItemRequest itemRequest : request.getItems()) {
				Product product = productRepository.findById(itemRequest.getProductId())
						.orElseThrow(() -> new IllegalArgumentException("Product not found"));
				BigDecimal quantityInProductUnit = resolveQuantityInProductUnit(itemRequest, product);

				if (product.getStock().compareTo(quantityInProductUnit) < 0) {
					throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
				}

				product.setStock(product.getStock().subtract(quantityInProductUnit));
				productRepository.save(product);
				OrderItem orderItem = OrderItem.builder()
						.order(order)
						.product(product)
						.quantity(quantityInProductUnit)
						.unitPrice(product.getPrice())
						.build();
				order.getItems().add(orderItem);
			}

			CustomerOrder savedOrder = orderRepository.save(order);
			for (OrderItem oi : savedOrder.getItems()) {
				Product p = oi.getProduct();
				stockService.recordMovement(p, oi.getQuantity(), MovementType.OUT, p.getUnit(),
						"Order update " + savedOrder.getId());
				productRepository.save(p);
			}
			return toResponse(savedOrder);
		}

		CustomerOrder savedOrder = orderRepository.save(order);
		return toResponse(savedOrder);
	}

	private BigDecimal resolveQuantityInProductUnit(OrderItemRequest itemRequest, Product product) {
		BigDecimal converted = unitConversionService.convert(
				itemRequest.getQuantity(),
				itemRequest.getUnitId(),
				product.getUnit().getId());
		return converted.setScale(3, RoundingMode.HALF_UP);
	}

    @Override
    public OrderResponse updateOrderStatus(UUID id, OrderStatusRequest request) {
        CustomerOrder order = orderRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND));

        order.setStatus(request.getStatus());
        CustomerOrder savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

	@Override
	@Transactional
	public void deleteOrder(UUID id) {
		CustomerOrder order = orderRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException(ORDER_NOT_FOUND));
		
		for (OrderItem existing : order.getItems()) {
			Product p = existing.getProduct();
			p.setStock(p.getStock().add(existing.getQuantity()));
			stockService.recordMovement(p, existing.getQuantity(), MovementType.IN, p.getUnit(),
					"Order deletion " + order.getId());
			productRepository.save(p);
		}
		orderRepository.delete(order);
	}

	private OrderResponse toResponse(CustomerOrder order) {
		List<OrderItemResponse> items = order.getItems().stream()
				.map(item -> OrderItemResponse.builder()
						.productId(item.getProduct().getId())
						.productName(item.getProduct().getName())
						.quantity(item.getQuantity())
						.unitPrice(item.getUnitPrice())
						.subtotal(item.getUnitPrice().multiply(item.getQuantity()))
						.build())
				.toList();

		BigDecimal total = items.stream()
				.map(OrderItemResponse::getSubtotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		return OrderResponse.builder()
				.id(order.getId())
				.orderNumber(order.getOrderNumber())
				.status(order.getStatus())
				.createdAt(order.getCreatedAt())
				.updatedAt(order.getUpdatedAt())
				.items(items)
				.total(total)
				.build();
	}

	private String generateOrderNumber() {
		String orderNumber;
		do {
			long timestamp = Instant.now().toEpochMilli();
			int random = ThreadLocalRandom.current().nextInt(100000, 999999);
			orderNumber = "ORD-" + timestamp + "-" + random;
		} while (orderRepository.existsByOrderNumber(orderNumber));
		return orderNumber;
	}
}
