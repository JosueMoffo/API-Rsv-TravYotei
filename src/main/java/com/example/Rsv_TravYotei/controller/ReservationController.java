package com.example.Rsv_TravYotei.controller;

import com.example.Rsv_TravYotei.model.dto.CreateReservationRequest;
import com.example.Rsv_TravYotei.model.Reservation;
import com.example.Rsv_TravYotei.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Validated
@Tag(name = "Réservations", description = "API de gestion des réservations")
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "Obtenir toutes les réservations", description = "Récupère la liste de toutes les réservations")
    @ApiResponse(responseCode = "200", description = "Liste des réservations récupérée avec succès")
    @GetMapping
    public List<Reservation> getAll() {
        return reservationService.getAll();
    }

    @Operation(summary = "Obtenir une réservation par ID", description = "Récupère une réservation spécifique par son identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Réservation trouvée"),
            @ApiResponse(responseCode = "404", description = "Réservation non trouvée")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getOne(
            @Parameter(description = "ID de la réservation", required = true)
            @PathVariable UUID id) {
        try {
            Reservation reservation = reservationService.getById(String.valueOf(id));
            return ResponseEntity.ok(reservation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Créer une nouvelle réservation", description = "Crée une réservation avec statut PENDING")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Réservation créée avec succès",
                    content = @Content(schema = @Schema(implementation = Reservation.class))),
            @ApiResponse(responseCode = "400", description = "Requête invalide")
    })
    @PostMapping
    public ResponseEntity<Reservation> create(
            @Parameter(description = "Données de la réservation", required = true)
            @Valid @RequestBody CreateReservationRequest request) {
        try {
            Reservation reservation = reservationService.createReservation(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Confirmer une réservation", description = "Confirme une réservation PENDING")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Réservation confirmée"),
            @ApiResponse(responseCode = "400", description = "Impossible de confirmer la réservation"),
            @ApiResponse(responseCode = "404", description = "Réservation non trouvée")
    })
    @PostMapping("/{id}/confirm")
    public ResponseEntity<Reservation> confirm(
            @Parameter(description = "ID de la réservation à confirmer", required = true)
            @PathVariable UUID id) {
        try {
            Reservation reservation = reservationService.confirmReservation(String.valueOf(id));
            return ResponseEntity.ok(reservation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Annuler une réservation", description = "Annule une réservation (PENDING ou CONFIRMED)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Réservation annulée"),
            @ApiResponse(responseCode = "400", description = "Impossible d'annuler la réservation"),
            @ApiResponse(responseCode = "404", description = "Réservation non trouvée")
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Reservation> cancel(
            @Parameter(description = "ID de la réservation à annuler", required = true)
            @PathVariable UUID id) {
        try {
            Reservation reservation = reservationService.cancelReservation(String.valueOf(id));
            return ResponseEntity.ok(reservation);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Obtenir les réservations par client", description = "Récupère toutes les réservations d'un client")
    @ApiResponse(responseCode = "200", description = "Liste des réservations du client")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Reservation>> getByClient(
            @Parameter(description = "ID du client", required = true)
            @PathVariable String clientId) {
        try {
            List<Reservation> reservations = reservationService.getByClientId(clientId);
            return ResponseEntity.ok(reservations);
        } catch (RuntimeException e) {
            return ResponseEntity.ok(List.of());
        }
    }
}