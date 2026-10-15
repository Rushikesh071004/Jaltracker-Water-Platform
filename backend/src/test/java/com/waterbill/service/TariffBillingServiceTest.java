package com.waterbill.service;

import com.waterbill.dto.TierChargeBreakdown;
import com.waterbill.entity.TariffPlan;
import com.waterbill.entity.TariffTier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TariffBillingServiceTest {

    @InjectMocks
    private TariffBillingService tariffBillingService;

    private List<TariffTier> tiers;

    @BeforeEach
    void setUp() {
        TariffPlan plan = new TariffPlan();
        tiers = new ArrayList<>();
        // Tier 1: 0 - 10 kL @ $2.50
        tiers.add(new TariffTier(plan, "Tier 1", BigDecimal.valueOf(0.0), BigDecimal.valueOf(10.0), BigDecimal.valueOf(2.50), 1));
        // Tier 2: 10 - 20 kL @ $4.00
        tiers.add(new TariffTier(plan, "Tier 2", BigDecimal.valueOf(10.0), BigDecimal.valueOf(20.0), BigDecimal.valueOf(4.00), 2));
        // Tier 3: 20 - 35 kL @ $6.50
        tiers.add(new TariffTier(plan, "Tier 3", BigDecimal.valueOf(20.0), BigDecimal.valueOf(35.0), BigDecimal.valueOf(6.50), 3));
        // Tier 4: > 35 kL @ $10.00
        tiers.add(new TariffTier(plan, "Tier 4", BigDecimal.valueOf(35.0), null, BigDecimal.valueOf(10.00), 4));
    }

    @Test
    @DisplayName("Zero consumption should yield $0.00 charge")
    void testZeroConsumption() {
        var result = tariffBillingService.calculateTieredCharge(BigDecimal.ZERO, tiers);
        assertEquals(0, BigDecimal.ZERO.setScale(2).compareTo(result.getTotalCharge()));
    }

    @Test
    @DisplayName("Consumption strictly within Tier 1 (e.g., 8 kL)")
    void testTier1Consumption() {
        // 8 kL * 2.50 = 20.00
        var result = tariffBillingService.calculateTieredCharge(BigDecimal.valueOf(8.0), tiers);
        assertEquals(0, BigDecimal.valueOf(20.00).compareTo(result.getTotalCharge()));
    }

    @Test
    @DisplayName("Consumption spanning Tier 1 and Tier 2 (e.g., 15 kL)")
    void testTier2Consumption() {
        // Tier 1: 10 kL * 2.50 = 25.00
        // Tier 2:  5 kL * 4.00 = 20.00
        // Total = 45.00
        var result = tariffBillingService.calculateTieredCharge(BigDecimal.valueOf(15.0), tiers);
        assertEquals(0, BigDecimal.valueOf(45.00).compareTo(result.getTotalCharge()));
    }

    @Test
    @DisplayName("Consumption spanning Tier 1, Tier 2, and Tier 3 (e.g., 25 kL)")
    void testTier3Consumption() {
        // Tier 1: 10 kL * 2.50 = 25.00
        // Tier 2: 10 kL * 4.00 = 40.00
        // Tier 3:  5 kL * 6.50 = 32.50
        // Total = 97.50
        var result = tariffBillingService.calculateTieredCharge(BigDecimal.valueOf(25.0), tiers);
        assertEquals(0, BigDecimal.valueOf(97.50).compareTo(result.getTotalCharge()));
    }

    @Test
    @DisplayName("Excessive consumption entering unbounded Tier 4 (e.g., 40 kL)")
    void testTier4Consumption() {
        // Tier 1: 10 kL * 2.50 = 25.00
        // Tier 2: 10 kL * 4.00 = 40.00
        // Tier 3: 15 kL * 6.50 = 97.50
        // Tier 4:  5 kL * 10.0 = 50.00
        // Total = 212.50
        var result = tariffBillingService.calculateTieredCharge(BigDecimal.valueOf(40.0), tiers);
        assertEquals(0, BigDecimal.valueOf(212.50).compareTo(result.getTotalCharge()));
    }
}
