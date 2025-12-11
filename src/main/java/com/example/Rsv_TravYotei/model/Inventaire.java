package com.example.Rsv_TravYotei.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventaire")
@Data
public class Inventaire {

    @Id
    @Column(name = "trajet_id", columnDefinition = "CHAR(36)")
    private String trajetId;

    @Column(name = "seats_available", nullable = false)
    private Integer seatsAvailable;

    @Column(name = "seats_locked", nullable = false)
    private Integer seatsLocked = 0;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "trajet_id")
    private Trajet trajet;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}