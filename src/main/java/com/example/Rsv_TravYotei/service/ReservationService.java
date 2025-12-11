package com.example.Rsv_TravYotei.service;

import com.example.Rsv_TravYotei.model.*;
import com.example.Rsv_TravYotei.model.dto.CreateReservationRequest;
import com.example.Rsv_TravYotei.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final InventoryManager inventoryManager;
    private final TrajetRepository trajetRepository;
    private final KafkaProducerService kafkaProducerService;

    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    public Reservation getById(String id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
    }

    @Transactional
    public Reservation createReservation(CreateReservationRequest request) {
        log.info("🚀 Création réservation pour trajet: {}", request.getTransportId());

        // 1. Vérifier l'inventaire
        int seatsRequired = request.getPassengers().size();
        boolean seatsAvailable = inventoryManager.checkAndLockSeats(
                request.getTransportId(), seatsRequired);

        if (!seatsAvailable) {
            throw new RuntimeException("Places insuffisantes pour ce trajet");
        }

        // 2. Récupérer le trajet
        Trajet trajet = trajetRepository.findById(request.getTransportId())
                .orElseThrow(() -> new RuntimeException("Trajet non trouvé"));

        // 3. Créer la réservation
        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID().toString());
        reservation.setClientId(request.getClientId());
        reservation.setTransportId(request.getTransportId());
        reservation.setStatus(ReservationStatus.PENDING);

        double totalAmount = trajet.getPricePerSeat() * seatsRequired;
        reservation.setTotalAmount(totalAmount);

        // Expiration en 2 minutes pour les tests
        reservation.setTtlExpiry(LocalDateTime.now().plusMinutes(2));

        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("📝 Réservation créée en base: {}", savedReservation.getId());

        // 4. Créer les items
        for (CreateReservationRequest.PassengerInfo passenger : request.getPassengers()) {
            ReservationItem item = new ReservationItem();
            item.setId(UUID.randomUUID().toString());
            item.setReservationId(savedReservation.getId());
            item.setPassengerName(passenger.getName());
            item.setSeatNumber(passenger.getSeatNumber());
            reservationItemRepository.save(item);
        }

        // 5. Émettre l'événement Kafka (sérialisation manuelle en String)
        kafkaProducerService.sendReservationCreatedEvent(savedReservation);

        log.info("✅ Réservation {} créée avec succès!", savedReservation.getId());
        return savedReservation;
    }

    @Transactional
    public Reservation confirmReservation(String reservationId) {
        Reservation reservation = getById(reservationId);

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("Seules les réservations PENDING peuvent être confirmées");
        }

        // Confirmer les places dans l'inventaire
        List<ReservationItem> items = reservationItemRepository.findByReservationId(reservationId);
        inventoryManager.confirmSeats(reservation.getTransportId(), items.size());

        // Mettre à jour le statut
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setTtlExpiry(null);

        Reservation confirmedReservation = reservationRepository.save(reservation);

        // Émettre l'événement Kafka
        kafkaProducerService.sendReservationConfirmedEvent(confirmedReservation);

        log.info("✅ Réservation {} confirmée", reservationId);
        return confirmedReservation;
    }

    @Transactional
    public Reservation cancelReservation(String reservationId) {
        Reservation reservation = getById(reservationId);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return reservation;
        }

        // Libérer les places si PENDING
        if (reservation.getStatus() == ReservationStatus.PENDING) {
            List<ReservationItem> items = reservationItemRepository.findByReservationId(reservationId);
            inventoryManager.releaseSeats(reservation.getTransportId(), items.size());
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation cancelledReservation = reservationRepository.save(reservation);

        // Émettre l'événement Kafka
        kafkaProducerService.sendReservationCancelledEvent(cancelledReservation);

        log.info("Réservation annulée: {}", reservationId);
        return cancelledReservation;
    }

    @Transactional
    public void expirePendingReservations() {
        log.info("⏰ Vérification des réservations PENDING expirées");

        try {
            LocalDateTime now = LocalDateTime.now();
            List<Reservation> expiredReservations = reservationRepository
                    .findByStatusAndTtlExpiryBefore(ReservationStatus.PENDING, now);

            if (expiredReservations.isEmpty()) {
                log.debug("✅ Aucune réservation à expirer");
                return;
            }

            log.info("🔍 {} réservation(s) PENDING expirée(s) trouvée(s)", expiredReservations.size());

            for (Reservation reservation : expiredReservations) {
                try {
                    log.info("⌛ Expiration automatique de la réservation {}", reservation.getId());

                    // Option 1: Changer directement le statut
                    reservation.setStatus(ReservationStatus.EXPIRED);
                    reservationRepository.save(reservation);

                    // Libérer les places
                    List<ReservationItem> items = reservationItemRepository.findByReservationId(reservation.getId());
                    inventoryManager.releaseSeats(reservation.getTransportId(), items.size());

                    // Envoyer l'événement
                    kafkaProducerService.sendReservationCancelledEvent(reservation);

                    log.info("✅ Réservation {} expirée automatiquement", reservation.getId());

                } catch (Exception e) {
                    log.error("❌ Erreur expiration réservation {}: {}", reservation.getId(), e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("❌ Erreur lors du traitement des expirations: {}", e.getMessage());
        }
    }

    public List<Reservation> getByClientId(String clientId) {
        return reservationRepository.findByClientId(clientId);
    }

    public List<Reservation> getByTransportId(String transportId) {
        return reservationRepository.findByTransportId(transportId);
    }

    public List<Reservation> getByStatus(ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }
}