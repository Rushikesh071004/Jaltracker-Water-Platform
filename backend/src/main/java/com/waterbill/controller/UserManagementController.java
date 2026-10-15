package com.waterbill.controller;

import com.waterbill.dto.UserProfileResponse;
import com.waterbill.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserManagementController {

    private final UserService userService;

    public UserManagementController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<UserProfileResponse>> getPendingUsers(@AuthenticationPrincipal UserDetails userDetails) {
        List<UserProfileResponse> pending = userService.getPendingUsers(userDetails.getUsername());
        return ResponseEntity.ok(pending);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers(@AuthenticationPrincipal UserDetails userDetails) {
        List<UserProfileResponse> users = userService.getAllUsers(userDetails.getUsername());
        return ResponseEntity.ok(users);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<UserProfileResponse> approveUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserProfileResponse updated = userService.approveUser(id, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<UserProfileResponse> rejectUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserProfileResponse updated = userService.rejectUser(id, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<UserProfileResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String status = body != null ? body.getOrDefault("status", "").toUpperCase() : "";
        if ("VERIFIED".equals(status) || "APPROVED".equals(status)) {
            return ResponseEntity.ok(userService.approveUser(id, userDetails.getUsername()));
        } else if ("REJECTED".equals(status)) {
            return ResponseEntity.ok(userService.rejectUser(id, userDetails.getUsername()));
        } else {
            throw new IllegalArgumentException("Invalid status: " + status + ". Allowed: VERIFIED, REJECTED");
        }
    }
}
