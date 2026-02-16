package marcostar.project.store_project.dtos.order;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class OrderRequest {
	@NotEmpty
	private List<OrderItemRequest> items;
}