package com.waterbill.service;

import com.waterbill.dto.ApartmentRequest;
import com.waterbill.entity.Apartment;
import com.waterbill.repository.ApartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;

    public ApartmentService(ApartmentRepository apartmentRepository) {
        this.apartmentRepository = apartmentRepository;
    }

    @Transactional
    public Apartment createApartment(ApartmentRequest request) {
        Apartment apartment = new Apartment(
                request.getName(),
                request.getAddress(),
                request.getTotalFlats(),
                request.getContactEmail(),
                request.getContactPhone()
        );
        return apartmentRepository.save(apartment);
    }

    @Transactional(readOnly = true)
    public List<Apartment> getAllApartments() {
        return apartmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Apartment getApartmentById(Long id) {
        return apartmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found with ID: " + id));
    }

    @Transactional
    public Apartment updateApartment(Long id, ApartmentRequest request) {
        Apartment apartment = getApartmentById(id);
        apartment.setName(request.getName());
        apartment.setAddress(request.getAddress());
        apartment.setTotalFlats(request.getTotalFlats());
        apartment.setContactEmail(request.getContactEmail());
        apartment.setContactPhone(request.getContactPhone());
        return apartmentRepository.save(apartment);
    }
}
