package marcostar.project.store_project.services.implementations;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import marcostar.project.store_project.dtos.product.ProductRequest;
import marcostar.project.store_project.dtos.product.ProductResponse;
import marcostar.project.store_project.entities.Product;
import marcostar.project.store_project.entities.Unit;
import marcostar.project.store_project.entities.enums.MovementType;
import marcostar.project.store_project.repositories.ProductRepository;
import marcostar.project.store_project.repositories.UnitRepository;
import marcostar.project.store_project.services.ProductService;
import marcostar.project.store_project.services.StockService;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
	private final StockService stockService;
	private final UnitRepository unitRepository;

    @Override
	public Page<ProductResponse> getAll(Pageable pageable) {
		return productRepository.findAll(pageable)
				.map(this::toResponse);
	}

    @Override
	public ProductResponse getById(UUID id) {
		return productRepository.findById(id)
				.map(this::toResponse)
				.orElseThrow(() -> new IllegalArgumentException("Product not found"));
	}

    @Override
	@Transactional
	public ProductResponse create(ProductRequest request) {
		Unit unit = unitRepository.findById(request.getUnitId())
				.orElseThrow(() -> new IllegalArgumentException("Unit not found"));
		
		Product product = Product.builder()
				.name(request.getName())
				.description(request.getDescription())
				.price(request.getPrice())
				.stock(request.getStock())
				.unit(unit)
				.build();
		Product saved = productRepository.save(product);
		
		if (saved.getStock() != null && saved.getStock().compareTo(BigDecimal.ZERO) > 0) {
			stockService.recordMovement(saved, saved.getStock(), MovementType.IN, saved.getUnit(), "Initial stock");
		}
		
		return toResponse(saved);
	}

    @Override
	@Transactional
	public ProductResponse update(UUID id, ProductRequest request) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Product not found"));

		Unit unit = unitRepository.findById(request.getUnitId())
				.orElseThrow(() -> new IllegalArgumentException("Unit not found"));

		BigDecimal oldStock = product.getStock() == null ? BigDecimal.ZERO : product.getStock();

		if (request.getStock() != null) {
			if (oldStock != request.getStock() && request.getStockChangeReason() == null) {
				throw new IllegalArgumentException("Stock change reason is required when stock is updated");
			} else {
				product.setStock(request.getStock());
			}
			
		}
		product.setName(request.getName());
		product.setDescription(request.getDescription());
		product.setPrice(request.getPrice());
		product.setUnit(unit);
		Product saved = productRepository.save(product);
		
		BigDecimal delta = (saved.getStock() == null ? BigDecimal.ZERO : saved.getStock()).subtract(oldStock);
		if (delta.compareTo(BigDecimal.ZERO) != 0) {
			MovementType type = delta.compareTo(BigDecimal.ZERO) > 0 ? MovementType.IN : MovementType.OUT;
			stockService.recordMovement(saved, delta.abs(), type, saved.getUnit(), request.getStockChangeReason());
		}
		return toResponse(saved);
	}

    @Override
	@Transactional
	public void delete(UUID id) {
		if (!productRepository.existsById(id)) {
			throw new IllegalArgumentException("Product not found");
		}
		productRepository.deleteById(id);
	}

	private ProductResponse toResponse(Product product) {
		return ProductResponse.builder()
				.id(product.getId())
				.name(product.getName())
				.description(product.getDescription())
				.price(product.getPrice())
				.stock(product.getStock())
				.unitId(product.getUnit().getId())
				.unitCode(product.getUnit().getCode())
				.unitSymbol(product.getUnit().getSymbol())
				.build();
	}
}
