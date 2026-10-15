package com.waterbill.repository;

import com.waterbill.entity.WaterUsageLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaterUsageLogRepository extends JpaRepository<WaterUsageLog, Long> {

    List<WaterUsageLog> findByHouseholdIdOrderByReadingDateDesc(Long householdId);
    org.springframework.data.domain.Page<WaterUsageLog> findByHouseholdIdOrderByReadingDateDesc(Long householdId, Pageable pageable);
    org.springframework.data.domain.Page<WaterUsageLog> findByHouseholdApartmentIdOrderByReadingDateDesc(Long apartmentId, Pageable pageable);

    List<WaterUsageLog> findByHouseholdIdAndReadingDateBetweenOrderByReadingDateAsc(
            Long householdId, LocalDate startDate, LocalDate endDate);

    Optional<WaterUsageLog> findByHouseholdIdAndReadingDate(Long householdId, LocalDate readingDate);

    boolean existsByHouseholdIdAndReadingDate(Long householdId, LocalDate readingDate);

    @Query("SELECT COALESCE(SUM(l.dailyConsumptionKl), 0) FROM WaterUsageLog l " +
           "WHERE l.household.id = :householdId AND l.readingDate BETWEEN :startDate AND :endDate")
    BigDecimal sumConsumptionByHouseholdAndDateRange(
            @Param("householdId") Long householdId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT l FROM WaterUsageLog l WHERE l.household.id = :householdId " +
           "AND l.readingDate < :beforeDate ORDER BY l.readingDate DESC")
    List<WaterUsageLog> findHistoricalLogsBefore(
            @Param("householdId") Long householdId,
            @Param("beforeDate") LocalDate beforeDate,
            Pageable pageable);

    @Query("SELECT l FROM WaterUsageLog l WHERE l.household.apartment.id = :apartmentId " +
           "AND l.readingDate BETWEEN :startDate AND :endDate ORDER BY l.readingDate ASC")
    List<WaterUsageLog> findByApartmentAndDateRange(
            @Param("apartmentId") Long apartmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    Optional<WaterUsageLog> findFirstByHouseholdIdAndReadingDateLessThanOrderByReadingDateDesc(
            Long householdId, LocalDate readingDate);

    List<WaterUsageLog> findTop50ByHouseholdApartmentIdOrderByReadingDateDesc(Long apartmentId);
}
