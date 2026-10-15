package com.waterbill.controller;

import com.waterbill.dto.TariffPlanRequest;
import com.waterbill.dto.TariffTierRequest;
import com.waterbill.entity.Apartment;
import com.waterbill.entity.TariffPlan;
import com.waterbill.entity.TariffTier;
import com.waterbill.repository.ApartmentRepository;
import com.waterbill.repository.TariffPlanRepository;
import com.waterbill.service.TariffBillingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tariffs")
public class TariffController {

    private final TariffPlanRepository tariffPlanRepository;
    private final ApartmentRepository apartmentRepository;
    private final TariffBillingService tariffBillingService;

    public TariffController(TariffPlanRepository tariffPlanRepository,
                            ApartmentRepository apartmentRepository,
                            TariffBillingService tariffBillingService) {
        this.tariffPlanRepository = tariffPlanRepository;
        this.apartmentRepository = apartmentRepository;
        this.tariffBillingService = tariffBillingService;
    }

    @PostMapping
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "tariffs", allEntries = true)
    public ResponseEntity<TariffPlan> createTariffPlan(@Valid @RequestBody TariffPlanRequest request) {
        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found with ID: " + request.getApartmentId()));

        TariffPlan plan = new TariffPlan(
                apartment,
                request.getName(),
                request.getDescription(),
                request.getEffectiveFrom(),
                request.getIsActive() != null ? request.getIsActive() : true
        );
        plan.setEffectiveTo(request.getEffectiveTo());

        int order = 1;
        for (TariffTierRequest tierReq : request.getTiers()) {
            TariffTier tier = new TariffTier(
                    plan,
                    tierReq.getTierName(),
                    tierReq.getMinVolumeKl(),
                    tierReq.getMaxVolumeKl(),
                    tierReq.getRatePerKl(),
                    tierReq.getTierOrder() != null ? tierReq.getTierOrder() : order++
            );
            plan.addTier(tier);
        }

        TariffPlan saved = tariffPlanRepository.save(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<TariffPlan>> getTariffPlans(@RequestParam Long apartmentId) {
        return ResponseEntity.ok(tariffPlanRepository.findByApartmentId(apartmentId));
    }

    @GetMapping("/active")
    public ResponseEntity<TariffPlan> getActiveTariff(@RequestParam Long apartmentId) {
        return ResponseEntity.ok(tariffBillingService.getActiveTariffForApartment(apartmentId));
    }
}
