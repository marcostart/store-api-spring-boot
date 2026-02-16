package marcostar.project.store_project.services.implementations;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import marcostar.project.store_project.entities.Unit;
import marcostar.project.store_project.entities.UnitConversion;
import marcostar.project.store_project.entities.enums.UnitCategory;
import marcostar.project.store_project.repositories.UnitConversionRepository;
import marcostar.project.store_project.repositories.UnitRepository;
import marcostar.project.store_project.services.UnitConversionService;

@Service
@RequiredArgsConstructor
public class UnitConversionServiceImpl implements UnitConversionService {
    private final UnitConversionRepository unitConversionRepository;
    private final UnitRepository unitRepository;

    @Override
    public BigDecimal convert(BigDecimal quantity, Unit fromUnit, Unit toUnit) {
        if (!fromUnit.getCategory().equals(toUnit.getCategory())) {
            throw new IllegalArgumentException(
                String.format("Cannot convert between different unit categories: %s to %s", 
                    fromUnit.getCategory(), toUnit.getCategory())
            );
        }
        
        if (fromUnit.getId().equals(toUnit.getId())) {
            return quantity;
        }
        
        return unitConversionRepository.findByFromUnitAndToUnit(fromUnit, toUnit)
            .map(conversion -> {
                System.err.println("Converting " + quantity + " from " + fromUnit.getCode() + " to " + toUnit.getCode() + ", factor: " + quantity.multiply(conversion.getFactor()).setScale(3, RoundingMode.HALF_UP));
                return quantity.multiply(conversion.getFactor()).setScale(3, RoundingMode.HALF_UP);
            })
            .orElseGet(() -> {
                Unit baseUnit = unitRepository.findByCategoryAndIsBaseUnit(fromUnit.getCategory(), true)
                    .orElseThrow(() -> new IllegalArgumentException("No base unit found for category: " + fromUnit.getCategory()));
                
                BigDecimal inBaseUnit = convertToBase(quantity, fromUnit, baseUnit);
                
                return convertFromBase(inBaseUnit, baseUnit, toUnit);
            });
    }
    
    private BigDecimal convertToBase(BigDecimal quantity, Unit fromUnit, Unit baseUnit) {
        if (fromUnit.getId().equals(baseUnit.getId())) {
            return quantity;
        }
        
        
        return unitConversionRepository.findByFromUnitAndToUnit(fromUnit, baseUnit)
            .map(conversion -> quantity.multiply(conversion.getFactor()))
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("No conversion found from %s to base unit %s", fromUnit.getCode(), baseUnit.getCode())
            ));
    }
    
    private BigDecimal convertFromBase(BigDecimal quantity, Unit baseUnit, Unit toUnit) {
        if (baseUnit.getId().equals(toUnit.getId())) {
            return quantity;
        }
        UnitConversion conversion = unitConversionRepository.findByFromUnitAndToUnit(baseUnit, toUnit)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("No conversion found from base unit %s to %s", baseUnit.getCode(), toUnit.getCode())
            ));
        return quantity.multiply(conversion.getFactor()).setScale(2, RoundingMode.HALF_UP);
    }
    
    @Override
    public Unit getBaseUnit(UnitCategory category) {
        return unitRepository.findByCategoryAndIsBaseUnit(category, true)
            .orElseThrow(() -> new IllegalArgumentException("No base unit found for category: " + category));
    }
    
    @Override
    public BigDecimal convert(BigDecimal quantity, UUID fromUnitId, UUID toUnitId) {
        Unit fromUnit = unitRepository.findById(fromUnitId)
            .orElseThrow(() -> new IllegalArgumentException("From unit not found: " + fromUnitId));
        Unit toUnit = unitRepository.findById(toUnitId)
            .orElseThrow(() -> new IllegalArgumentException("To unit not found: " + toUnitId));
        
        return convert(quantity, fromUnit, toUnit);
    }
}
