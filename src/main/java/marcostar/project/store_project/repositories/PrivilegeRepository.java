package marcostar.project.store_project.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import marcostar.project.store_project.entities.Privilege;

@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, UUID> {
    
}
