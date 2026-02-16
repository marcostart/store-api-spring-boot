package marcostar.project.store_project.dtos.unit;

import org.hibernate.annotations.ColumnDefault;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import marcostar.project.store_project.entities.enums.UnitCategory;

@Data
public class UnitRequest {
    @NotBlank
    private String code;
    
    @NotBlank
    private String name;
    
    @NotBlank
    private String symbol;
    
    @NotNull
    private UnitCategory category;
    
    @ColumnDefault(value = "false")
    private Boolean isBaseUnit=false;
}
