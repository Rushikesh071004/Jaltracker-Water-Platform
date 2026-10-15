package com.waterbill.controller;

import com.waterbill.dto.CsvUploadResult;
import com.waterbill.dto.UpdateUsageLogRequest;
import com.waterbill.dto.UsageLogRequest;
import com.waterbill.entity.WaterUsageLog;
import com.waterbill.service.WaterUsageService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class WaterUsageController {

    private final WaterUsageService waterUsageService;

    public WaterUsageController(WaterUsageService waterUsageService) {
        this.waterUsageService = waterUsageService;
    }

    @PostMapping("/usage/log")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<WaterUsageLog> logDailyUsage(@Valid @RequestBody UsageLogRequest request) {
        WaterUsageLog saved = waterUsageService.recordDailyUsage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Requirement 3: Backend PUT /api/usage-logs/{id} for ADMIN and SUPERADMIN roles to edit/update readings.
     */
    @PutMapping({"/usage-logs/{id}", "/usage/logs/{id}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<WaterUsageLog> updateUsageLog(
            @PathVariable Long id,
            @RequestBody UpdateUsageLogRequest request) {
        WaterUsageLog updated = waterUsageService.updateUsageLog(
                id,
                request.getCurrentReadingKl(),
                request.getDailyConsumptionKl(),
                request.getNotes()
        );
        return ResponseEntity.ok(updated);
    }

    @PostMapping(value = "/usage/upload-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<CsvUploadResult> uploadBulkCsv(
            @RequestParam Long apartmentId,
            @RequestParam("file") MultipartFile file) {
        CsvUploadResult result = waterUsageService.processBulkCsv(apartmentId, file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/usage/history")
    public ResponseEntity<List<WaterUsageLog>> getUsageHistory(
            @RequestParam Long householdId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<WaterUsageLog> history = waterUsageService.getHouseholdUsageHistory(householdId, startDate, endDate);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/usage/apartment-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<WaterUsageLog>> getApartmentLogs(@RequestParam Long apartmentId) {
        List<WaterUsageLog> logs = waterUsageService.getRecentApartmentLogs(apartmentId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/usage/apartment-logs/paged")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<org.springframework.data.domain.Page<WaterUsageLog>> getApartmentLogsPaged(
            @RequestParam Long apartmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return ResponseEntity.ok(waterUsageService.getApartmentLogsPaged(apartmentId, pageable));
    }
}
