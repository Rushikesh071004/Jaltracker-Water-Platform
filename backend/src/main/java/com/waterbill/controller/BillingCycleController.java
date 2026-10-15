package com.waterbill.controller;

import com.waterbill.dto.BillingCalculationResult;
import com.waterbill.dto.BillingCycleRequest;
import com.waterbill.dto.BulkWaterPurchaseRequest;
import com.waterbill.entity.BillingCycle;
import com.waterbill.entity.BulkWaterPurchase;
import com.waterbill.repository.BulkWaterPurchaseRepository;
import com.waterbill.service.BillingCycleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/billing")
public class BillingCycleController {

    private final BillingCycleService billingCycleService;
    private final BulkWaterPurchaseRepository bulkWaterPurchaseRepository;

    public BillingCycleController(BillingCycleService billingCycleService,
                                  BulkWaterPurchaseRepository bulkWaterPurchaseRepository) {
        this.billingCycleService = billingCycleService;
        this.bulkWaterPurchaseRepository = bulkWaterPurchaseRepository;
    }

    @PostMapping("/cycles")
    public ResponseEntity<BillingCycle> createCycle(@Valid @RequestBody BillingCycleRequest request) {
        BillingCycle created = billingCycleService.createBillingCycle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/cycles")
    public ResponseEntity<List<BillingCycle>> getCycles(@RequestParam Long apartmentId) {
        return ResponseEntity.ok(billingCycleService.getCyclesByApartment(apartmentId));
    }

    @GetMapping("/cycles/{id}")
    public ResponseEntity<BillingCycle> getCycleById(@PathVariable Long id) {
        return ResponseEntity.ok(billingCycleService.getCycleById(id));
    }

    @PostMapping("/bulk-purchases")
    public ResponseEntity<BulkWaterPurchase> addBulkPurchase(@Valid @RequestBody BulkWaterPurchaseRequest request) {
        BulkWaterPurchase purchase = billingCycleService.addBulkWaterPurchase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(purchase);
    }

    @GetMapping("/cycles/{id}/purchases")
    public ResponseEntity<List<BulkWaterPurchase>> getPurchasesForCycle(@PathVariable Long id) {
        return ResponseEntity.ok(bulkWaterPurchaseRepository.findByBillingCycleId(id));
    }

    @GetMapping("/cycles/{id}/calculate")
    public ResponseEntity<BillingCalculationResult> calculateCycle(@PathVariable Long id) {
        BillingCalculationResult result = billingCycleService.calculateCycleBilling(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cycles/{id}/finalize")
    public ResponseEntity<java.util.Map<String, Object>> finalizeCycle(@PathVariable Long id) {
        java.util.Map<String, Object> result = billingCycleService.finalizeCycleAsync(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/cycles/{id}/archive")
    public ResponseEntity<BillingCycle> archiveCycle(@PathVariable Long id) {
        BillingCycle archived = billingCycleService.archiveCycle(id);
        return ResponseEntity.ok(archived);
    }
}
