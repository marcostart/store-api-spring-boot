package marcostar.project.store_project.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import marcostar.project.store_project.dtos.unit.UnitConversionRequest;
import marcostar.project.store_project.dtos.unit.UnitConversionResponse;
import marcostar.project.store_project.dtos.unit.UnitRequest;
import marcostar.project.store_project.dtos.unit.UnitResponse;
import marcostar.project.store_project.entities.Unit;
import marcostar.project.store_project.entities.enums.UnitCategory;
import marcostar.project.store_project.services.UnitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
@Tag(name = "Units", description = "Endpoints pour la gestion des unités de mesure et leurs conversions")
@SecurityRequirement(name = "bearerAuth")
public class UnitController {

    private final UnitService unitService;

    @GetMapping
    @Operation(
        summary = "Liste toutes les unités",
        description = "Récupère toutes les unités de mesure. Peut être filtré par catégorie (WEIGHT, VOLUME, QUANTITY)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Liste des unités récupérée avec succès"),
            @ApiResponse(responseCode = "401", description = "Non authentifié")
        }
    )
    public ResponseEntity<List<UnitResponse>> getAllUnits(
            @Parameter(description = "Filtrer par catégorie (WEIGHT, VOLUME, QUANTITY)")
            @RequestParam(required = false) UnitCategory category) {
        if (category != null) {
            return ResponseEntity.ok(unitService.getUnitsByCategory(category));
        }
        return ResponseEntity.ok(unitService.getAllUnits());
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Récupère une unité par ID",
        description = "Retourne les détails d'une unité spécifique",
        responses = {
            @ApiResponse(responseCode = "200", description = "Unité trouvée"),
            @ApiResponse(responseCode = "404", description = "Unité non trouvée")
        }
    )
    public ResponseEntity<UnitResponse> getUnitById(
        @Parameter(description = "ID de l'unité") @PathVariable UUID id) {
        return ResponseEntity.ok(unitService.getUnitById(id));
    }

    @PostMapping
    @Operation(
        summary = "Créer une nouvelle unité",
        description = "Crée une nouvelle unité de mesure. Nécessite le rôle ADMIN",
        responses = {
            @ApiResponse(responseCode = "201", description = "Unité créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou code déjà existant"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
        }
    )
	@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnitResponse> createUnit(@Valid @RequestBody UnitRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(unitService.createUnit(request));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Mettre à jour une unité",
        description = "Met à jour une unité existante. Nécessite le rôle ADMIN",
        responses = {
            @ApiResponse(responseCode = "200", description = "Unité mise à jour avec succès"),
            @ApiResponse(responseCode = "404", description = "Unité non trouvée"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
        }
    )
	@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnitResponse> updateUnit(
            @Parameter(description = "ID de l'unité") @PathVariable UUID id,
            @Valid @RequestBody UnitRequest request) {
        return ResponseEntity.ok(unitService.updateUnit(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Supprimer une unité",
        description = "Supprime une unité du système. Nécessite le rôle ADMIN. Attention: cela peut affecter les produits utilisant cette unité",
        responses = {
            @ApiResponse(responseCode = "204", description = "Unité supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Unité non trouvée"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
        }
    )
	@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUnit(
        @Parameter(description = "ID de l'unité") @PathVariable UUID id) {
        unitService.deleteUnit(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conversions")
    @Operation(
        summary = "Liste toutes les conversions",
        description = "Récupère toutes les conversions d'unités configurées",
        responses = {
            @ApiResponse(responseCode = "200", description = "Liste des conversions récupérée avec succès")
        }
    )
    public ResponseEntity<List<UnitConversionResponse>> getAllConversions() {
        return ResponseEntity.ok(unitService.getAllConversions());
    }

    @GetMapping("/conversions/{id}")
    @Operation(
        summary = "Récupère une conversion par ID",
        description = "Retourne les détails d'une conversion spécifique",
        responses = {
            @ApiResponse(responseCode = "200", description = "Conversion trouvée"),
            @ApiResponse(responseCode = "404", description = "Conversion non trouvée")
        }
    )
    public ResponseEntity<UnitConversionResponse> getConversionById(
        @Parameter(description = "ID de la conversion") @PathVariable UUID id) {
        return ResponseEntity.ok(unitService.getConversionById(id));
    }

    @PostMapping("/conversions")
    @Operation(
        summary = "Créer une nouvelle conversion",
        description = "Crée une nouvelle règle de conversion entre deux unités. Nécessite le rôle ADMIN. Les unités doivent être de la même catégorie",
        responses = {
            @ApiResponse(responseCode = "201", description = "Conversion créée avec succès"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou unités de catégories différentes"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
        }
    )
	@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnitConversionResponse> createConversion(
            @Valid @RequestBody UnitConversionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(unitService.createConversion(request));
    }

    @PutMapping("/conversions/{id}")
    @Operation(
        summary = "Mettre à jour une conversion",
        description = "Met à jour le facteur de conversion entre deux unités. Nécessite le rôle ADMIN",
        responses = {
            @ApiResponse(responseCode = "200", description = "Conversion mise à jour avec succès"),
            @ApiResponse(responseCode = "404", description = "Conversion non trouvée"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
        }
    )
	@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UnitConversionResponse> updateConversion(
            @Parameter(description = "ID de la conversion") @PathVariable UUID id,
            @Valid @RequestBody UnitConversionRequest request) {
        return ResponseEntity.ok(unitService.updateConversion(id, request));
    }

    @DeleteMapping("/conversions/{id}")
    @Operation(
        summary = "Supprimer une conversion",
        description = "Supprime une règle de conversion. Nécessite le rôle ADMIN",
        responses = {
            @ApiResponse(responseCode = "204", description = "Conversion supprimée avec succès"),
            @ApiResponse(responseCode = "404", description = "Conversion non trouvée"),
            @ApiResponse(responseCode = "403", description = "Accès refusé")
        }
    )
	@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteConversion(
        @Parameter(description = "ID de la conversion") @PathVariable UUID id) {
        unitService.deleteConversion(id);
        return ResponseEntity.noContent().build();
    }
}
