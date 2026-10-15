package com.waterbill.service;

import com.waterbill.dto.WaterMeterRequest;
import com.waterbill.entity.Household;
import com.waterbill.entity.MeterStatus;
import com.waterbill.entity.WaterMeter;
import com.waterbill.repository.HouseholdRepository;
import com.waterbill.repository.WaterMeterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class WaterMeterService {

    private final WaterMeterRepository waterMeterRepository;
    private final HouseholdRepository householdRepository;

    public WaterMeterService(WaterMeterRepository waterMeterRepository, HouseholdRepository householdRepository) {
        this.waterMeterRepository = waterMeterRepository;
        this.householdRepository = householdRepository;
    }

    @Transactional
    public WaterMeter configureMeter(WaterMeterRequest request) {
        if (waterMeterRepository.existsByMeterSerialNumber(request.getMeterSerialNumber())) {
            throw new IllegalArgumentException("Water meter serial number already exists: " + request.getMeterSerialNumber());
        }

        Household household = null;
        if (request.getHouseholdId() != null) {
            household = householdRepository.findById(request.getHouseholdId())
                    .orElseThrow(() -> new IllegalArgumentException("Household not found with ID: " + request.getHouseholdId()));

            if (household.getWaterMeter() != null) {
                throw new IllegalStateException("Household " + household.getFlatNumber() + " already has an assigned water meter: "
                        + household.getWaterMeter().getMeterSerialNumber());
            }
        }

        WaterMeter meter = new WaterMeter(
                request.getMeterSerialNumber(),
                household,
                request.getInstallationDate() != null ? request.getInstallationDate() : LocalDate.now(),
                request.getInitialReading(),
                request.getStatus(),
                request.getMeterType()
        );

        if (household != null) {
            household.setIsMetered(true);
            household.setWaterMeter(meter);
        }

        return waterMeterRepository.save(meter);
    }

    @Transactional(readOnly = true)
    public List<WaterMeter> getAllMeters() {
        return waterMeterRepository.findAll();
    }

    @Transactional(readOnly = true)
    public WaterMeter getMeterById(Long id) {
        return waterMeterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Water meter not found with ID: " + id));
    }

    @Transactional
    public WaterMeter updateMeterStatus(Long id, MeterStatus status) {
        WaterMeter meter = getMeterById(id);
        meter.setStatus(status);
        return waterMeterRepository.save(meter);
    }
}
