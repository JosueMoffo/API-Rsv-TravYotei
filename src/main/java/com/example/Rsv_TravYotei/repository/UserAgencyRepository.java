package com.example.Rsv_TravYotei.repository;

import com.example.Rsv_TravYotei.model.UserAgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAgencyRepository extends JpaRepository<UserAgency, String> {
    Optional<UserAgency> findByNameAgency(String nameAgency);

    @Query("SELECT ua FROM UserAgency ua WHERE LOWER(ua.nameAgency) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<UserAgency> findByNameAgencyContainingIgnoreCase(@Param("name") String name);

    boolean existsByNameAgency(String nameAgency);

    @Query("SELECT COUNT(ua) FROM UserAgency ua")
    Long countAllAgencies();
}