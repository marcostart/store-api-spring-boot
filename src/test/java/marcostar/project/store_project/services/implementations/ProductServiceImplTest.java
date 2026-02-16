package marcostar.project.store_project.services.implementations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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

import marcostar.project.store_project.dtos.product.ProductRequest;
import marcostar.project.store_project.dtos.product.ProductResponse;
import marcostar.project.store_project.entities.Product;
import marcostar.project.store_project.entities.Unit;
import marcostar.project.store_project.entities.enums.MovementType;
import marcostar.project.store_project.repositories.ProductRepository;
import marcostar.project.store_project.repositories.UnitRepository;
import marcostar.project.store_project.services.StockService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Unit Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockService stockService;

    @Mock
    private UnitRepository unitRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Unit testUnit;
    private Product testProduct;
    private ProductRequest testRequest;
    private UUID testUnitId;
    private UUID testProductId;

    @BeforeEach
    void setUp() {
        testUnitId = UUID.randomUUID();
        testProductId = UUID.randomUUID();

        testUnit = new Unit();
        testUnit.setId(testUnitId);
        testUnit.setCode("KG");
        testUnit.setSymbol("kg");
        testUnit.setName("Kilogram");

        testProduct = Product.builder()
                .id(testProductId)
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.valueOf(10.50))
                .stock(BigDecimal.valueOf(100))
                .unit(testUnit)
                .build();

        testRequest = new ProductRequest();
        testRequest.setName("New Product");
        testRequest.setDescription("New Description");
        testRequest.setPrice(BigDecimal.valueOf(15.00));
        testRequest.setStock(BigDecimal.valueOf(50));
        testRequest.setUnitId(testUnitId);
    }

    @Test
    @DisplayName("Should return paginated products")
    void testGetAll() {
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(java.util.List.of(testProduct));
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        
        Page<ProductResponse> result = productService.getAll(pageable);

        
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Product", result.getContent().get(0).getName());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Should return product by ID when product exists")
    void testGetByIdSuccess() {
        
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));

        
        ProductResponse result = productService.getById(testProductId);

        
        assertNotNull(result);
        assertEquals(testProductId, result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals(BigDecimal.valueOf(10.50), result.getPrice());
        verify(productRepository, times(1)).findById(testProductId);
    }

    @Test
    @DisplayName("Should throw exception when product not found by ID")
    void testGetByIdNotFound() {
        
        when(productRepository.findById(testProductId)).thenReturn(Optional.empty());

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.getById(testProductId)
        );
        assertEquals("Product not found", exception.getMessage());
        verify(productRepository, times(1)).findById(testProductId);
    }

    @Test
    @DisplayName("Should create product successfully with stock")
    void testCreateWithStock() {
        
        when(unitRepository.findById(testUnitId)).thenReturn(Optional.of(testUnit));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(testProductId);
            return product;
        });

        
        ProductResponse result = productService.create(testRequest);

        
        assertNotNull(result);
        assertEquals("New Product", result.getName());
        assertEquals(BigDecimal.valueOf(15.00), result.getPrice());
        assertEquals(BigDecimal.valueOf(50), result.getStock());
        verify(unitRepository, times(1)).findById(testUnitId);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(stockService, times(1)).recordMovement(
                any(Product.class),
                eq(BigDecimal.valueOf(50)),
                eq(MovementType.IN),
                eq(testUnit),
                eq("Initial stock")
        );
    }

    @Test
    @DisplayName("Should create product without recording stock movement when stock is zero")
    void testCreateWithZeroStock() {
        
        testRequest.setStock(BigDecimal.ZERO);
        when(unitRepository.findById(testUnitId)).thenReturn(Optional.of(testUnit));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(testProductId);
            return product;
        });

        
        ProductResponse result = productService.create(testRequest);

        
        assertNotNull(result);
        verify(stockService, never()).recordMovement(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw exception when unit not found during create")
    void testCreateUnitNotFound() {
        
        when(unitRepository.findById(testUnitId)).thenReturn(Optional.empty());

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.create(testRequest)
        );
        assertEquals("Unit not found", exception.getMessage());
        verify(unitRepository, times(1)).findById(testUnitId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product successfully with stock increase")
    void testUpdateWithStockIncrease() {
        
        testRequest.setStock(BigDecimal.valueOf(150));
        testRequest.setStockChangeReason("Restock");
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(unitRepository.findById(testUnitId)).thenReturn(Optional.of(testUnit));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        
        ProductResponse result = productService.update(testProductId, testRequest);

        
        assertNotNull(result);
        verify(productRepository, times(1)).findById(testProductId);
        verify(productRepository, times(1)).save(any(Product.class));
        verify(stockService, times(1)).recordMovement(
                any(Product.class),
                eq(BigDecimal.valueOf(50)),
                eq(MovementType.IN),
                eq(testUnit),
                eq("Restock")
        );
    }

    @Test
    @DisplayName("Should update product with stock decrease")
    void testUpdateWithStockDecrease() {
        
        testRequest.setStock(BigDecimal.valueOf(80));
        testRequest.setStockChangeReason("Manual adjustment");
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(unitRepository.findById(testUnitId)).thenReturn(Optional.of(testUnit));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        
        ProductResponse result = productService.update(testProductId, testRequest);

        
        assertNotNull(result);
        verify(stockService, times(1)).recordMovement(
                any(Product.class),
                eq(BigDecimal.valueOf(20)),
                eq(MovementType.OUT),
                eq(testUnit),
                eq("Manual adjustment")
        );
    }

    @Test
    @DisplayName("Should throw exception when updating stock without reason")
    void testUpdateStockWithoutReason() {
        
        testRequest.setStock(BigDecimal.valueOf(150));
        testRequest.setStockChangeReason(null);
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(unitRepository.findById(testUnitId)).thenReturn(Optional.of(testUnit));

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.update(testProductId, testRequest)
        );
        assertEquals("Stock change reason is required when stock is updated", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw exception when product not found during update")
    void testUpdateProductNotFound() {
        
        when(productRepository.findById(testProductId)).thenReturn(Optional.empty());

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.update(testProductId, testRequest)
        );
        assertEquals("Product not found", exception.getMessage());
        verify(productRepository, times(1)).findById(testProductId);
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteSuccess() {
        
        when(productRepository.existsById(testProductId)).thenReturn(true);

        
        productService.delete(testProductId);

        
        verify(productRepository, times(1)).existsById(testProductId);
        verify(productRepository, times(1)).deleteById(testProductId);
    }

    @Test
    @DisplayName("Should throw exception when product not found during delete")
    void testDeleteProductNotFound() {
        
        when(productRepository.existsById(testProductId)).thenReturn(false);

        
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.delete(testProductId)
        );
        assertEquals("Product not found", exception.getMessage());
        verify(productRepository, times(1)).existsById(testProductId);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should handle null stock correctly during update")
    void testUpdateWithNullStock() {
        
        testRequest.setStock(null);
        when(productRepository.findById(testProductId)).thenReturn(Optional.of(testProduct));
        when(unitRepository.findById(testUnitId)).thenReturn(Optional.of(testUnit));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        
        ProductResponse result = productService.update(testProductId, testRequest);

        
        assertNotNull(result);
        verify(stockService, never()).recordMovement(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should handle product with null initial stock during create")
    void testCreateWithNullStock() {
        
        testRequest.setStock(null);
        when(unitRepository.findById(testUnitId)).thenReturn(Optional.of(testUnit));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(testProductId);
            return product;
        });

        
        ProductResponse result = productService.create(testRequest);

        
        assertNotNull(result);
        verify(stockService, never()).recordMovement(any(), any(), any(), any(), any());
    }
}
