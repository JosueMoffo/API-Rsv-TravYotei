package com.example.Rsv_TravYotei.config;

import com.example.Rsv_TravYotei.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class SchedulerConfig {
    
    private final ReservationService reservationService;
    
    @Scheduled(fixedRate = 120000) // Toutes les 2 min
    public void checkExpiredReservations() {
        log.debug("Exécution du scheduler d'expiration des réservations");
        try {
            reservationService.expirePendingReservations();
        } catch (Exception e) {
            log.error("Erreur lors de la vérification des réservations expirées", e);
        }
    }
}