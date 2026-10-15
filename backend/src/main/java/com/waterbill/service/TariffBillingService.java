package com.waterbill.service;

import com.waterbill.dto.TierChargeBreakdown;
import com.waterbill.entity.TariffPlan;
import com.waterbill.entity.TariffTier;
import com.waterbill.repository.TariffPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TariffBillingService {

    private final TariffPlanRepository tariffPlanRepository;

    public TariffBillingService(TariffPlanRepository tariffPlanRepository) {
        this.tariffPlanRepository = tariffPlanRepository;
    }

    public static class TierCalculationResult {
        private final BigDecimal totalCharge;
        private final List<TierChargeBreakdown> breakdowns;

        public TierCalculationResult(BigDecimal totalCharge, List<TierChargeBreakdown> breakdowns) {
            this.totalCharge = totalCharge;
            this.breakdowns = breakdowns;
        }

        public BigDecimal getTotalCharge() { return totalCharge; }
        public List<TierChargeBreakdown> getBreakdowns() { return breakdowns; }
    }

    /**
     * Calculates tiered consumption charges for a given metered volume across ordered rate tiers.
     */
    public TierCalculationResult calculateTieredCharge(BigDecimal consumptionKl, List<TariffTier> tiers) {
        if (consumptionKl == null || consumptionKl.compareTo(BigDecimal.ZERO) <= 0 || tiers == null || tiers.isEmpty()) {
            return new TierCalculationResult(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), new ArrayList<>());
        }

        List<TariffTier> sortedTiers = tiers.stream()
                .sorted(Comparator.comparing(TariffTier::getTierOrder))
                .toList();

        BigDecimal totalCharge = BigDecimal.ZERO;
        List<TierChargeBreakdown> breakdowns = new ArrayList<>();

        for (TariffTier tier : sortedTiers) {
            BigDecimal minVol = tier.getMinVolumeKl() != null ? tier.getMinVolumeKl() : BigDecimal.ZERO;
            BigDecimal maxVol = tier.getMaxVolumeKl(); // null denotes unbounded

            if (consumptionKl.compareTo(minVol) <= 0) {
                // Consumption has not reached this tier
                breakdowns.add(new TierChargeBreakdown(
                        tier.getTierName(),
                        minVol,
                        maxVol,
                        BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP),
                        tier.getRatePerKl(),
                        BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                ));
                continue;
            }

            BigDecimal upperLimit = (maxVol != null && consumptionKl.compareTo(maxVol) > 0) ? maxVol : consumptionKl;
            BigDecimal volumeInTier = upperLimit.subtract(minVol);

            if (volumeInTier.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal tierSubtotal = volumeInTier.multiply(tier.getRatePerKl()).setScale(2, RoundingMode.HALF_UP);
                totalCharge = totalCharge.add(tierSubtotal);

                breakdowns.add(new TierChargeBreakdown(
                        tier.getTierName(),
                        minVol,
                        maxVol,
                        volumeInTier.setScale(3, RoundingMode.HALF_UP),
                        tier.getRatePerKl(),
                        tierSubtotal
                ));
            }
        }

        return new TierCalculationResult(totalCharge.setScale(2, RoundingMode.HALF_UP), breakdowns);
    }

    @org.springframework.cache.annotation.Cacheable(value = "tariffs", key = "#apartmentId")
    @Transactional(readOnly = true)
    public TariffPlan getActiveTariffForApartment(Long apartmentId) {
        return tariffPlanRepository.findFirstByApartmentIdAndIsActiveTrueOrderByEffectiveFromDesc(apartmentId)
                .orElseThrow(() -> new IllegalStateException("No active tariff plan found for apartment ID: " + apartmentId));
    }
}
