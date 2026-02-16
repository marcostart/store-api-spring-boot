package marcostar.project.store_project.dtos.unit;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UnitConversionResponse {
    private UUID id;
    private UUID fromUnitId;
    private String fromUnitCode;
    private UUID toUnitId;
    private String toUnitCode;
    private BigDecimal factor;
}
