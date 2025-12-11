package com.example.Rsv_TravYotei.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "trajet")
@Data
public class Trajet {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "depart_city", nullable = false, length = 100)
    private String departCity;

    @Column(name = "arrival_city", nullable = false, length = 100)
    private String arrivalCity;

    @Column(name = "depart_time", nullable = false)
    private LocalDateTime departTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "price_per_seat", nullable = false)
    private Double pricePerSeat;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
    }
}