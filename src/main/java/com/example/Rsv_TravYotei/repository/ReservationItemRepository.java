package com.example.Rsv_TravYotei.repository;

import com.example.Rsv_TravYotei.model.ReservationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationItemRepository extends JpaRepository<ReservationItem, String> {
    List<ReservationItem> findByReservationId(String reservationId);

    @Query("SELECT COUNT(ri) FROM ReservationItem ri WHERE ri.reservationId = :reservationId")
    Integer countByReservationId(@Param("reservationId") String reservationId);

    @Query("SELECT ri FROM ReservationItem ri WHERE ri.reservationId IN :reservationIds")
    List<ReservationItem> findByReservationIds(@Param("reservationIds") List<String> reservationIds);

    @Query("SELECT ri.seatNumber FROM ReservationItem ri WHERE ri.reservationId = :reservationId ORDER BY ri.seatNumber")
    List<Integer> findSeatNumbersByReservationId(@Param("reservationId") String reservationId);

    boolean existsByReservationIdAndSeatNumber(String reservationId, Integer seatNumber);
}