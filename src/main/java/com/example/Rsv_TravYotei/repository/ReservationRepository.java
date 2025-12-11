package com.example.Rsv_TravYotei.repository;

import com.example.Rsv_TravYotei.model.Reservation;
import com.example.Rsv_TravYotei.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {
    List<Reservation> findByClientId(String clientId);
    List<Reservation> findByTransportId(String transportId);
    List<Reservation> findByStatus(ReservationStatus status);
    List<Reservation> findByStatusAndTtlExpiryBefore(ReservationStatus status, LocalDateTime expiryTime);
    Optional<Reservation> findByOperationToken(String operationToken);

    @Modifying
    @Query("UPDATE Reservation r SET r.status = 'CANCELLED' WHERE r.status = 'PENDING' AND r.ttlExpiry < :now")
    int expirePendingReservations(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.transportId = :transportId AND r.status = :status")
    Long countByTransportIdAndStatus(@Param("transportId") String transportId,
                                     @Param("status") ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.clientId = :clientId AND r.status IN :statuses ORDER BY r.createdAt DESC")
    List<Reservation> findByClientIdAndStatusIn(@Param("clientId") String clientId,
                                                @Param("statuses") List<ReservationStatus> statuses);
}