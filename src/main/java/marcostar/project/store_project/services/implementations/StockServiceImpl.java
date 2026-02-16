package marcostar.project.store_project.services.implementations;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import marcostar.project.store_project.entities.Product;
import marcostar.project.store_project.entities.StockMovement;
import marcostar.project.store_project.entities.Unit;
import marcostar.project.store_project.entities.enums.MovementType;
import marcostar.project.store_project.repositories.StockMovementRepository;
import marcostar.project.store_project.services.StockService;
import marcostar.project.store_project.services.UnitConversionService;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    private final StockMovementRepository stockMovementRepository;
    private final UnitConversionService unitConversionService;
    
    @Override
    public StockMovement recordMovement(Product product, BigDecimal quantity, MovementType type, Unit unit, String reason) {
        BigDecimal quantityInProductUnit = quantity;
        if (!unit.getId().equals(product.getUnit().getId())) {
            BigDecimal converted = unitConversionService.convert(
                quantity,
                unit,
                product.getUnit()
            );
            quantityInProductUnit = converted;
        }
        
        StockMovement m = new StockMovement(product, quantityInProductUnit, type, unit, reason);
        return stockMovementRepository.save(m);
    }

    @Override
    public Page<StockMovement> getMovementsForProduct(UUID productId, Pageable pageable) {
        return stockMovementRepository.findByProductId(productId, pageable);
    }

    @Override
    public Page<StockMovement> getAllMovements(Pageable pageable) {
        return stockMovementRepository.findAll(pageable);
    }
}
