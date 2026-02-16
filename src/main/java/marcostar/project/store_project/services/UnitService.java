package marcostar.project.store_project.services;

import java.util.List;
import java.util.UUID;
import marcostar.project.store_project.dtos.unit.UnitConversionRequest;
import marcostar.project.store_project.dtos.unit.UnitConversionResponse;
import marcostar.project.store_project.dtos.unit.UnitRequest;
import marcostar.project.store_project.dtos.unit.UnitResponse;
import marcostar.project.store_project.entities.enums.UnitCategory;


public interface UnitService {
    List<UnitResponse> getAllUnits();
    List<UnitResponse> getUnitsByCategory(UnitCategory category);
    UnitResponse getUnitById(UUID id);
    UnitResponse createUnit(UnitRequest request);
    UnitResponse updateUnit(UUID id, UnitRequest request);
    void deleteUnit(UUID id);
    List<UnitConversionResponse> getAllConversions();
    UnitConversionResponse getConversionById(UUID id);
    UnitConversionResponse createConversion(UnitConversionRequest request);
    UnitConversionResponse updateConversion(UUID id, UnitConversionRequest request);
    void deleteConversion(UUID id);
}