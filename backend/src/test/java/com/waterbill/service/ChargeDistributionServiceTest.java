package com.waterbill.service;

import com.waterbill.entity.Apartment;
import com.waterbill.entity.Household;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ChargeDistributionServiceTest {

    private ChargeDistributionService distributionService;
    private Apartment apartment;

    @BeforeEach
    void setUp() {
        distributionService = new ChargeDistributionService();
        apartment = new Apartment("Test Complex", "123 Main St", 4, "test@test.com", "555-0100");
        apartment.setId(1L);
    }

    @Test
    @DisplayName("All metered households: apportioned strictly proportional to metered usage")
    void testAllMeteredProportional() {
        Household h1 = new Household(apartment, "101", "A", 1, BigDecimal.valueOf(1000), 2, true, BigDecimal.valueOf(1.5));
        h1.setId(1L);
        Household h2 = new Household(apartment, "102", "A", 1, BigDecimal.valueOf(1000), 2, true, BigDecimal.valueOf(1.5));
        h2.setId(2L);

        List<Household> households = List.of(h1, h2);

        Map<Long, BigDecimal> usage = Map.of(
                1L, BigDecimal.valueOf(10.0), // 25% of 40 kL
                2L, BigDecimal.valueOf(30.0)  // 75% of 40 kL
        );

        BigDecimal totalSharedCost = BigDecimal.valueOf(100.00);
        Map<Long, BigDecimal> result = distributionService.apportionSharedCharges(totalSharedCost, households, usage);

        assertEquals(0, BigDecimal.valueOf(25.00).compareTo(result.get(1L)));
        assertEquals(0, BigDecimal.valueOf(75.00).compareTo(result.get(2L)));

        BigDecimal sum = result.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, totalSharedCost.compareTo(sum));
    }

    @Test
    @DisplayName("All unmetered households: fallback to flat area (sqft) distribution")
    void testAllUnmeteredFlatArea() {
        Household h1 = new Household(apartment, "101", "A", 1, BigDecimal.valueOf(1000), 2, false, BigDecimal.valueOf(1.5));
        h1.setId(1L);
        Household h2 = new Household(apartment, "102", "A", 1, BigDecimal.valueOf(3000), 4, false, BigDecimal.valueOf(1.5));
        h2.setId(2L);

        List<Household> households = List.of(h1, h2);
        Map<Long, BigDecimal> usage = Map.of(1L, BigDecimal.ZERO, 2L, BigDecimal.ZERO);

        BigDecimal totalSharedCost = BigDecimal.valueOf(200.00);
        Map<Long, BigDecimal> result = distributionService.apportionSharedCharges(totalSharedCost, households, usage);

        // h1 is 1000/4000 = 25% -> 50.00
        // h2 is 3000/4000 = 75% -> 150.00
        assertEquals(0, BigDecimal.valueOf(50.00).compareTo(result.get(1L)));
        assertEquals(0, BigDecimal.valueOf(150.00).compareTo(result.get(2L)));
    }

    @Test
    @DisplayName("Hybrid apartment: metered proportional + unmetered flat area fallback")
    void testHybridApartmentDistribution() {
        // Metered households: Total area = 2000 sqft
        Household h1 = new Household(apartment, "101", "A", 1, BigDecimal.valueOf(1000), 2, true, BigDecimal.valueOf(1.5));
        h1.setId(1L);
        Household h2 = new Household(apartment, "102", "A", 1, BigDecimal.valueOf(1000), 2, true, BigDecimal.valueOf(1.5));
        h2.setId(2L);

        // Unmetered households: Total area = 2000 sqft
        Household h3 = new Household(apartment, "201", "B", 2, BigDecimal.valueOf(800), 2, false, BigDecimal.valueOf(1.5));
        h3.setId(3L);
        Household h4 = new Household(apartment, "202", "B", 2, BigDecimal.valueOf(1200), 3, false, BigDecimal.valueOf(1.5));
        h4.setId(4L);

        List<Household> households = List.of(h1, h2, h3, h4);

        // Metered usage: h1 = 20 kL (40%), h2 = 30 kL (60%)
        Map<Long, BigDecimal> usage = Map.of(
                1L, BigDecimal.valueOf(20.0),
                2L, BigDecimal.valueOf(30.0),
                3L, BigDecimal.ZERO,
                4L, BigDecimal.ZERO
        );

        BigDecimal totalSharedCost = BigDecimal.valueOf(400.00);
        // Total area = 4000 sqft.
        // Unmetered area = 2000 sqft (50%) -> Unmetered Pool = $200
        // Metered area = 2000 sqft (50%) -> Metered Pool = $200
        // Unmetered h3: 800/2000 * 200 = $80.00
        // Unmetered h4: 1200/2000 * 200 = $120.00
        // Metered h1: 20/50 * 200 = $80.00
        // Metered h2: 30/50 * 200 = $120.00
        Map<Long, BigDecimal> result = distributionService.apportionSharedCharges(totalSharedCost, households, usage);

        assertEquals(0, BigDecimal.valueOf(80.00).compareTo(result.get(1L)));
        assertEquals(0, BigDecimal.valueOf(120.00).compareTo(result.get(2L)));
        assertEquals(0, BigDecimal.valueOf(80.00).compareTo(result.get(3L)));
        assertEquals(0, BigDecimal.valueOf(120.00).compareTo(result.get(4L)));

        BigDecimal sum = result.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, totalSharedCost.compareTo(sum));
    }
}
