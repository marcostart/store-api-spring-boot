package marcostar.project.store_project.dtos.unit;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class UnitConversionRequest {
    @NotNull
    private UUID fromUnitId;
    
    @NotNull
    private UUID toUnitId;
    
    @NotNull
    private BigDecimal factor;
}
