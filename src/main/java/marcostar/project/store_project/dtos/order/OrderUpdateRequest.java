package marcostar.project.store_project.dtos.order;

import jakarta.validation.Valid;
import java.util.List;
import lombok.Data;

@Data
public class OrderUpdateRequest {
	@Valid
	private List<OrderItemRequest> items;
}
