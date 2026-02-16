package marcostar.project.store_project.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marcostar.project.store_project.entities.Unit;
import marcostar.project.store_project.entities.enums.UnitCategory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitRepository extends JpaRepository<Unit, UUID> {
    Optional<Unit> findByCode(String code);
    boolean existsByCode(String code);
    List<Unit> findByCategory(UnitCategory category);
    Optional<Unit> findByCategoryAndIsBaseUnit(UnitCategory category, Boolean isBaseUnit);
}
