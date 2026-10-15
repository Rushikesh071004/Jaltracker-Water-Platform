package com.waterbill.repository;

import com.waterbill.entity.BillingCycle;
import com.waterbill.entity.CycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillingCycleRepository extends JpaRepository<BillingCycle, Long> {
    List<BillingCycle> findByApartmentIdOrderByStartDateDesc(Long apartmentId);
    List<BillingCycle> findByApartmentIdAndStatus(Long apartmentId, CycleStatus status);
    Optional<BillingCycle> findFirstByApartmentIdAndStatusOrderByStartDateDesc(Long apartmentId, CycleStatus status);
}
