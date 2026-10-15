package com.waterbill.service;

import com.waterbill.dto.*;
import com.waterbill.entity.*;
import com.waterbill.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class BillingCycleService {

    private final BillingCycleRepository billingCycleRepository;
    private final ApartmentRepository apartmentRepository;
    private final HouseholdRepository householdRepository;
    private final WaterUsageLogRepository usageLogRepository;
    private final BulkWaterPurchaseRepository bulkWaterPurchaseRepository;
    private final InvoiceRepository invoiceRepository;
    private final TariffBillingService tariffBillingService;
    private final ChargeDistributionService chargeDistributionService;
    private final EmailNotificationService emailNotificationService;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;
    private final BillingAsyncProcessor billingAsyncProcessor;

    public BillingCycleService(
            BillingCycleRepository billingCycleRepository,
            ApartmentRepository apartmentRepository,
            HouseholdRepository householdRepository,
            WaterUsageLogRepository usageLogRepository,
            BulkWaterPurchaseRepository bulkWaterPurchaseRepository,
            InvoiceRepository invoiceRepository,
            TariffBillingService tariffBillingService,
            ChargeDistributionService chargeDistributionService,
            EmailNotificationService emailNotificationService,
            org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate,
            BillingAsyncProcessor billingAsyncProcessor) {
        this.billingCycleRepository = billingCycleRepository;
        this.apartmentRepository = apartmentRepository;
        this.householdRepository = householdRepository;
        this.usageLogRepository = usageLogRepository;
        this.bulkWaterPurchaseRepository = bulkWaterPurchaseRepository;
        this.invoiceRepository = invoiceRepository;
        this.tariffBillingService = tariffBillingService;
        this.chargeDistributionService = chargeDistributionService;
        this.emailNotificationService = emailNotificationService;
        this.rabbitTemplate = rabbitTemplate;
        this.billingAsyncProcessor = billingAsyncProcessor;
    }

    @Transactional
    public BillingCycle createBillingCycle(BillingCycleRequest request) {
        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found with ID: " + request.getApartmentId()));

        BillingCycle cycle = new BillingCycle(
                apartment,
                request.getCycleName(),
                request.getStartDate(),
                request.getEndDate(),
                request.getDueDate()
        );
        cycle.setTotalCommonAreaVolumeKl(request.getTotalCommonAreaVolumeKl() != null ? request.getTotalCommonAreaVolumeKl() : BigDecimal.ZERO);
        return billingCycleRepository.save(cycle);
    }

    @Transactional
    public BulkWaterPurchase addBulkWaterPurchase(BulkWaterPurchaseRequest request) {
        BillingCycle cycle = billingCycleRepository.findById(request.getBillingCycleId())
                .orElseThrow(() -> new IllegalArgumentException("Billing cycle not found with ID: " + request.getBillingCycleId()));
        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found with ID: " + request.getApartmentId()));

        BulkWaterPurchase purchase = new BulkWaterPurchase(
                cycle,
                apartment,
                request.getPurchaseDate(),
                request.getSupplierName(),
                request.getPurchaseType(),
                request.getVolumeKl(),
                request.getUnitCostPerKl(),
                request.getInvoiceReference(),
                request.getNotes()
        );

        BulkWaterPurchase savedPurchase = bulkWaterPurchaseRepository.save(purchase);

        // Update cycle total bulk water cost
        BigDecimal updatedTotal = cycle.getTotalBulkWaterCost().add(savedPurchase.getTotalCost());
        cycle.setTotalBulkWaterCost(updatedTotal);
        billingCycleRepository.save(cycle);

        return savedPurchase;
    }

    @Transactional(readOnly = true)
    public BillingCalculationResult calculateCycleBilling(Long cycleId) {
        BillingCycle cycle = billingCycleRepository.findById(cycleId)
                .orElseThrow(() -> new IllegalArgumentException("Billing cycle not found with ID: " + cycleId));

        Long apartmentId = cycle.getApartment().getId();
        List<Household> households = householdRepository.findByApartmentId(apartmentId);
        TariffPlan tariffPlan = tariffBillingService.getActiveTariffForApartment(apartmentId);

        // 1. Calculate metered consumption per household
        Map<Long, BigDecimal> meteredUsageMap = new HashMap<>();
        BigDecimal totalMeteredUsage = BigDecimal.ZERO;

        for (Household h : households) {
            if (Boolean.TRUE.equals(h.getIsMetered())) {
                BigDecimal consumption = usageLogRepository.sumConsumptionByHouseholdAndDateRange(
                        h.getId(), cycle.getStartDate(), cycle.getEndDate());
                if (consumption == null) consumption = BigDecimal.ZERO;
                meteredUsageMap.put(h.getId(), consumption.setScale(3, RoundingMode.HALF_UP));
                totalMeteredUsage = totalMeteredUsage.add(consumption);
            } else {
                meteredUsageMap.put(h.getId(), BigDecimal.ZERO);
            }
        }

        // 2. Compute shared cost pool
        BigDecimal totalSharedCost = cycle.getTotalBulkWaterCost() != null ? cycle.getTotalBulkWaterCost() : BigDecimal.ZERO;

        // 3. Apportion shared charges (proportional to metered usage, fallback to flat area)
        Map<Long, BigDecimal> sharedChargesMap = chargeDistributionService.apportionSharedCharges(
                totalSharedCost, households, meteredUsageMap);

        // 4. Compute tiered tariff charge and build summary
        BillingCalculationResult result = new BillingCalculationResult();
        result.setCycleId(cycle.getId());
        result.setCycleName(cycle.getCycleName());
        result.setTotalBulkPurchasedCost(totalSharedCost);
        result.setTotalMeteredConsumptionKl(totalMeteredUsage.setScale(3, RoundingMode.HALF_UP));

        BigDecimal totalMeteredCharges = BigDecimal.ZERO;
        BigDecimal totalSharedAllocated = BigDecimal.ZERO;

        List<HouseholdBillSummary> bills = new ArrayList<>();
        for (Household h : households) {
            BigDecimal usage = meteredUsageMap.getOrDefault(h.getId(), BigDecimal.ZERO);
            TariffBillingService.TierCalculationResult tierResult =
                    tariffBillingService.calculateTieredCharge(usage, tariffPlan.getTiers());

            BigDecimal meteredCharge = tierResult.getTotalCharge();
            BigDecimal sharedCharge = sharedChargesMap.getOrDefault(h.getId(), BigDecimal.ZERO);
            BigDecimal totalAmount = meteredCharge.add(sharedCharge).setScale(2, RoundingMode.HALF_UP);

            totalMeteredCharges = totalMeteredCharges.add(meteredCharge);
            totalSharedAllocated = totalSharedAllocated.add(sharedCharge);

            HouseholdBillSummary bill = new HouseholdBillSummary();
            bill.setHouseholdId(h.getId());
            bill.setFlatNumber(h.getFlatNumber());
            bill.setAreaSqft(h.getAreaSqft());
            bill.setIsMetered(h.getIsMetered());
            bill.setMeteredConsumptionKl(usage);
            bill.setTierBreakdowns(tierResult.getBreakdowns());
            bill.setMeteredCharge(meteredCharge);
            bill.setSharedCommonCharge(sharedCharge);
            bill.setTotalAmount(totalAmount);

            if (h.getResidents() != null && !h.getResidents().isEmpty()) {
                bill.setResidentName(h.getResidents().get(0).getFullName());
                bill.setResidentEmail(h.getResidents().get(0).getEmail());
            }

            bills.add(bill);
        }

        result.setTotalMeteredCharges(totalMeteredCharges.setScale(2, RoundingMode.HALF_UP));
        result.setTotalSharedCommonCharges(totalSharedAllocated.setScale(2, RoundingMode.HALF_UP));
        result.setTotalCycleBilling(totalMeteredCharges.add(totalSharedAllocated).setScale(2, RoundingMode.HALF_UP));
        result.setHouseholdsBilledCount(bills.size());
        result.setHouseholdBills(bills);

        return result;
    }

    @Transactional
    public BillingCalculationResult finalizeCycle(Long cycleId) {
        BillingCycle cycle = billingCycleRepository.findById(cycleId)
                .orElseThrow(() -> new IllegalArgumentException("Billing cycle not found with ID: " + cycleId));

        if (cycle.getStatus() == CycleStatus.FINALIZED || cycle.getStatus() == CycleStatus.ARCHIVED) {
            throw new IllegalStateException("Billing cycle is already " + cycle.getStatus());
        }

        BillingCalculationResult calculation = calculateCycleBilling(cycleId);

        // Persist invoices
        for (HouseholdBillSummary bill : calculation.getHouseholdBills()) {
            Household household = householdRepository.findById(bill.getHouseholdId())
                    .orElseThrow(() -> new IllegalArgumentException("Household not found"));

            String invoiceNumber = String.format("INV-%d-%s-%d", cycle.getId(),
                    household.getFlatNumber().replaceAll("[^a-zA-Z0-9]", ""),
                    System.currentTimeMillis() % 10000);

            bill.setInvoiceNumber(invoiceNumber);

            // Check if existing invoice for cycle/household
            Invoice invoice = invoiceRepository.findByBillingCycleIdAndHouseholdId(cycle.getId(), household.getId())
                    .orElse(new Invoice());

            invoice.setBillingCycle(cycle);
            invoice.setHousehold(household);
            invoice.setInvoiceNumber(invoiceNumber);
            invoice.setMeteredConsumptionKl(bill.getMeteredConsumptionKl());
            invoice.setMeteredCharge(bill.getMeteredCharge());
            invoice.setSharedCommonCharge(bill.getSharedCommonCharge());
            invoice.setTotalAmount(bill.getTotalAmount());
            invoice.setPaymentStatus(PaymentStatus.PENDING);

            invoiceRepository.save(invoice);
        }

        cycle.setStatus(CycleStatus.FINALIZED);
        cycle.setFinalizedAt(OffsetDateTime.now());
        billingCycleRepository.save(cycle);

        // Offload PDF invoice generation and email dispatching to asynchronous RabbitMQ message queue
        com.waterbill.event.BillingFinalizeEvent event = new com.waterbill.event.BillingFinalizeEvent(
                cycle.getId(),
                cycle.getApartment().getId(),
                cycle.getCycleName(),
                calculation.getHouseholdBills()
        );

        try {
            rabbitTemplate.convertAndSend(com.waterbill.config.RabbitMQConfig.EXCHANGE, com.waterbill.config.RabbitMQConfig.ROUTING_KEY, event);
        } catch (Exception ex) {
            // Fallback to async background executor if local RabbitMQ broker is not running
            billingAsyncProcessor.processEventInvoicesAsync(event);
        }

        return calculation;
    }

    @Transactional
    public Map<String, Object> finalizeCycleAsync(Long cycleId) {
        BillingCalculationResult calculation = finalizeCycle(cycleId);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Processing started");
        response.put("message", "Billing cycle finalization and invoice distribution initiated asynchronously.");
        response.put("cycleId", cycleId);
        response.put("cycleName", calculation.getCycleName());
        response.put("totalCycleBilling", calculation.getTotalCycleBilling());
        response.put("householdsBilledCount", calculation.getHouseholdsBilledCount());
        return response;
    }

    @Transactional
    public BillingCycle archiveCycle(Long cycleId) {
        BillingCycle cycle = billingCycleRepository.findById(cycleId)
                .orElseThrow(() -> new IllegalArgumentException("Billing cycle not found with ID: " + cycleId));
        cycle.setStatus(CycleStatus.ARCHIVED);
        return billingCycleRepository.save(cycle);
    }

    @Transactional(readOnly = true)
    public List<BillingCycle> getCyclesByApartment(Long apartmentId) {
        return billingCycleRepository.findByApartmentIdOrderByStartDateDesc(apartmentId);
    }

    @Transactional(readOnly = true)
    public BillingCycle getCycleById(Long cycleId) {
        return billingCycleRepository.findById(cycleId)
                .orElseThrow(() -> new IllegalArgumentException("Cycle not found with ID: " + cycleId));
    }
}
