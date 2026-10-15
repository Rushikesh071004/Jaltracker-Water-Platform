package com.waterbill.controller;

import com.waterbill.dto.ManualInvoiceRequest;
import com.waterbill.entity.*;
import com.waterbill.repository.ApartmentRepository;
import com.waterbill.repository.BillingCycleRepository;
import com.waterbill.repository.HouseholdRepository;
import com.waterbill.repository.InvoiceRepository;
import com.waterbill.repository.UserRepository;
import com.waterbill.service.EmailNotificationService;
import com.waterbill.service.PdfInvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final PdfInvoiceService pdfInvoiceService;
    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;
    private final BillingCycleRepository billingCycleRepository;
    private final EmailNotificationService emailNotificationService;

    public InvoiceController(InvoiceRepository invoiceRepository,
                             PdfInvoiceService pdfInvoiceService,
                             HouseholdRepository householdRepository,
                             UserRepository userRepository,
                             ApartmentRepository apartmentRepository,
                             BillingCycleRepository billingCycleRepository,
                             EmailNotificationService emailNotificationService) {
        this.invoiceRepository = invoiceRepository;
        this.pdfInvoiceService = pdfInvoiceService;
        this.householdRepository = householdRepository;
        this.userRepository = userRepository;
        this.apartmentRepository = apartmentRepository;
        this.billingCycleRepository = billingCycleRepository;
        this.emailNotificationService = emailNotificationService;
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + id));
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<Invoice>> getHouseholdInvoices(@RequestParam Long householdId) {
        return ResponseEntity.ok(invoiceRepository.findByHouseholdIdOrderByCreatedAtDesc(householdId));
    }

    @GetMapping("/admin/invoices")
    public ResponseEntity<List<Invoice>> getAdminInvoices(
            @RequestParam(required = false) Long apartmentId,
            @RequestParam(required = false) Long cycleId) {
        if (cycleId != null) {
            return ResponseEntity.ok(invoiceRepository.findByBillingCycleId(cycleId));
        } else if (apartmentId != null) {
            return ResponseEntity.ok(invoiceRepository.findByHouseholdApartmentIdOrderByCreatedAtDesc(apartmentId));
        }
        return ResponseEntity.ok(invoiceRepository.findAll());
    }

    @GetMapping("/admin/invoices/paged")
    public ResponseEntity<org.springframework.data.domain.Page<Invoice>> getAdminInvoicesPaged(
            @RequestParam(required = false) Long apartmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        if (apartmentId != null) {
            return ResponseEntity.ok(invoiceRepository.findByHouseholdApartmentIdOrderByCreatedAtDesc(apartmentId, pageable));
        }
        return ResponseEntity.ok(invoiceRepository.findAll(pageable));
    }

    @GetMapping(value = "/invoices/{id}/download", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) throws IOException {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + id));

        byte[] pdfBytes = pdfInvoiceService.generateInvoicePdf(invoice);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = "invoice_" + invoice.getInvoiceNumber() + ".pdf";
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PatchMapping("/invoices/{id}/status")
    public ResponseEntity<Invoice> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + id));

        String statusStr = payload.get("status");
        if (statusStr != null) {
            PaymentStatus newStatus = PaymentStatus.valueOf(statusStr.toUpperCase());
            invoice.setPaymentStatus(newStatus);
            if (newStatus == PaymentStatus.PAID) {
                invoice.setPaymentDate(OffsetDateTime.now());
            }
        }
        if (payload.containsKey("notes")) {
            invoice.setNotes(payload.get("notes"));
        }

        Invoice updated = invoiceRepository.save(invoice);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/admin/invoices/issue")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Invoice> issueManualInvoice(
            @Valid @RequestBody ManualInvoiceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Household household = null;
        if (request.getHouseholdId() != null) {
            household = householdRepository.findById(request.getHouseholdId())
                    .orElseThrow(() -> new IllegalArgumentException("Household not found with ID: " + request.getHouseholdId()));
        } else if (request.getResidentUserId() != null) {
            User resident = userRepository.findById(request.getResidentUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Resident user not found with ID: " + request.getResidentUserId()));
            household = resident.getHousehold();
            if (household == null) {
                Apartment apt = resident.getApartment() != null ? resident.getApartment() :
                        apartmentRepository.findAll().stream().findFirst().orElse(null);
                household = new Household(apt, "Flat-" + resident.getId(), new BigDecimal("1000.00"), true);
                household = householdRepository.save(household);
                resident.setHousehold(household);
                userRepository.save(resident);
            }
        } else {
            throw new IllegalArgumentException("Either householdId or residentUserId must be provided.");
        }

        Apartment apartment = household.getApartment();
        BillingCycle cycle = null;
        if (request.getBillingCycleId() != null) {
            cycle = billingCycleRepository.findById(request.getBillingCycleId()).orElse(null);
        }
        if (cycle == null && apartment != null) {
            cycle = billingCycleRepository.findFirstByApartmentIdAndStatusOrderByStartDateDesc(apartment.getId(), CycleStatus.OPEN)
                    .orElseGet(() -> {
                        List<BillingCycle> anyCycles = billingCycleRepository.findByApartmentIdOrderByStartDateDesc(apartment.getId());
                        if (!anyCycles.isEmpty()) {
                            return anyCycles.get(0);
                        }
                        BillingCycle newC = new BillingCycle(
                                apartment,
                                "Cycle " + LocalDate.now().getYear() + "-" + String.format("%02d", LocalDate.now().getMonthValue()),
                                LocalDate.now().withDayOfMonth(1),
                                LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()),
                                LocalDate.now().plusDays(15)
                        );
                        return billingCycleRepository.save(newC);
                    });
        }

        String flatStr = household.getFlatNumber() != null ? household.getFlatNumber().replaceAll("[^a-zA-Z0-9]", "") : "FLAT";
        String invoiceNumber = String.format("INV-MANUAL-%s-%d", flatStr, System.currentTimeMillis() % 100000);

        BigDecimal meteredKl = request.getMeteredConsumptionKl() != null ? request.getMeteredConsumptionKl() : BigDecimal.ZERO;
        BigDecimal meteredCharge = request.getMeteredCharge() != null ? request.getMeteredCharge() : BigDecimal.ZERO;
        BigDecimal sharedCharge = request.getSharedCommonCharge() != null ? request.getSharedCommonCharge() : BigDecimal.ZERO;
        BigDecimal total = request.getTotalAmount() != null ? request.getTotalAmount() : meteredCharge.add(sharedCharge);

        Invoice invoice = new Invoice();
        invoice.setHousehold(household);
        invoice.setBillingCycle(cycle);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setMeteredConsumptionKl(meteredKl);
        invoice.setMeteredCharge(meteredCharge);
        invoice.setSharedCommonCharge(sharedCharge);
        invoice.setTotalAmount(total);
        invoice.setPaymentStatus(PaymentStatus.PENDING);
        invoice.setNotes(request.getNotes() != null && !request.getNotes().isBlank()
                ? request.getNotes()
                : "Manually issued water bill in INR (₹) by Super Admin");

        Invoice saved = invoiceRepository.save(invoice);

        try {
            emailNotificationService.sendMonthlyBillNotification(saved);
        } catch (Exception ex) {
            // Ignore notification error in test / mock environment
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
