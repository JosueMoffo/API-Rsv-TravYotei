package com.example.Rsv_TravYotei.service;

import com.example.Rsv_TravYotei.model.dto.PaymentConfirmationEvent;
import com.example.Rsv_TravYotei.model.Reservation;
import com.example.Rsv_TravYotei.model.ReservationStatus;
import com.example.Rsv_TravYotei.repository.ReservationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(
        value = "payment.simulator.enabled",
        havingValue = "true",
        matchIfMissing = false
)
@RequiredArgsConstructor
@Slf4j
public class PaymentSimulator {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ReservationRepository reservationRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    @PostConstruct
    public void init() {
        log.info("💰 PAYMENT SIMULATOR INITIALISÉ");
        log.info("   → Topics écoutés: reservation-created");
        log.info("   → Topics émis: payment-confirmed");
        log.info("   → Délai: 30 secondes");
        log.info("   → Group ID: payment-simulator-group");
    }

    @KafkaListener(
            topics = "reservation-created",
            groupId = "payment-simulator-group",
            containerFactory = "stringKafkaListenerContainerFactory"
    )
    @Async
    public void simulatePayment(String reservationJson) {
        try {
            log.info("📨 [SIMULATEUR] Message reçu sur 'reservation-created'");

            Reservation reservation = objectMapper.readValue(reservationJson, Reservation.class);
            String reservationId = reservation.getId();

            log.info("💰 [SIMULATEUR] Traitement réservation {}", reservationId);
            log.info("   → Client: {}", reservation.getClientId());
            log.info("   → Montant: {}€", reservation.getTotalAmount());
            log.info("   → Statut actuel: {}", reservation.getStatus());
            log.info("   → Expire à: {}", reservation.getTtlExpiry());

            log.info("⏳ [SIMULATEUR] Simulation paiement en cours (30s)...");

            // Planifier le traitement après 30 secondes
            scheduler.schedule(() -> {
                try {
                    log.info("🔄 [SIMULATEUR] Exécution traitement paiement pour {}", reservationId);
                    processPaymentConfirmation(reservationId);
                } catch (Exception e) {
                    log.error("❌ [SIMULATEUR] Erreur simulation paiement", e);
                }
            }, 30, TimeUnit.SECONDS);

        } catch (JsonProcessingException e) {
            log.error("❌ [SIMULATEUR] Erreur parsing JSON: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ [SIMULATEUR] Erreur inattendue", e);
        }
    }

    private void processPaymentConfirmation(String reservationId) {
        try {
            log.info("🔍 [SIMULATEUR] Vérification réservation {}", reservationId);

            var reservationOpt = reservationRepository.findById(reservationId);
            if (reservationOpt.isEmpty()) {
                log.warn("⚠️ [SIMULATEUR] Réservation {} introuvable en base", reservationId);
                return;
            }

            var reservation = reservationOpt.get();
            ReservationStatus currentStatus = reservation.getStatus();

            if (currentStatus != ReservationStatus.PENDING) {
                log.warn("⚠️ [SIMULATEUR] Réservation {} déjà traitée (statut: {})",
                        reservationId, currentStatus);
                return;
            }

            // Vérifier qu'elle n'est pas expirée
            if (reservation.getTtlExpiry() != null &&
                    reservation.getTtlExpiry().isBefore(java.time.LocalDateTime.now())) {
                log.warn("⚠️ [SIMULATEUR] Réservation {} expirée", reservationId);
                return;
            }

            log.info("✅ [SIMULATEUR] Réservation {} valide, création événement paiement...", reservationId);

            // Créer l'événement de confirmation en utilisant le DTO
            PaymentConfirmationEvent event = PaymentConfirmationEvent.builder()
                    .reservationId(reservationId)
                    .status("CONFIRMED")
                    .amount(reservation.getTotalAmount())
                    .paymentMethod("CARTE_CREDIT")
                    .transactionId("PAY-SIM-" + System.currentTimeMillis())
                    .timestamp(java.time.LocalDateTime.now().toString())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);

            // Envoyer l'événement
            log.info("📤 [SIMULATEUR] Envoi vers topic 'payment-confirmed' pour {}", reservationId);
            kafkaTemplate.send("payment-confirmed", reservationId, eventJson)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("✅ [SIMULATEUR] Paiement simulé ENVOYÉ pour {}", reservationId);
                        } else {
                            log.error("❌ [SIMULATEUR] Erreur envoi Kafka: {}", ex.getMessage());
                        }
                    });

        } catch (Exception e) {
            log.error("❌ [SIMULATEUR] Erreur traitement confirmation paiement", e);
        }
    }
}