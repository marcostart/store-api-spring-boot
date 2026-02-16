package marcostar.project.store_project.dtos.unit;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import marcostar.project.store_project.entities.enums.UnitCategory;

@Data
@Builder
@AllArgsConstructor
public class UnitResponse {
    private UUID id;
    private String code;
    private String name;
    private String symbol;
    private UnitCategory category;
    private Boolean isBaseUnit;
}
