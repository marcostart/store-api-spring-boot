package marcostar.project.store_project.config.security;

import java.time.Instant;
import java.util.Optional;
import marcostar.project.store_project.entities.BlacklistedToken;
import marcostar.project.store_project.repositories.BlacklistedTokenRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

	private final BlacklistedTokenRepository blacklistedTokenRepository;
	private final JwtService jwtService;

	public void blacklist(String token) {
		if (token == null || token.isBlank()) {
			return;
		}
		if (blacklistedTokenRepository.existsByToken(token)) {
			return;
		}
		Instant expiresAt = jwtService.extractExpiration(token).toInstant();
		blacklistedTokenRepository.save(new BlacklistedToken(token, expiresAt));
	}

	public boolean isBlacklisted(String token) {
		if (token == null || token.isBlank()) {
			return false;
		}
		Optional<BlacklistedToken> entry = blacklistedTokenRepository.findByToken(token);
		if (entry.isEmpty()) {
			return false;
		}
		Instant expiresAt = entry.get().getExpiresAt();
		if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
			blacklistedTokenRepository.delete(entry.get());
			return false;
		}
		return true;
	}
}