package com.waterbill.repository;

import com.waterbill.entity.WaterMeter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WaterMeterRepository extends JpaRepository<WaterMeter, Long> {
    Optional<WaterMeter> findByMeterSerialNumber(String meterSerialNumber);
    Optional<WaterMeter> findByHouseholdId(Long householdId);
    boolean existsByMeterSerialNumber(String meterSerialNumber);
}
