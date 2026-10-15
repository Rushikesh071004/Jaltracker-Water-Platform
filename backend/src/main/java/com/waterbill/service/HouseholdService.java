package com.waterbill.service;

import com.waterbill.dto.HouseholdRequest;
import com.waterbill.entity.Apartment;
import com.waterbill.entity.Household;
import com.waterbill.repository.ApartmentRepository;
import com.waterbill.repository.HouseholdRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final ApartmentRepository apartmentRepository;

    public HouseholdService(HouseholdRepository householdRepository, ApartmentRepository apartmentRepository) {
        this.householdRepository = householdRepository;
        this.apartmentRepository = apartmentRepository;
    }

    @Transactional
    public Household createHousehold(HouseholdRequest request) {
        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found with ID: " + request.getApartmentId()));

        householdRepository.findByApartmentIdAndFlatNumber(request.getApartmentId(), request.getFlatNumber())
                .ifPresent(h -> {
                    throw new IllegalStateException("Household with flat number " + request.getFlatNumber() + " already exists in this apartment.");
                });

        Household household = new Household(
                apartment,
                request.getFlatNumber(),
                request.getBlockOrWing(),
                request.getFloorNumber(),
                request.getAreaSqft(),
                request.getNumberOfOccupants(),
                request.getIsMetered(),
                request.getDailyUsageThresholdKl()
        );

        return householdRepository.save(household);
    }

    @Transactional(readOnly = true)
    public List<Household> getHouseholdsByApartment(Long apartmentId) {
        return householdRepository.findByApartmentId(apartmentId);
    }

    @Transactional(readOnly = true)
    public Household getHouseholdById(Long id) {
        return householdRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Household not found with ID: " + id));
    }

    @Transactional
    public Household updateHousehold(Long id, HouseholdRequest request) {
        Household household = getHouseholdById(id);
        household.setFlatNumber(request.getFlatNumber());
        household.setBlockOrWing(request.getBlockOrWing());
        household.setFloorNumber(request.getFloorNumber());
        household.setAreaSqft(request.getAreaSqft());
        household.setNumberOfOccupants(request.getNumberOfOccupants());
        household.setIsMetered(request.getIsMetered());
        household.setDailyUsageThresholdKl(request.getDailyUsageThresholdKl());
        return householdRepository.save(household);
    }
}
