package com.waterbill.controller;

import com.waterbill.dto.ApartmentRequest;
import com.waterbill.entity.Apartment;
import com.waterbill.service.ApartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/apartments")
public class ApartmentController {

    private final ApartmentService apartmentService;

    public ApartmentController(ApartmentService apartmentService) {
        this.apartmentService = apartmentService;
    }

    @PostMapping
    public ResponseEntity<Apartment> createApartment(@Valid @RequestBody ApartmentRequest request) {
        Apartment created = apartmentService.createApartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Apartment>> getAllApartments() {
        return ResponseEntity.ok(apartmentService.getAllApartments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Apartment> getApartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(apartmentService.getApartmentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Apartment> updateApartment(@PathVariable Long id, @Valid @RequestBody ApartmentRequest request) {
        return ResponseEntity.ok(apartmentService.updateApartment(id, request));
    }
}
