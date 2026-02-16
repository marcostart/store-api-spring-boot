package marcostar.project.store_project.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import marcostar.project.store_project.dtos.auth.AuthRequest;
import marcostar.project.store_project.dtos.auth.AuthResponse;
import marcostar.project.store_project.dtos.auth.RegisterRequest;
import marcostar.project.store_project.dtos.auth.RegisterResponse;
import marcostar.project.store_project.entities.Role;
import marcostar.project.store_project.services.AuthenticationService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints pour l'authentification et la gestion des utilisateurs")
public class AuthController {

	private final AuthenticationService authService;

	@PostMapping("/register")
	@Operation(
		summary = "Créer un nouveau compte utilisateur",
		description = "Permet de créer un nouveau compte utilisateur avec email, username et mot de passe",
		responses = {
			@ApiResponse(responseCode = "200", description = "Utilisateur créé avec succès"),
			@ApiResponse(responseCode = "400", description = "Données invalides")
		}
	)
	public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.ok(authService.register(request));
	}

	@PostMapping("/login")
	@Operation(
		summary = "Se connecter",
		description = "Authentifie un utilisateur et retourne un token JWT",
		responses = {
			@ApiResponse(responseCode = "200", description = "Connexion réussie, token JWT retourné"),
			@ApiResponse(responseCode = "401", description = "Identifiants incorrects")
		}
	)
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/logout")
	@Operation(
		summary = "Se déconnecter",
		description = "Invalide le token JWT actuel en l'ajoutant à la blacklist",
		security = @SecurityRequirement(name = "bearerAuth"),
		responses = {
			@ApiResponse(responseCode = "204", description = "Déconnexion réussie"),
			@ApiResponse(responseCode = "401", description = "Token invalide ou manquant")
		}
	)
	public ResponseEntity<Void> logout(
		@Parameter(description = "Token JWT (format: Bearer <token>)", required = true)
		@RequestHeader("Authorization") String authHeader) {
		String token = authHeader != null && authHeader.startsWith("Bearer ")
				? authHeader.substring(7)
				: authHeader;
		authService.logout(token);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/roles")
	@Operation(
		summary = "Liste des rôles disponibles",
		description = "Retourne la liste de tous les rôles disponibles dans le système",
		responses = {
			@ApiResponse(responseCode = "200", description = "Liste des rôles retournée avec succès")
		}
	)
	public ResponseEntity<List<Role>> roles() {
		return ResponseEntity.ok(authService.getRoles());
	}
}