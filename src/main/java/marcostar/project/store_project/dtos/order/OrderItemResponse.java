package marcostar.project.store_project.dtos.order;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrderItemResponse {
	private UUID productId;
	private String productName;
	private BigDecimal quantity;
	private BigDecimal unitPrice;
	private BigDecimal subtotal;
}