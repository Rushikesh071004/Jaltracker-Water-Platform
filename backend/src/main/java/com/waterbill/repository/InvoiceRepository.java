package com.waterbill.repository;

import com.waterbill.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByBillingCycleId(Long billingCycleId);
    List<Invoice> findByHouseholdIdOrderByCreatedAtDesc(Long householdId);
    Page<Invoice> findByHouseholdIdOrderByCreatedAtDesc(Long householdId, Pageable pageable);
    Optional<Invoice> findByBillingCycleIdAndHouseholdId(Long billingCycleId, Long householdId);
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findByHouseholdApartmentIdOrderByCreatedAtDesc(Long apartmentId);
    Page<Invoice> findByHouseholdApartmentIdOrderByCreatedAtDesc(Long apartmentId, Pageable pageable);
}
