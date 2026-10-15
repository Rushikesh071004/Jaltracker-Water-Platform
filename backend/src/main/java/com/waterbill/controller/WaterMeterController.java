package com.waterbill.controller;

import com.waterbill.dto.WaterMeterRequest;
import com.waterbill.entity.MeterStatus;
import com.waterbill.entity.WaterMeter;
import com.waterbill.service.WaterMeterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/meters")
public class WaterMeterController {

    private final WaterMeterService waterMeterService;

    public WaterMeterController(WaterMeterService waterMeterService) {
        this.waterMeterService = waterMeterService;
    }

    @PostMapping
    public ResponseEntity<WaterMeter> configureMeter(@Valid @RequestBody WaterMeterRequest request) {
        WaterMeter created = waterMeterService.configureMeter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<WaterMeter>> getAllMeters() {
        return ResponseEntity.ok(waterMeterService.getAllMeters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WaterMeter> getMeterById(@PathVariable Long id) {
        return ResponseEntity.ok(waterMeterService.getMeterById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<WaterMeter> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String statusStr = payload.get("status");
        if (statusStr == null) {
            throw new IllegalArgumentException("status is required");
        }
        MeterStatus status = MeterStatus.valueOf(statusStr.toUpperCase());
        return ResponseEntity.ok(waterMeterService.updateMeterStatus(id, status));
    }
}
