package com.example.Rsv_TravYotei.service;

import com.example.Rsv_TravYotei.model.dto.PaymentConfirmationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConfirmationConsumer {

    private final ReservationService reservationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "payment-confirmed",
            groupId = "reservation-service-group",
            containerFactory = "stringKafkaListenerContainerFactory"
    )
    @Transactional
    public void handlePaymentConfirmation(String eventJson, @Header(KafkaHeaders.RECEIVED_TIMESTAMP) Long timestamp) {
        String reservationId = null;

        try {
            long messageAge = System.currentTimeMillis() - timestamp;

            // IGNORER les messages de plus de 5 minutes
            if (messageAge > 5 * 60 * 1000) {
                log.debug("⏭️ [CONFIRMATION] Message ignoré (trop ancien: {} ms)", messageAge);
                return;
            }

            log.info("📨 [CONFIRMATION] Événement reçu sur 'payment-confirmed' (âge: {} ms)", messageAge);

            // Désérialiser avec le DTO
            PaymentConfirmationEvent event = objectMapper.readValue(
                    eventJson,
                    PaymentConfirmationEvent.class
            );

            reservationId = event.getReservationId();

            log.info("✅ [CONFIRMATION] Traitement réservation {}", reservationId);
            log.info("   → Statut: {}", event.getStatus());
            log.info("   → Montant: {}€", event.getAmount());
            log.info("   → Transaction: {}", event.getTransactionId());
            log.info("   → Timestamp: {}", event.getTimestamp());

            if (!"CONFIRMED".equals(event.getStatus())) {
                log.warn("⚠️ [CONFIRMATION] Statut non CONFIRMED: {}", event.getStatus());
                return;
            }

            // Confirmer la réservation
            var reservation = reservationService.confirmReservation(reservationId);

            log.info("🎉 [CONFIRMATION] Réservation {} CONFIRMÉE avec succès", reservationId);
            log.info("   → Nouveau statut: {}", reservation.getStatus());

        } catch (JsonProcessingException e) {
            log.error("❌ [CONFIRMATION] Erreur parsing JSON: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ [CONFIRMATION] Erreur confirmation réservation {}: {}",
                    reservationId, e.getMessage());
            // Ne pas relancer l'exception pour éviter les boucles de retry
        }
    }
}