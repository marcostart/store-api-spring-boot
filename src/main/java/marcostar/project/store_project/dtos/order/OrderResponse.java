package marcostar.project.store_project.dtos.order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import marcostar.project.store_project.entities.enums.OrderStatus;

@Data
@Builder
@AllArgsConstructor
public class OrderResponse {
	private UUID id;
	private String orderNumber;
	private OrderStatus status;
	private Date createdAt;
    private Date updatedAt;
	private List<OrderItemResponse> items;
	private BigDecimal total;
}