package marcostar.project.store_project.repositories;

import java.util.Optional;
import java.util.UUID;
import marcostar.project.store_project.entities.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, UUID> {
	Optional<BlacklistedToken> findByToken(String token);
	boolean existsByToken(String token);
}