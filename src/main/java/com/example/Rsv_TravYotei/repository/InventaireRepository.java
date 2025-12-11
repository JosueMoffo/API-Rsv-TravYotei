package com.example.Rsv_TravYotei.repository;

import com.example.Rsv_TravYotei.model.Inventaire;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventaireRepository extends JpaRepository<Inventaire, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Inventaire> findWithLockingByTrajetId(String trajetId);

    @Modifying
    @Query("UPDATE Inventaire i SET i.seatsAvailable = i.seatsAvailable - :seats, " +
            "i.seatsLocked = i.seatsLocked + :seats, i.version = i.version + 1 " +
            "WHERE i.trajetId = :trajetId AND i.seatsAvailable >= :seats")
    int lockSeats(@Param("trajetId") String trajetId, @Param("seats") Integer seats);

    @Modifying
    @Query("UPDATE Inventaire i SET i.seatsLocked = i.seatsLocked - :seats, " +
            "i.seatsAvailable = i.seatsAvailable + :seats, i.version = i.version + 1 " +
            "WHERE i.trajetId = :trajetId AND i.seatsLocked >= :seats")
    int releaseSeats(@Param("trajetId") String trajetId, @Param("seats") Integer seats);

    @Modifying
    @Query("UPDATE Inventaire i SET i.seatsLocked = i.seatsLocked - :seats, " +
            "i.version = i.version + 1 WHERE i.trajetId = :trajetId")
    int confirmSeats(@Param("trajetId") String trajetId, @Param("seats") Integer seats);

    @Query("SELECT i.seatsAvailable FROM Inventaire i WHERE i.trajetId = :trajetId")
    Optional<Integer> findAvailableSeatsByTrajetId(@Param("trajetId") String trajetId);

    @Query("SELECT i.seatsAvailable + i.seatsLocked FROM Inventaire i WHERE i.trajetId = :trajetId")
    Optional<Integer> findTotalOccupiedSeatsByTrajetId(@Param("trajetId") String trajetId);

    @Modifying
    @Query("UPDATE Inventaire i SET i.seatsAvailable = :seatsAvailable, " +
            "i.version = i.version + 1 WHERE i.trajetId = :trajetId")
    int updateAvailableSeats(@Param("trajetId") String trajetId,
                             @Param("seatsAvailable") Integer seatsAvailable);
}