package com.waterbill.service;

import com.waterbill.entity.Household;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class ChargeDistributionService {

    /**
     * Apportions total shared apartment water costs across households.
     * Metered households are charged proportionally by their metered consumption volume.
     * Unmetered households fall back to flat area (sqft) distribution.
     *
     * @param totalSharedCost Total shared apartment water cost (bulk purchases + common area costs)
     * @param households List of all households in the apartment
     * @param meteredUsageMap Map of householdId to metered consumption (kL) for the cycle
     * @return Map of householdId to allocated shared charge
     */
    public Map<Long, BigDecimal> apportionSharedCharges(
            BigDecimal totalSharedCost,
            List<Household> households,
            Map<Long, BigDecimal> meteredUsageMap) {

        Map<Long, BigDecimal> allocations = new HashMap<>();

        if (totalSharedCost == null || totalSharedCost.compareTo(BigDecimal.ZERO) <= 0 || households == null || households.isEmpty()) {
            if (households != null) {
                for (Household h : households) {
                    allocations.put(h.getId(), BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
                }
            }
            return allocations;
        }

        List<Household> meteredList = new ArrayList<>();
        List<Household> unmeteredList = new ArrayList<>();

        BigDecimal totalArea = BigDecimal.ZERO;
        BigDecimal meteredArea = BigDecimal.ZERO;
        BigDecimal unmeteredArea = BigDecimal.ZERO;

        for (Household h : households) {
            BigDecimal area = h.getAreaSqft() != null ? h.getAreaSqft() : BigDecimal.valueOf(1000.0);
            totalArea = totalArea.add(area);

            if (Boolean.TRUE.equals(h.getIsMetered())) {
                meteredList.add(h);
                meteredArea = meteredArea.add(area);
            } else {
                unmeteredList.add(h);
                unmeteredArea = unmeteredArea.add(area);
            }
        }

        if (totalArea.compareTo(BigDecimal.ZERO) <= 0) {
            totalArea = BigDecimal.valueOf(households.size());
        }

        // Calculate total metered consumption
        BigDecimal totalMeteredUsage = BigDecimal.ZERO;
        for (Household h : meteredList) {
            BigDecimal usage = meteredUsageMap.getOrDefault(h.getId(), BigDecimal.ZERO);
            if (usage.compareTo(BigDecimal.ZERO) > 0) {
                totalMeteredUsage = totalMeteredUsage.add(usage);
            }
        }

        // Case 1: All households are metered
        if (unmeteredList.isEmpty()) {
            if (totalMeteredUsage.compareTo(BigDecimal.ZERO) > 0) {
                // Strictly proportional to consumption
                for (Household h : meteredList) {
                    BigDecimal usage = meteredUsageMap.getOrDefault(h.getId(), BigDecimal.ZERO);
                    BigDecimal ratio = usage.divide(totalMeteredUsage, 8, RoundingMode.HALF_UP);
                    BigDecimal share = totalSharedCost.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                    allocations.put(h.getId(), share);
                }
            } else {
                // Fallback to flat area distribution if total usage is 0
                for (Household h : meteredList) {
                    BigDecimal area = h.getAreaSqft() != null ? h.getAreaSqft() : BigDecimal.valueOf(1000.0);
                    BigDecimal ratio = area.divide(totalArea, 8, RoundingMode.HALF_UP);
                    BigDecimal share = totalSharedCost.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                    allocations.put(h.getId(), share);
                }
            }
        }
        // Case 2: All households are unmetered
        else if (meteredList.isEmpty()) {
            for (Household h : unmeteredList) {
                BigDecimal area = h.getAreaSqft() != null ? h.getAreaSqft() : BigDecimal.valueOf(1000.0);
                BigDecimal ratio = area.divide(totalArea, 8, RoundingMode.HALF_UP);
                BigDecimal share = totalSharedCost.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                allocations.put(h.getId(), share);
            }
        }
        // Case 3: Hybrid apartment (mix of metered and unmetered)
        else {
            BigDecimal unmeteredCostPool = totalSharedCost
                    .multiply(unmeteredArea)
                    .divide(totalArea, 8, RoundingMode.HALF_UP);
            BigDecimal meteredCostPool = totalSharedCost.subtract(unmeteredCostPool);

            // Apportion unmetered pool by flat area
            for (Household h : unmeteredList) {
                BigDecimal area = h.getAreaSqft() != null ? h.getAreaSqft() : BigDecimal.valueOf(1000.0);
                BigDecimal ratio = unmeteredArea.compareTo(BigDecimal.ZERO) > 0
                        ? area.divide(unmeteredArea, 8, RoundingMode.HALF_UP)
                        : BigDecimal.ONE.divide(BigDecimal.valueOf(unmeteredList.size()), 8, RoundingMode.HALF_UP);
                BigDecimal share = unmeteredCostPool.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                allocations.put(h.getId(), share);
            }

            // Apportion metered pool by metered consumption volume
            if (totalMeteredUsage.compareTo(BigDecimal.ZERO) > 0) {
                for (Household h : meteredList) {
                    BigDecimal usage = meteredUsageMap.getOrDefault(h.getId(), BigDecimal.ZERO);
                    BigDecimal ratio = usage.divide(totalMeteredUsage, 8, RoundingMode.HALF_UP);
                    BigDecimal share = meteredCostPool.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                    allocations.put(h.getId(), share);
                }
            } else {
                // Fallback to flat area distribution for metered pool
                for (Household h : meteredList) {
                    BigDecimal area = h.getAreaSqft() != null ? h.getAreaSqft() : BigDecimal.valueOf(1000.0);
                    BigDecimal ratio = area.divide(meteredArea, 8, RoundingMode.HALF_UP);
                    BigDecimal share = meteredCostPool.multiply(ratio).setScale(2, RoundingMode.HALF_UP);
                    allocations.put(h.getId(), share);
                }
            }
        }

        // Adjust any fractional rounding difference (e.g. +/- 0.01) to match totalSharedCost exactly
        BigDecimal sumAllocated = allocations.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discrepancy = totalSharedCost.subtract(sumAllocated).setScale(2, RoundingMode.HALF_UP);

        if (discrepancy.compareTo(BigDecimal.ZERO) != 0 && !allocations.isEmpty()) {
            // Find key with largest allocation and adjust
            Long maxKey = Collections.max(allocations.entrySet(), Map.Entry.comparingByValue()).getKey();
            allocations.put(maxKey, allocations.get(maxKey).add(discrepancy));
        }

        return allocations;
    }
}
