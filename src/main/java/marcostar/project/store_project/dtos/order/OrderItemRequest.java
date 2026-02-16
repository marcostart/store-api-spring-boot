package marcostar.project.store_project.dtos.order;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class OrderItemRequest {
	@NotNull
	private UUID productId;

	@NotNull
	private UUID unitId;

	@NotNull
	private BigDecimal quantity;
}