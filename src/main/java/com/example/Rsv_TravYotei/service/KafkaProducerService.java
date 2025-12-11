package com.example.Rsv_TravYotei.service;

import com.example.Rsv_TravYotei.model.Reservation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendReservationCreatedEvent(Reservation reservation) {
        sendEvent("reservation-created", reservation, "création de réservation");
    }

    public void sendReservationConfirmedEvent(Reservation reservation) {
        sendEvent("reservation-confirmed", reservation, "confirmation de réservation");
    }

    public void sendReservationCancelledEvent(Reservation reservation) {
        sendEvent("reservation-cancelled", reservation, "annulation de réservation");
    }

    private void sendEvent(String topic, Reservation reservation, String eventType) {
        try {
            String reservationJson = objectMapper.writeValueAsString(reservation);
            String key = reservation.getId();

            log.info("📤 Envoi Kafka - Topic: '{}', Key: {}", topic, key);

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(topic, key, reservationJson);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Kafka: Événement '{}' envoyé avec succès pour {}", topic, key);
                } else {
                    log.error("❌ Kafka: Échec envoi '{}' pour {}: {}", topic, key, ex.getMessage());
                }
            });

        } catch (JsonProcessingException e) {
            log.error("❌ Erreur sérialisation JSON pour l'événement '{}': {}", eventType, e.getMessage());
        }
    }
}