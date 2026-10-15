package com.waterbill.service;

import com.waterbill.entity.*;
import com.waterbill.repository.AlertRepository;
import com.waterbill.repository.HouseholdRepository;
import com.waterbill.repository.WaterUsageLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertEngineServiceTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private WaterUsageLogRepository usageLogRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    private AlertEngineService alertEngineService;
    private Household household;

    @BeforeEach
    void setUp() {
        alertEngineService = new AlertEngineService(
                alertRepository,
                usageLogRepository,
                householdRepository,
                emailNotificationService,
                2.0, // 2-sigma threshold
                7    // min 7 days history
        );

        Apartment apartment = new Apartment("Test Heights", "123 Elm", 10, "admin@test.com", "555-0100");
        household = new Household(apartment, "101", "A", 1, BigDecimal.valueOf(1000), 2, true, BigDecimal.valueOf(1.500));
        household.setId(1L);
    }

    @Test
    @DisplayName("Evaluate usage alert triggers OVERUSE_THRESHOLD when daily consumption exceeds limit")
    void testOveruseThresholdAlert() {
        LocalDate date = LocalDate.of(2026, 10, 10);
        BigDecimal consumption = BigDecimal.valueOf(2.200); // threshold is 1.500

        when(alertRepository.existsByHouseholdIdAndReadingDateAndAlertType(1L, date, AlertType.OVERUSE_THRESHOLD))
                .thenReturn(false);

        alertEngineService.evaluateUsageAlerts(household, date, consumption);

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository, atLeastOnce()).save(alertCaptor.capture());

        boolean thresholdFound = alertCaptor.getAllValues().stream()
                .anyMatch(a -> a.getAlertType() == AlertType.OVERUSE_THRESHOLD);
        assertTrue(thresholdFound, "Should have created an OVERUSE_THRESHOLD alert");
        verify(emailNotificationService, atLeastOnce()).sendOveruseAlert(eq(household), any(Alert.class));
    }

    @Test
    @DisplayName("Evaluate usage alert triggers LEAK_DETECTED_2SIGMA when consumption > 2 standard deviations")
    void test2SigmaOutlierLeakDetection() {
        LocalDate date = LocalDate.of(2026, 10, 11);
        BigDecimal spikeConsumption = BigDecimal.valueOf(2.900);

        // Prepare 10 days of stable historical usage (~0.500 kL)
        List<WaterUsageLog> history = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            WaterUsageLog log = new WaterUsageLog(
                    household, null, date.minusDays(i),
                    BigDecimal.valueOf(100 + i),
                    BigDecimal.valueOf(0.500 + (i % 2 == 0 ? 0.05 : -0.05)),
                    LogSource.MANUAL_ENTRY, "baseline"
            );
            history.add(log);
        }

        when(usageLogRepository.findHistoricalLogsBefore(eq(1L), eq(date), any(Pageable.class)))
                .thenReturn(history);
        lenient().when(alertRepository.existsByHouseholdIdAndReadingDateAndAlertType(anyLong(), any(LocalDate.class), any(AlertType.class)))
                .thenReturn(false);

        alertEngineService.evaluateUsageAlerts(household, date, spikeConsumption);

        ArgumentCaptor<Alert> alertCaptor = ArgumentCaptor.forClass(Alert.class);
        verify(alertRepository, atLeastOnce()).save(alertCaptor.capture());

        Alert leakAlert = alertCaptor.getAllValues().stream()
                .filter(a -> a.getAlertType() == AlertType.LEAK_DETECTED_2SIGMA)
                .findFirst()
                .orElse(null);

        assertNotNull(leakAlert, "LEAK_DETECTED_2SIGMA alert must be generated");
        assertEquals(AlertSeverity.CRITICAL, leakAlert.getSeverity());
        assertTrue(leakAlert.getSigmaValue().doubleValue() > 2.0);
        verify(emailNotificationService, atLeastOnce()).sendLeakAlert(eq(household), any(Alert.class));
    }
}
