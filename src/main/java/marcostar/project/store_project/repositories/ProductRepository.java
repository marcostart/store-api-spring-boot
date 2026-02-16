package marcostar.project.store_project.repositories;

import java.util.UUID;
import marcostar.project.store_project.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
}