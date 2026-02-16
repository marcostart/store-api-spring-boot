package marcostar.project.store_project.repositories;

import java.util.Optional;
import java.util.UUID;
import marcostar.project.store_project.entities.Unit;
import marcostar.project.store_project.entities.UnitConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitConversionRepository extends JpaRepository<UnitConversion, UUID> {
    Optional<UnitConversion> findByFromUnitAndToUnit(Unit fromUnit, Unit toUnit);
}
