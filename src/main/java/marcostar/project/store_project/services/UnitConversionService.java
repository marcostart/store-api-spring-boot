package marcostar.project.store_project.services;

import marcostar.project.store_project.entities.Unit;
import marcostar.project.store_project.entities.enums.UnitCategory;

import java.math.BigDecimal;
import java.util.UUID;

public interface UnitConversionService {
    BigDecimal convert(BigDecimal quantity, Unit fromUnit, Unit toUnit);
    Unit getBaseUnit(UnitCategory category);
    BigDecimal convert(BigDecimal quantity, UUID fromUnitId, UUID toUnitId);
}
