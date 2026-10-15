package com.waterbill.controller;

import com.waterbill.dto.HouseholdRequest;
import com.waterbill.dto.UserProfileResponse;
import com.waterbill.entity.Household;
import com.waterbill.service.HouseholdService;
import com.waterbill.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/households")
public class HouseholdController {

    private final HouseholdService householdService;
    private final UserService userService;

    public HouseholdController(HouseholdService householdService, UserService userService) {
        this.householdService = householdService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Household> createHousehold(@Valid @RequestBody HouseholdRequest request) {
        Household created = householdService.createHousehold(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Household>> getHouseholds(@RequestParam Long apartmentId) {
        return ResponseEntity.ok(householdService.getHouseholdsByApartment(apartmentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Household> getHouseholdById(@PathVariable Long id) {
        return ResponseEntity.ok(householdService.getHouseholdById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Household> updateHousehold(@PathVariable Long id, @Valid @RequestBody HouseholdRequest request) {
        return ResponseEntity.ok(householdService.updateHousehold(id, request));
    }

    @PostMapping("/{id}/assign-resident")
    public ResponseEntity<UserProfileResponse> assignResident(
            @PathVariable Long id,
            @RequestBody Map<String, Long> payload) {
        Long userId = payload.get("userId");
        if (userId == null) {
            throw new IllegalArgumentException("userId is required in payload");
        }
        UserProfileResponse updated = userService.assignResidentToHousehold(userId, id);
        return ResponseEntity.ok(updated);
    }
}
