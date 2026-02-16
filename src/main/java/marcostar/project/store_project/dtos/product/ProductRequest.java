package marcostar.project.store_project.dtos.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class ProductRequest {
	@NotBlank
	private String name;

	private String description;

	@NotNull
	private BigDecimal price;

	private BigDecimal stock = null;

	@NotNull
	private UUID unitId;

	private String stockChangeReason;
}