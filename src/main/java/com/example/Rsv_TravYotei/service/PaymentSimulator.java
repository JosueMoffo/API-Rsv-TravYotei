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
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
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
        log.info("   → Ne traite QUE les messages FUTURS (offset: latest)");
        log.info("   → Ignore les anciennes réservations");
    }

    @KafkaListener(
            topics = "reservation-created",
            groupId = "payment-simulator-group",
            containerFactory = "stringKafkaListenerContainerFactory"
    )
    @Async
    public void simulatePayment(String reservationJson, @Header(KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp) {
        try {
            long messageAge = System.currentTimeMillis() - timestamp;

            // IGNORER les messages de plus de 5 minutes
            if (messageAge > 5 * 60 * 1000) { // 5 minutes
                log.debug("⏭️ [SIMULATEUR] Message ignoré (trop ancien: {} ms)", messageAge);
                return;
            }

            log.info("📨 [SIMULATEUR] Message reçu sur 'reservation-created'");

            Reservation reservation = objectMapper.readValue(reservationJson, Reservation.class);
            String reservationId = reservation.getId();

            // VÉRIFIER immédiatement si la réservation est toujours valide
            var reservationOpt = reservationRepository.findById(reservationId);
            if (reservationOpt.isEmpty()) {
                log.warn("⚠️ [SIMULATEUR] Réservation {} introuvable en base", reservationId);
                return;
            }

            var reservationInDb = reservationOpt.get();

            // Vérifier le statut ACTUEL en base
            if (reservationInDb.getStatus() != ReservationStatus.PENDING) {
                log.warn("⚠️ [SIMULATEUR] Réservation {} déjà traitée (statut: {})",
                        reservationId, reservationInDb.getStatus());
                return;
            }

            // Vérifier qu'elle n'est pas expirée
            if (reservationInDb.getTtlExpiry() != null &&
                    reservationInDb.getTtlExpiry().isBefore(java.time.LocalDateTime.now())) {
                log.warn("⚠️ [SIMULATEUR] Réservation {} expirée à {}",
                        reservationId, reservationInDb.getTtlExpiry());
                return;
            }

            log.info("💰 [SIMULATEUR] Traitement réservation {}", reservationId);
            log.info("   → Client: {}", reservation.getClientId());
            log.info("   → Montant: {}€", reservation.getTotalAmount());
            log.info("   → Expire à: {}", reservationInDb.getTtlExpiry());
            log.info("   → Âge message: {} ms", messageAge);

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
            log.info("🔍 [SIMULATEUR] Vérification finale réservation {}", reservationId);

            var reservationOpt = reservationRepository.findById(reservationId);
            if (reservationOpt.isEmpty()) {
                log.warn("⚠️ [SIMULATEUR] Réservation {} introuvable en base", reservationId);
                return;
            }

            var reservation = reservationOpt.get();

            // DOUBLE VÉRIFICATION du statut
            if (reservation.getStatus() != ReservationStatus.PENDING) {
                log.warn("⚠️ [SIMULATEUR] Réservation {} déjà traitée (statut: {})",
                        reservationId, reservation.getStatus());
                return;
            }

            // Vérifier qu'elle n'est pas expirée
            if (reservation.getTtlExpiry() != null &&
                    reservation.getTtlExpiry().isBefore(java.time.LocalDateTime.now())) {
                log.warn("⚠️ [SIMULATEUR] Réservation {} expirée à {}",
                        reservationId, reservation.getTtlExpiry());
                return;
            }

            log.info("✅ [SIMULATEUR] Réservation {} valide, création événement paiement...", reservationId);

            // Créer l'événement de confirmation
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
                            log.info("   → Topic: {}", result.getRecordMetadata().topic());
                            log.info("   → Partition: {}", result.getRecordMetadata().partition());
                            log.info("   → Offset: {}", result.getRecordMetadata().offset());
                        } else {
                            log.error("❌ [SIMULATEUR] Erreur envoi Kafka: {}", ex.getMessage());
                        }
                    });

        } catch (Exception e) {
            log.error("❌ [SIMULATEUR] Erreur traitement confirmation paiement", e);
        }
    }
}