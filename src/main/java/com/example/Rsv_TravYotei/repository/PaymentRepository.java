package com.example.Rsv_TravYotei.repository;

import com.example.Rsv_TravYotei.model.Payment;
import com.example.Rsv_TravYotei.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByReservationId(String reservationId);

    List<Payment> findByStatus(PaymentStatus status);

    Optional<Payment> findByProviderRef(String providerRef);

    @Query("SELECT p FROM Payment p WHERE p.reservationId = :reservationId AND p.status = 'SUCCESS'")
    Optional<Payment> findSuccessfulPaymentByReservationId(@Param("reservationId") String reservationId);

    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :startDate AND p.createdAt < :endDate")
    List<Payment> findPaymentsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCESS' AND p.createdAt >= :startDate")
    Double findTotalAmountSince(@Param("startDate") LocalDateTime startDate);

    @Modifying
    @Query("UPDATE Payment p SET p.status = :newStatus WHERE p.id = :paymentId")
    int updatePaymentStatus(@Param("paymentId") String paymentId,
                            @Param("newStatus") PaymentStatus newStatus);
}