package com.waterbill.controller;

import com.waterbill.entity.Alert;
import com.waterbill.entity.AlertStatus;
import com.waterbill.service.AlertEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertEngineService alertEngineService;

    public AlertController(AlertEngineService alertEngineService) {
        this.alertEngineService = alertEngineService;
    }

    @GetMapping
    public ResponseEntity<List<Alert>> getAlerts(
            @RequestParam(required = false) Long apartmentId,
            @RequestParam(required = false) Long householdId) {
        if (householdId != null) {
            return ResponseEntity.ok(alertEngineService.getAlertsByHousehold(householdId));
        } else if (apartmentId != null) {
            return ResponseEntity.ok(alertEngineService.getAlertsByApartment(apartmentId));
        }
        return ResponseEntity.badRequest().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Alert> updateAlertStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String statusStr = payload.get("status");
        if (statusStr == null) {
            throw new IllegalArgumentException("status is required");
        }
        AlertStatus status = AlertStatus.valueOf(statusStr.toUpperCase());
        Alert updated = alertEngineService.updateAlertStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/scan")
    public ResponseEntity<Map<String, String>> triggerAnomalyScan() {
        alertEngineService.runScheduledAnomalyScan();
        return ResponseEntity.ok(Map.of("message", "Water usage anomaly scan completed successfully."));
    }
}
