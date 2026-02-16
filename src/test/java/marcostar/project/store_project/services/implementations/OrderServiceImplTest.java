package marcostar.project.store_project.services.implementations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import marcostar.project.store_project.config.security.LoggerUser;
import marcostar.project.store_project.dtos.order.OrderItemRequest;
import marcostar.project.store_project.dtos.order.OrderRequest;
import marcostar.project.store_project.dtos.order.OrderResponse;
import marcostar.project.store_project.dtos.order.OrderStatusRequest;
import marcostar.project.store_project.dtos.order.OrderUpdateRequest;
import marcostar.project.store_project.entities.CustomerOrder;
import marcostar.project.store_project.entities.OrderItem;
import marcostar.project.store_project.entities.Product;
import marcostar.project.store_project.entities.Unit;
import marcostar.project.store_project.entities.User;
import marcostar.project.store_project.entities.enums.MovementType;
import marcostar.project.store_project.entities.enums.OrderStatus;
import marcostar.project.store_project.repositories.OrderRepository;
import marcostar.project.store_project.repositories.ProductRepository;
import marcostar.project.store_project.services.StockService;
import marcostar.project.store_project.services.UnitConversionService;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Unit Tests")
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockService stockService;

    @Mock
    private LoggerUser loggerUser;

    @Mock
    private UnitConversionService unitConversionService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Unit testUnit;
    private Product testProduct;
    private CustomerOrder testOrder;
    private UUID testUserId;
    private UUID testProductId;
    private UUID testOrderId;
    private UUID testUnitId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
        testOrderId = UUID.randomUUID();
        testUnitId = UUID.randomUUID();

        testUser = User.builder()
                .id(testUserId)
                .username("testuser")
                .build();

        testUnit = new Unit();
        testUnit.setId(testUnitId);
        testUnit.setCode("KG");
        testUnit.setSymbol("kg");
        testUnit.setName("Kilogram");

        testProduct = Product.builder()
                .id(testProductId)
                .name("Test Product")
                .price(BigDecimal.valueOf(10.00))
                .stock(BigDecimal.valueOf(100))
                .unit(testUnit)
                .build();

        testOrder = CustomerOrder.builder()
                .id(testOrderId)
                .orderNumber("ORD-123456789-123456")
                .user(testUser)
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should create order successfully")
    void testCreateOrderSuccess() {
        
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(testProductId);
        itemRequest.setQuantity(BigDecimal.valueOf(150));
        itemRequest.setUnitId(testUnitId);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(List.of(itemRequest));

        when(loggerUser.getCurrentUser()).thenReturn(testUser);
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(unitConversionService.convert(any(BigDecimal.class), any(UUID.class), any(UUID.class)))
                .thenReturn(BigDecimal.valueOf(5));
        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> {
            CustomerOrder order = invocation.getArgument(0);
            order.setId(testOrderId);
            return order;
        });

        
        OrderResponse result = orderService.createOrder(orderRequest);

        
        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        verify(loggerUser, times(1)).getCurrentUser();
        verify(productRepository, times(1)).findById(testProductId);
        verify(productRepository, times(2)).save(any(Product.class));
        verify(stockService, times(1)).recordMovement(
                any(Product.class),
                any(BigDecimal.class),
                eq(MovementType.OUT),
                any(Unit.class),
                contains("Creating new order")
        );
    }

    @Test
    @DisplayName("Should throw exception when product not found during order creation")
    void testCreateOrderProductNotFound() {
        
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(testProductId);
        itemRequest.setQuantity(BigDecimal.valueOf(5));
        itemRequest.setUnitId(testUnitId);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(List.of(itemRequest));

        when(loggerUser.getCurrentUser()).thenReturn(testUser);
        when(productRepository.findById(testProductId)).thenReturn(Optional.empty());

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(orderRequest)
        );
        assertEquals("Product not found", exception.getMessage());
        verify(orderRepository, never()).save(any(CustomerOrder.class));
    }

    @Test
    @DisplayName("Should throw exception when insufficient stock")
    void testCreateOrderInsufficientStock() {
        
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(testProductId);
        itemRequest.setQuantity(BigDecimal.valueOf(150));
        itemRequest.setUnitId(testUnitId);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(List.of(itemRequest));

        when(loggerUser.getCurrentUser()).thenReturn(testUser);
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(unitConversionService.convert(any(BigDecimal.class), any(UUID.class), any(UUID.class)))
                .thenReturn(BigDecimal.valueOf(150));

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(orderRequest)
        );
        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(orderRepository, never()).save(any(CustomerOrder.class));
    }

    @Test
    @DisplayName("Should skip order item when quantity is zero or negative")
    void testCreateOrderSkipZeroQuantity() {
        
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(testProductId);
        itemRequest.setQuantity(BigDecimal.valueOf(0));
        itemRequest.setUnitId(testUnitId);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(List.of(itemRequest));

        when(loggerUser.getCurrentUser()).thenReturn(testUser);
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(unitConversionService.convert(any(BigDecimal.class), any(UUID.class), any(UUID.class)))
                .thenReturn(BigDecimal.ZERO);
        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        OrderResponse result = orderService.createOrder(orderRequest);

        
        assertNotNull(result);
        assertEquals(0, result.getItems().size());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should return paginated orders for user")
    void testGetOrdersForUser() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomerOrder> orderPage = new PageImpl<>(List.of(testOrder));
        when(orderRepository.findByUserId(testUserId, pageable)).thenReturn(orderPage);

        
        Page<OrderResponse> result = orderService.getOrdersForUser(testUserId, pageable);

        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findByUserId(testUserId, pageable);
    }

    @Test
    @DisplayName("Should return all orders paginated")
    void testGetAllOrders() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomerOrder> orderPage = new PageImpl<>(List.of(testOrder));
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        
        Page<OrderResponse> result = orderService.getAllOrders(pageable);

        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should get my orders for current user")
    void testGetMyOrders() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomerOrder> orderPage = new PageImpl<>(List.of(testOrder));
        when(loggerUser.getCurrentUser()).thenReturn(testUser);
        when(orderRepository.findByUserId(testUserId, pageable)).thenReturn(orderPage);

        
        Page<OrderResponse> result = orderService.getMyOrders(pageable);

        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(loggerUser, times(1)).getCurrentUser();
        verify(orderRepository, times(1)).findByUserId(testUserId, pageable);
    }

    @Test
    @DisplayName("Should get order by ID successfully")
    void testGetOrderByIdSuccess() {
        
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        
        OrderResponse result = orderService.getOrderById(testOrderId);

        
        assertNotNull(result);
        assertEquals(testOrderId, result.getId());
        verify(orderRepository, times(1)).findById(testOrderId);
    }

    @Test
    @DisplayName("Should throw exception when order not found by ID")
    void testGetOrderByIdNotFound() {
        
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getOrderById(testOrderId)
        );
        assertEquals("Order not found", exception.getMessage());
        verify(orderRepository, times(1)).findById(testOrderId);
    }

    @Test
    @DisplayName("Should update order status successfully")
    void testUpdateOrderStatus() {
        
        OrderStatusRequest statusRequest = new OrderStatusRequest();
        statusRequest.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(CustomerOrder.class))).thenReturn(testOrder);

        
        OrderResponse result = orderService.updateOrderStatus(testOrderId, statusRequest);

        
        assertNotNull(result);
        verify(orderRepository, times(1)).findById(testOrderId);
        verify(orderRepository, times(1)).save(any(CustomerOrder.class));
    }

    @Test
    @DisplayName("Should update order items successfully")
    void testUpdateOrderItems() {
        
        OrderItem existingItem = OrderItem.builder()
                .product(testProduct)
                .quantity(BigDecimal.valueOf(5))
                .unitPrice(BigDecimal.valueOf(10.00))
                .order(testOrder)
                .build();
        testOrder.getItems().add(existingItem);

        OrderItemRequest newItemRequest = new OrderItemRequest();
        newItemRequest.setProductId(testProductId);
        newItemRequest.setQuantity(BigDecimal.valueOf(10));
        newItemRequest.setUnitId(testUnitId);

        OrderUpdateRequest updateRequest = new OrderUpdateRequest();
        updateRequest.setItems(List.of(newItemRequest));

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(loggerUser.getCurrentUser()).thenReturn(testUser);
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(unitConversionService.convert(any(BigDecimal.class), any(UUID.class), any(UUID.class)))
                .thenReturn(BigDecimal.valueOf(10));
        when(orderRepository.save(any(CustomerOrder.class))).thenReturn(testOrder);

        
        OrderResponse result = orderService.updateOrder(testOrderId, updateRequest);

        
        assertNotNull(result);
        verify(orderRepository, times(1)).findById(testOrderId);
        verify(stockService, atLeastOnce()).recordMovement(
                any(Product.class),
                any(BigDecimal.class),
                any(MovementType.class),
                any(Unit.class),
                anyString()
        );
    }

    @Test
    @DisplayName("Should throw exception when updating order by non-owner")
    void testUpdateOrderAccessDenied() {
        
        User differentUser = User.builder()
                .id(UUID.randomUUID())
                .username("differentuser")
                .build();

        OrderUpdateRequest updateRequest = new OrderUpdateRequest();
        updateRequest.setItems(List.of());

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
        when(loggerUser.getCurrentUser()).thenReturn(differentUser);

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.updateOrder(testOrderId, updateRequest)
        );
        assertTrue(exception.getMessage().contains("Access denied"));
        verify(orderRepository, never()).save(any(CustomerOrder.class));
    }

    @Test
    @DisplayName("Should delete order successfully and restore stock")
    void testDeleteOrderSuccess() {
        
        OrderItem item = OrderItem.builder()
                .product(testProduct)
                .quantity(BigDecimal.valueOf(5))
                .unitPrice(BigDecimal.valueOf(10.00))
                .order(testOrder)
                .build();
        testOrder.getItems().add(item);

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        
        orderService.deleteOrder(testOrderId);

        
        verify(orderRepository, times(1)).findById(testOrderId);
        verify(stockService, times(1)).recordMovement(
                any(Product.class),
                eq(BigDecimal.valueOf(5)),
                eq(MovementType.IN),
                any(Unit.class),
                contains("Order deletion")
        );
        verify(productRepository, times(1)).save(any(Product.class));
        verify(orderRepository, times(1)).delete(testOrder);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent order")
    void testDeleteOrderNotFound() {
        
        when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.deleteOrder(testOrderId)
        );
        assertEquals("Order not found", exception.getMessage());
        verify(orderRepository, never()).delete(any(CustomerOrder.class));
    }

    @Test
    @DisplayName("Should generate unique order number")
    void testGenerateOrderNumber() {
        
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(testProductId);
        itemRequest.setQuantity(BigDecimal.valueOf(5));
        itemRequest.setUnitId(testUnitId);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(List.of(itemRequest));

        when(loggerUser.getCurrentUser()).thenReturn(testUser);
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(unitConversionService.convert(any(BigDecimal.class), any(UUID.class), any(UUID.class)))
                .thenReturn(BigDecimal.valueOf(5));
        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> {
            CustomerOrder order = invocation.getArgument(0);
            order.setId(testOrderId);
            return order;
        });

        
        OrderResponse result = orderService.createOrder(orderRequest);

        
        assertNotNull(result);
        assertNotNull(result.getOrderNumber());
        assertTrue(result.getOrderNumber().startsWith("ORD-"));
        verify(orderRepository, atLeastOnce()).existsByOrderNumber(anyString());
    }

    @Test
    @DisplayName("Should calculate order total correctly")
    void testOrderTotalCalculation() {
        
        OrderItem item1 = OrderItem.builder()
                .product(testProduct)
                .quantity(BigDecimal.valueOf(2))
                .unitPrice(BigDecimal.valueOf(10.00))
                .order(testOrder)
                .build();

        OrderItem item2 = OrderItem.builder()
                .product(testProduct)
                .quantity(BigDecimal.valueOf(3))
                .unitPrice(BigDecimal.valueOf(15.00))
                .order(testOrder)
                .build();

        testOrder.getItems().add(item1);
        testOrder.getItems().add(item2);

        when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

        
        OrderResponse result = orderService.getOrderById(testOrderId);

        
        assertNotNull(result);
        BigDecimal expectedTotal = BigDecimal.valueOf(20.00).add(BigDecimal.valueOf(45.00));
        assertEquals(expectedTotal, result.getTotal());
    }
}
