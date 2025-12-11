package com.example.Rsv_TravYotei.repository;

import com.example.Rsv_TravYotei.model.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrajetRepository extends JpaRepository<Trajet, String> {
    List<Trajet> findByDepartCityAndArrivalCityAndDepartTimeAfter(
            String departCity, String arrivalCity, LocalDateTime departTime);

    List<Trajet> findByIsActiveTrue();

    List<Trajet> findByDepartCityAndArrivalCity(String departCity, String arrivalCity);

    @Query("SELECT t FROM Trajet t WHERE t.departCity = :departCity AND t.arrivalCity = :arrivalCity " +
            "AND t.departTime >= :startDate AND t.departTime < :endDate AND t.isActive = true")
    List<Trajet> findTrajetsByCriteria(@Param("departCity") String departCity,
                                       @Param("arrivalCity") String arrivalCity,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Trajet t WHERE t.departTime >= :now AND t.isActive = true ORDER BY t.departTime")
    List<Trajet> findUpcomingTrajets(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(t) FROM Trajet t WHERE t.isActive = true")
    Long countActiveTrajets();
}