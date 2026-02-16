package marcostar.project.store_project.dtos.product;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ProductResponse {
	private UUID id;
	private String name;
	private String description;
	private BigDecimal price;
	private BigDecimal stock;
	private UUID unitId;
	private String unitCode;
	private String unitSymbol;
}