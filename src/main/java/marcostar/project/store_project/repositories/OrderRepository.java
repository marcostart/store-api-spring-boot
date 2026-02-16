package marcostar.project.store_project.repositories;

import java.util.UUID;
import marcostar.project.store_project.entities.CustomerOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {
	Page<CustomerOrder> findByUserUsername(String username, Pageable pageable);
	Page<CustomerOrder> findByUserId(UUID userId, Pageable pageable);
	boolean existsByOrderNumber(String orderNumber);
}