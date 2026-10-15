package com.waterbill.repository;

import com.waterbill.entity.Alert;
import com.waterbill.entity.AlertStatus;
import com.waterbill.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByHouseholdIdOrderByReadingDateDesc(Long householdId);
    List<Alert> findByHouseholdApartmentIdOrderByReadingDateDesc(Long apartmentId);
    List<Alert> findByHouseholdApartmentIdAndStatusOrderByReadingDateDesc(Long apartmentId, AlertStatus status);
    List<Alert> findByHouseholdIdAndStatusOrderByReadingDateDesc(Long householdId, AlertStatus status);
    boolean existsByHouseholdIdAndReadingDateAndAlertType(Long householdId, LocalDate readingDate, AlertType alertType);
}
