package marcostar.project.store_project.services.implementations;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import marcostar.project.store_project.dtos.unit.UnitConversionRequest;
import marcostar.project.store_project.dtos.unit.UnitConversionResponse;
import marcostar.project.store_project.dtos.unit.UnitRequest;
import marcostar.project.store_project.dtos.unit.UnitResponse;
import marcostar.project.store_project.entities.Unit;
import marcostar.project.store_project.entities.UnitConversion;
import marcostar.project.store_project.entities.enums.UnitCategory;
import marcostar.project.store_project.repositories.UnitConversionRepository;
import marcostar.project.store_project.repositories.UnitRepository;
import marcostar.project.store_project.services.UnitService;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService{
    private final UnitRepository unitRepository;
    private final UnitConversionRepository unitConversionRepository;

    @Override
    public List<UnitResponse> getAllUnits() {
        return unitRepository.findAll().stream()
                .map(this::toUnitResponse)
                .toList();
    }

    @Override
    public List<UnitResponse> getUnitsByCategory(UnitCategory category) {
        return unitRepository.findByCategory(category).stream()
                .map(this::toUnitResponse)
                .toList();
    }

    @Override
    public UnitResponse getUnitById(UUID id) {
        return unitRepository.findById(id)
                .map(this::toUnitResponse)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found"));
    }

    @Override
    @Transactional
    public UnitResponse createUnit(UnitRequest request) {
        if (unitRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Unit with code " + request.getCode() + " already exists");
        }

        if (Boolean.TRUE.equals(request.getIsBaseUnit())) {
            unsetOtherBaseUnit(request.getCategory(), null);
        }

        Unit unit = new Unit(
                request.getCode(),
                request.getName(),
                request.getSymbol(),
                request.getCategory(),
                request.getIsBaseUnit()
        );
        Unit saved = unitRepository.save(unit);
        return toUnitResponse(saved);
    }

    @Override
    @Transactional
    public UnitResponse updateUnit(UUID id, UnitRequest request) {
        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unit not found"));

        if (Boolean.TRUE.equals(request.getIsBaseUnit())) {
            unsetOtherBaseUnit(request.getCategory(), unit.getId());
        }

        unit.setCode(request.getCode());
        unit.setName(request.getName());
        unit.setSymbol(request.getSymbol());
        unit.setCategory(request.getCategory());
        unit.setIsBaseUnit(request.getIsBaseUnit());

        Unit saved = unitRepository.save(unit);
        return toUnitResponse(saved);
    }

    private void unsetOtherBaseUnit(UnitCategory category, UUID currentUnitId) {
        unitRepository.findByCategoryAndIsBaseUnit(category, true)
                .filter(existing -> currentUnitId == null || !existing.getId().equals(currentUnitId))
                .ifPresent(existing -> {
                    existing.setIsBaseUnit(false);
                    unitRepository.save(existing);
                });
    }

    @Override
    @Transactional
    public void deleteUnit(UUID id) {
        if (!unitRepository.existsById(id)) {
            throw new IllegalArgumentException("Unit not found");
        }
        unitRepository.deleteById(id);
    }

    @Override
    public List<UnitConversionResponse> getAllConversions() {
        return unitConversionRepository.findAll().stream()
                .map(this::toConversionResponse)
                .toList();
    }

    @Override
    public UnitConversionResponse getConversionById(UUID id) {
        return unitConversionRepository.findById(id)
                .map(this::toConversionResponse)
                .orElseThrow(() -> new IllegalArgumentException("Conversion not found"));
    }

    @Override
    @Transactional
    public UnitConversionResponse createConversion(UnitConversionRequest request) {
        Unit fromUnit = unitRepository.findById(request.getFromUnitId())
                .orElseThrow(() -> new IllegalArgumentException("From unit not found"));
        Unit toUnit = unitRepository.findById(request.getToUnitId())
                .orElseThrow(() -> new IllegalArgumentException("To unit not found"));

        if (!fromUnit.getCategory().equals(toUnit.getCategory())) {
            throw new IllegalArgumentException("Cannot create conversion between different categories");
        }

        if (unitConversionRepository.findByFromUnitAndToUnit(fromUnit, toUnit).isPresent()) {
            throw new IllegalArgumentException("Conversion already exists");
        }

        UnitConversion conversion = new UnitConversion(fromUnit, toUnit, request.getFactor());
        UnitConversion saved = unitConversionRepository.save(conversion);
        return toConversionResponse(saved);
    }

    @Override
    @Transactional
    public UnitConversionResponse updateConversion(UUID id, UnitConversionRequest request) {
        UnitConversion conversion = unitConversionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Conversion not found"));

        Unit fromUnit = unitRepository.findById(request.getFromUnitId())
                .orElseThrow(() -> new IllegalArgumentException("From unit not found"));
        Unit toUnit = unitRepository.findById(request.getToUnitId())
                .orElseThrow(() -> new IllegalArgumentException("To unit not found"));

        if (!fromUnit.getCategory().equals(toUnit.getCategory())) {
            throw new IllegalArgumentException("Cannot create conversion between different categories");
        }

        conversion.setFromUnit(fromUnit);
        conversion.setToUnit(toUnit);
        conversion.setFactor(request.getFactor());

        UnitConversion saved = unitConversionRepository.save(conversion);
        return toConversionResponse(saved);
    }

    @Override
    @Transactional
    public void deleteConversion(UUID id) {
        if (!unitConversionRepository.existsById(id)) {
            throw new IllegalArgumentException("Conversion not found");
        }
        unitConversionRepository.deleteById(id);
    }

    private UnitResponse toUnitResponse(Unit unit) {
        return UnitResponse.builder()
                .id(unit.getId())
                .code(unit.getCode())
                .name(unit.getName())
                .symbol(unit.getSymbol())
                .category(unit.getCategory())
                .isBaseUnit(unit.getIsBaseUnit())
                .build();
    }

    private UnitConversionResponse toConversionResponse(UnitConversion conversion) {
        return UnitConversionResponse.builder()
                .id(conversion.getId())
                .fromUnitId(conversion.getFromUnit().getId())
                .fromUnitCode(conversion.getFromUnit().getCode())
                .toUnitId(conversion.getToUnit().getId())
                .toUnitCode(conversion.getToUnit().getCode())
                .factor(conversion.getFactor())
                .build();
    }
}
