package marcostar.project.store_project.config;

import lombok.RequiredArgsConstructor;
import marcostar.project.store_project.entities.Unit;
import marcostar.project.store_project.entities.UnitConversion;
import marcostar.project.store_project.entities.enums.UnitCategory;
import marcostar.project.store_project.repositories.UnitConversionRepository;
import marcostar.project.store_project.repositories.UnitRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(2)
@RequiredArgsConstructor
public class UnitSeeder implements CommandLineRunner {

    private final UnitRepository unitRepository;
    private final UnitConversionRepository unitConversionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedUnits();
        seedConversions();
    }

    private void seedUnits() {
        createUnitIfNotExists("GRAM", "Gram", "g", UnitCategory.WEIGHT, true);
        createUnitIfNotExists("KILOGRAM", "Kilogram", "kg", UnitCategory.WEIGHT, false);
        createUnitIfNotExists("MILLIGRAM", "Milligram", "mg", UnitCategory.WEIGHT, false);
        createUnitIfNotExists("TON", "Ton", "t", UnitCategory.WEIGHT, false);

        createUnitIfNotExists("MILLILITER", "Milliliter", "mL", UnitCategory.VOLUME, true);
        createUnitIfNotExists("LITER", "Liter", "L", UnitCategory.VOLUME, false);
        createUnitIfNotExists("CENTILITER", "Centiliter", "cL", UnitCategory.VOLUME, false);
        createUnitIfNotExists("DECILITER", "Deciliter", "dL", UnitCategory.VOLUME, false);
    }

    private void createUnitIfNotExists(String code, String name, String symbol, UnitCategory category, boolean isBaseUnit) {
        if (!unitRepository.existsByCode(code)) {
            Unit unit = new Unit(code, name, symbol, category, isBaseUnit);
            unitRepository.save(unit);
            System.out.println("Created unit: " + code);
        }
    }

    private void seedConversions() {
        Map<String, Unit> unitCache = new HashMap<>();
        unitRepository.findAll().forEach(unit -> unitCache.put(unit.getCode(), unit));

        createConversionIfNotExists(unitCache, "MILLIGRAM", "GRAM", "0.001");
        createConversionIfNotExists(unitCache, "GRAM", "MILLIGRAM", "1000");
        createConversionIfNotExists(unitCache, "GRAM", "KILOGRAM", "0.001");
        createConversionIfNotExists(unitCache, "KILOGRAM", "GRAM", "1000");
        createConversionIfNotExists(unitCache, "KILOGRAM", "TON", "0.001");
        createConversionIfNotExists(unitCache, "TON", "KILOGRAM", "1000");
        createConversionIfNotExists(unitCache, "MILLIGRAM", "KILOGRAM", "0.000001");
        createConversionIfNotExists(unitCache, "KILOGRAM", "MILLIGRAM", "1000000");
        createConversionIfNotExists(unitCache, "MILLIGRAM", "TON", "0.000000001");
        createConversionIfNotExists(unitCache, "TON", "MILLIGRAM", "1000000000");
        createConversionIfNotExists(unitCache, "GRAM", "TON", "0.000001");
        createConversionIfNotExists(unitCache, "TON", "GRAM", "1000000");

        createConversionIfNotExists(unitCache, "MILLILITER", "CENTILITER", "0.1");
        createConversionIfNotExists(unitCache, "CENTILITER", "MILLILITER", "10");
        createConversionIfNotExists(unitCache, "CENTILITER", "DECILITER", "0.1");
        createConversionIfNotExists(unitCache, "DECILITER", "CENTILITER", "10");
        createConversionIfNotExists(unitCache, "DECILITER", "LITER", "0.1");
        createConversionIfNotExists(unitCache, "LITER", "DECILITER", "10");
        createConversionIfNotExists(unitCache, "MILLILITER", "LITER", "0.001");
        createConversionIfNotExists(unitCache, "LITER", "MILLILITER", "1000");
        createConversionIfNotExists(unitCache, "MILLILITER", "DECILITER", "0.01");
        createConversionIfNotExists(unitCache, "DECILITER", "MILLILITER", "100");
        createConversionIfNotExists(unitCache, "CENTILITER", "LITER", "0.01");
        createConversionIfNotExists(unitCache, "LITER", "CENTILITER", "100");

    }

    private void createConversionIfNotExists(Map<String, Unit> unitCache, String fromCode, String toCode, String factor) {
        Unit fromUnit = unitCache.get(fromCode);
        Unit toUnit = unitCache.get(toCode);

        if (fromUnit == null || toUnit == null) {
            System.out.println("Skipping conversion " + fromCode + " -> " + toCode + " (unit not found)");
            return;
        }

        if (unitConversionRepository.findByFromUnitAndToUnit(fromUnit, toUnit).isEmpty()) {
            UnitConversion conversion = new UnitConversion(fromUnit, toUnit, new BigDecimal(factor));
            unitConversionRepository.save(conversion);
            System.out.println("Created conversion: " + fromCode + " -> " + toCode + " (×" + factor + ")");
        }
    }
}
