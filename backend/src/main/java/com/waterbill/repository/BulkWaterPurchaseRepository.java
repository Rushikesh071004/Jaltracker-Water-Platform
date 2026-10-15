package com.waterbill.repository;

import com.waterbill.entity.BulkWaterPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BulkWaterPurchaseRepository extends JpaRepository<BulkWaterPurchase, Long> {
    List<BulkWaterPurchase> findByBillingCycleId(Long billingCycleId);
    List<BulkWaterPurchase> findByApartmentIdOrderByPurchaseDateDesc(Long apartmentId);
}
