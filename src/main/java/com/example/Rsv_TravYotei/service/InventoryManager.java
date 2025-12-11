package com.example.Rsv_TravYotei.service;

import com.example.Rsv_TravYotei.model.Inventaire;
import com.example.Rsv_TravYotei.repository.InventaireRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryManager {

    private final InventaireRepository inventaireRepository;

    @Transactional
    @Retryable(retryFor = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public boolean checkAndLockSeats(String trajetId, int seatsRequired) {
        // Vérification initiale
        Optional<Inventaire> inventaireOpt = inventaireRepository.findById(trajetId);
        if (inventaireOpt.isEmpty()) {
            log.error("Inventaire non trouvé pour le trajet: {}", trajetId);
            return false;
        }

        Inventaire inventaire = inventaireOpt.get();
        if (inventaire.getSeatsAvailable() < seatsRequired) {
            log.warn("Places insuffisantes. Disponibles: {}, Requises: {}",
                    inventaire.getSeatsAvailable(), seatsRequired);
            return false;
        }

        // Tentative de blocage avec verrou pessimiste
        int updated = inventaireRepository.lockSeats(trajetId, seatsRequired);
        if (updated == 0) {
            log.error("Échec du blocage des places pour le trajet: {}", trajetId);
            return false;
        }

        log.info("{} places bloquées avec succès pour le trajet: {}", seatsRequired, trajetId);
        return true;
    }

    @Transactional
    public void releaseSeats(String trajetId, int seatsToRelease) {
        int updated = inventaireRepository.releaseSeats(trajetId, seatsToRelease);
        if (updated > 0) {
            log.info("{} places libérées pour le trajet: {}", seatsToRelease, trajetId);
        } else {
            log.warn("Échec de la libération des places pour le trajet: {}", trajetId);
        }
    }

    @Transactional
    public void confirmSeats(String trajetId, int seatsToConfirm) {
        int updated = inventaireRepository.confirmSeats(trajetId, seatsToConfirm);
        if (updated > 0) {
            log.info("{} places confirmées pour le trajet: {}", seatsToConfirm, trajetId);
        } else {
            log.warn("Échec de la confirmation des places pour le trajet: {}", trajetId);
        }
    }

    public Optional<Inventaire> getInventory(String trajetId) {
        return inventaireRepository.findById(trajetId);
    }

    public Inventaire createOrUpdateInventory(String trajetId, int totalSeats) {
        Optional<Inventaire> existing = inventaireRepository.findById(trajetId);
        if (existing.isPresent()) {
            Inventaire inventaire = existing.get();
            inventaire.setSeatsAvailable(totalSeats);
            return inventaireRepository.save(inventaire);
        } else {
            Inventaire inventaire = new Inventaire();
            inventaire.setTrajetId(trajetId);
            inventaire.setSeatsAvailable(totalSeats);
            inventaire.setSeatsLocked(0);
            inventaire.setVersion(0);
            return inventaireRepository.save(inventaire);
        }
    }
}