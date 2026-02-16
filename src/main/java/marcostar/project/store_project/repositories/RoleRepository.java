package marcostar.project.store_project.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import marcostar.project.store_project.entities.Role;
import marcostar.project.store_project.entities.enums.TypeRole;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
	boolean existsByTypeRole(TypeRole typeRole);
	Optional<Role> findByTypeRole(TypeRole typeRole);
}
