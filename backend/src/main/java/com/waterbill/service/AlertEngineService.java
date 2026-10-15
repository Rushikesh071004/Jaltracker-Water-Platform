package com.waterbill.service;

import com.waterbill.entity.*;
import com.waterbill.repository.AlertRepository;
import com.waterbill.repository.HouseholdRepository;
import com.waterbill.repository.WaterUsageLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class AlertEngineService {

    private static final Logger log = LoggerFactory.getLogger(AlertEngineService.class);

    private final AlertRepository alertRepository;
    private final WaterUsageLogRepository usageLogRepository;
    private final HouseholdRepository householdRepository;
    private final EmailNotificationService emailNotificationService;

    private final double sigmaThreshold;
    private final int minHistoricalDays;

    public AlertEngineService(
            AlertRepository alertRepository,
            WaterUsageLogRepository usageLogRepository,
            HouseholdRepository householdRepository,
            EmailNotificationService emailNotificationService,
            @Value("${app.alerts.leak-sigma-threshold:2.0}") double sigmaThreshold,
            @Value("${app.alerts.min-historical-days:7}") int minHistoricalDays) {
        this.alertRepository = alertRepository;
        this.usageLogRepository = usageLogRepository;
        this.householdRepository = householdRepository;
        this.emailNotificationService = emailNotificationService;
        this.sigmaThreshold = sigmaThreshold;
        this.minHistoricalDays = minHistoricalDays;
    }

    /**
     * Nightly scheduled scan across all households to detect threshold breaches and 2-sigma anomalies.
     */
    @Scheduled(cron = "${app.alerts.scheduled-cron:0 0 2 * * ?}")
    @Transactional
    public void runScheduledAnomalyScan() {
        log.info("Running automated scheduled water usage anomaly and leak detection scan...");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Household> allHouseholds = householdRepository.findAll();

        int alertsGenerated = 0;
        for (Household household : allHouseholds) {
            usageLogRepository.findByHouseholdIdAndReadingDate(household.getId(), yesterday)
                    .ifPresent(logEntry -> evaluateUsageAlerts(household, logEntry.getReadingDate(), logEntry.getDailyConsumptionKl()));
        }
        log.info("Scheduled anomaly scan complete. Processed {} households.", allHouseholds.size());
    }

    /**
     * Evaluates a specific daily reading against threshold and historical 2-sigma leak criteria.
     */
    @Transactional
    public void evaluateUsageAlerts(Household household, LocalDate readingDate, BigDecimal consumptionKl) {
        if (household == null || consumptionKl == null || readingDate == null) {
            return;
        }

        // 1. Check Configurable Usage Threshold
        BigDecimal threshold = household.getDailyUsageThresholdKl();
        if (threshold != null && consumptionKl.compareTo(threshold) > 0) {
            boolean alreadyAlerted = alertRepository.existsByHouseholdIdAndReadingDateAndAlertType(
                    household.getId(), readingDate, AlertType.OVERUSE_THRESHOLD);

            if (!alreadyAlerted) {
                Alert thresholdAlert = new Alert(
                        household,
                        AlertType.OVERUSE_THRESHOLD,
                        AlertSeverity.WARNING,
                        "Daily Usage Threshold Exceeded in " + household.getFlatNumber(),
                        String.format("Usage on %s was %.3f kL, which exceeded your daily configured limit of %.3f kL.",
                                readingDate, consumptionKl, threshold),
                        readingDate,
                        consumptionKl,
                        threshold,
                        null
                );
                alertRepository.save(thresholdAlert);
                emailNotificationService.sendOveruseAlert(household, thresholdAlert);
                log.warn("Threshold alert triggered for household {}: {} kL > {} kL",
                        household.getFlatNumber(), consumptionKl, threshold);
            }
        }

        // 2. Statistical Outlier Detection (Usage > 2σ above household average) -> Flag potential leak
        List<WaterUsageLog> historicalLogs = usageLogRepository.findHistoricalLogsBefore(
                household.getId(), readingDate, PageRequest.of(0, 30));

        if (historicalLogs.size() >= minHistoricalDays) {
            double[] historicalValues = historicalLogs.stream()
                    .mapToDouble(l -> l.getDailyConsumptionKl().doubleValue())
                    .toArray();

            double sum = 0.0;
            for (double val : historicalValues) {
                sum += val;
            }
            double mean = sum / historicalValues.length;

            double varianceSum = 0.0;
            for (double val : historicalValues) {
                varianceSum += Math.pow(val - mean, 2);
            }
            double variance = varianceSum / historicalValues.length;
            double stdDev = Math.sqrt(variance);

            double currentVal = consumptionKl.doubleValue();
            if (stdDev > 0.05) { // Ensure standard deviation is non-trivial
                double zScore = (currentVal - mean) / stdDev;

                if (zScore >= sigmaThreshold) {
                    boolean alreadyAlerted = alertRepository.existsByHouseholdIdAndReadingDateAndAlertType(
                            household.getId(), readingDate, AlertType.LEAK_DETECTED_2SIGMA);

                    if (!alreadyAlerted) {
                        BigDecimal sigmaBd = BigDecimal.valueOf(zScore).setScale(2, RoundingMode.HALF_UP);
                        Alert leakAlert = new Alert(
                                household,
                                AlertType.LEAK_DETECTED_2SIGMA,
                                AlertSeverity.CRITICAL,
                                "Potential Water Leak Detected in " + household.getFlatNumber(),
                                String.format("Water consumption on %s was %.3f kL, which is %.2f standard deviations (σ) above the 30-day baseline average (%.3f kL). Please inspect faucets, pipes, and cisterns immediately.",
                                        readingDate, consumptionKl, sigmaBd, mean),
                                readingDate,
                                consumptionKl,
                                threshold,
                                sigmaBd
                        );
                        alertRepository.save(leakAlert);
                        emailNotificationService.sendLeakAlert(household, leakAlert);
                        log.error("CRITICAL LEAK DETECTED for household {}: usage=%.3f kL, mean=%.3f, stdDev=%.3f, zScore=%.2fσ",
                                household.getFlatNumber(), currentVal, mean, stdDev, zScore);
                    }
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Alert> getAlertsByApartment(Long apartmentId) {
        return alertRepository.findByHouseholdApartmentIdOrderByReadingDateDesc(apartmentId);
    }

    @Transactional(readOnly = true)
    public List<Alert> getAlertsByHousehold(Long householdId) {
        return alertRepository.findByHouseholdIdOrderByReadingDateDesc(householdId);
    }

    @Transactional
    public Alert updateAlertStatus(Long alertId, AlertStatus status) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found with ID: " + alertId));
        alert.setStatus(status);
        return alertRepository.save(alert);
    }
}
