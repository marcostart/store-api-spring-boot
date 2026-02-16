package marcostar.project.store_project.services;

import java.math.BigDecimal;
import java.util.UUID;
import marcostar.project.store_project.entities.Product;
import marcostar.project.store_project.entities.StockMovement;
import marcostar.project.store_project.entities.Unit;
import marcostar.project.store_project.entities.enums.MovementType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StockService {
    StockMovement recordMovement(Product product, BigDecimal quantity, MovementType type, Unit unit, String reason);
    Page<StockMovement> getMovementsForProduct(UUID productId, Pageable pageable);
    Page<StockMovement> getAllMovements(Pageable pageable);
}