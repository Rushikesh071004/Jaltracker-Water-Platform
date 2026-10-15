package com.waterbill.controller;

import com.waterbill.audit.AuditService;
import com.waterbill.dto.AuditRecordDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<List<AuditRecordDto>> getUserAuditTrail(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.getUserAuditTrail(id));
    }

    @GetMapping("/usage-logs/{id}")
    public ResponseEntity<List<AuditRecordDto>> getUsageLogAuditTrail(@PathVariable Long id) {
        return ResponseEntity.ok(auditService.getUsageLogAuditTrail(id));
    }
}
