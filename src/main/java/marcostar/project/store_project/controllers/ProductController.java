package marcostar.project.store_project.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import marcostar.project.store_project.dtos.product.ProductRequest;
import marcostar.project.store_project.dtos.product.ProductResponse;
import marcostar.project.store_project.entities.StockMovement;
import marcostar.project.store_project.services.ProductService;
import marcostar.project.store_project.services.StockService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Endpoints pour la gestion des produits")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

	private final ProductService productService;
	private final StockService stockService;

	@GetMapping
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(
		summary = "Liste tous les produits",
		description = "Récupère la liste complète de tous les produits avec leurs unités",
		responses = {
			@ApiResponse(responseCode = "200", description = "Liste des produits récupérée avec succès"),
			@ApiResponse(responseCode = "401", description = "Non authentifié")
		}
	)
	public ResponseEntity<Page<ProductResponse>> getAll(
		@Parameter(description = "Paramètres de pagination (page, size, sort)")
		@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(productService.getAll(pageable));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(
		summary = "Récupère un produit par son ID",
		description = "Retourne les détails d'un produit spécifique",
		responses = {
			@ApiResponse(responseCode = "200", description = "Produit trouvé"),
			@ApiResponse(responseCode = "404", description = "Produit non trouvé"),
			@ApiResponse(responseCode = "401", description = "Non authentifié")
		}
	)
	public ResponseEntity<ProductResponse> getById(
		@Parameter(description = "ID du produit") @PathVariable UUID id) {
		return ResponseEntity.ok(productService.getById(id));
	}

	@GetMapping("/{id}/movements")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "Récupère les mouvements de stock d'un produit",
		description = "Retourne la liste des mouvements de stock pour un produit spécifique",
		responses = {
			@ApiResponse(responseCode = "200", description = "Produit trouvé"),
			@ApiResponse(responseCode = "404", description = "Produit non trouvé"),
			@ApiResponse(responseCode = "401", description = "Non authentifié")
		}
	)
	public ResponseEntity<Page<StockMovement>> getMovementsForProduct(
		@Parameter(description = "ID du produit") @PathVariable UUID id,
		@Parameter(description = "Paramètres de pagination (page, size, sort)")
		@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(stockService.getMovementsForProduct(id, pageable));
	}

	@GetMapping("/movements")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "Récupère les mouvements de stock de tous les produits",
		description = "Retourne la liste des mouvements de stock pour tous les produits",
		responses = {
			@ApiResponse(responseCode = "200", description = "Produit trouvé"),
			@ApiResponse(responseCode = "404", description = "Produit non trouvé"),
			@ApiResponse(responseCode = "401", description = "Non authentifié")
		}
	)
	public ResponseEntity<Page<StockMovement>> getAllMovements(
		@Parameter(description = "Paramètres de pagination (page, size, sort)")
		@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(stockService.getAllMovements(pageable));
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "Créer un nouveau produit",
		description = "Crée un nouveau produit avec son stock initial et enregistre un mouvement de stock IN",
		responses = {
			@ApiResponse(responseCode = "201", description = "Produit créé avec succès"),
			@ApiResponse(responseCode = "400", description = "Données invalides"),
			@ApiResponse(responseCode = "403", description = "Accès refusé - Rôle ADMIN requis")
		}
	)
	public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "Mettre à jour un produit",
		description = "Met à jour un produit existant. Les changements de stock sont enregistrés comme mouvements IN/OUT",
		responses = {
			@ApiResponse(responseCode = "200", description = "Produit mis à jour avec succès"),
			@ApiResponse(responseCode = "404", description = "Produit non trouvé"),
			@ApiResponse(responseCode = "400", description = "Données invalides"),
			@ApiResponse(responseCode = "403", description = "Accès refusé - Rôle ADMIN requis")
		}
	)
	public ResponseEntity<ProductResponse> update(
		@Parameter(description = "ID du produit") @PathVariable UUID id, 
		@Valid @RequestBody ProductRequest request) {
		return ResponseEntity.ok(productService.update(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(
		summary = "Supprimer un produit",
		description = "Supprime un produit du système",
		responses = {
			@ApiResponse(responseCode = "204", description = "Produit supprimé avec succès"),
			@ApiResponse(responseCode = "404", description = "Produit non trouvé"),
			@ApiResponse(responseCode = "403", description = "Accès refusé - Rôle ADMIN requis")
		}
	)
	public ResponseEntity<Void> delete(
		@Parameter(description = "ID du produit") @PathVariable UUID id) {
		productService.delete(id);
		return ResponseEntity.noContent().build();
	}
}