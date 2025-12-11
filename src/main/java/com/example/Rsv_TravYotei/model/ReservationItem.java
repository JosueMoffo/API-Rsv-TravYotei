package com.example.Rsv_TravYotei.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_items")
@Data
public class ReservationItem {

    @Id
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "reservation_id", nullable = false, columnDefinition = "CHAR(36)")
    private String reservationId;

    @Column(name = "seat_number")
    private Integer seatNumber;

    @Column(name = "passenger_name", length = 255)
    private String passengerName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", insertable = false, updatable = false)
    private Reservation reservation;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
    }
}