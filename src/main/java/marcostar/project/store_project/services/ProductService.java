package marcostar.project.store_project.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;
import marcostar.project.store_project.dtos.product.ProductRequest;
import marcostar.project.store_project.dtos.product.ProductResponse;

public interface ProductService {
	Page<ProductResponse> getAll(Pageable pageable);
	ProductResponse getById(UUID id);
	ProductResponse create(ProductRequest request);
	ProductResponse update(UUID id, ProductRequest request);
	void delete(UUID id);
}