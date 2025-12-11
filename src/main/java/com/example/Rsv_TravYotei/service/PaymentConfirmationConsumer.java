package com.example.Rsv_TravYotei.service;

import com.example.Rsv_TravYotei.model.dto.PaymentConfirmationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
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
    public void handlePaymentConfirmation(String eventJson) {
        String reservationId = null;

        try {
            log.info("📨 [CONFIRMATION] Événement reçu sur 'payment-confirmed'");

            // Désérialiser avec le DTO
            PaymentConfirmationEvent event = objectMapper.readValue(
                    eventJson,
                    PaymentConfirmationEvent.class
            );

            reservationId = event.getReservationId();

            log.info("✅ [CONFIRMATION] Traitement réservation {}", reservationId);
            log.info("   → Statut: {}", event.getStatus());
            log.info("   → Montant: {}€", event.getAmount());
            log.info("   → Méthode: {}", event.getPaymentMethod());
            log.info("   → Transaction: {}", event.getTransactionId());
            log.info("   → Timestamp: {}", event.getTimestamp());

            if (!"CONFIRMED".equals(event.getStatus())) {
                log.warn("⚠️ [CONFIRMATION] Statut non CONFIRMED: {}", event.getStatus());
                return;
            }

            // Confirmer la réservation
            var reservation = reservationService.confirmReservation(reservationId);

            log.info("🎉 [CONFIRMATION] Réservation {} CONFIRMÉE avec succès", reservationId);

        } catch (JsonProcessingException e) {
            log.error("❌ [CONFIRMATION] Erreur parsing JSON: {}", e.getMessage());
            log.error("❌ JSON reçu: {}", eventJson);
        } catch (Exception e) {
            log.error("❌ [CONFIRMATION] Erreur confirmation réservation {}: {}",
                    reservationId, e.getMessage(), e);
        }
    }
}