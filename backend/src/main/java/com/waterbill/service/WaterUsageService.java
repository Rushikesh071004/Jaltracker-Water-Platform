package com.waterbill.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.waterbill.dto.CsvUploadResult;
import com.waterbill.dto.UsageLogRequest;
import com.waterbill.entity.Household;
import com.waterbill.entity.LogSource;
import com.waterbill.entity.WaterMeter;
import com.waterbill.entity.WaterUsageLog;
import com.waterbill.repository.HouseholdRepository;
import com.waterbill.repository.WaterMeterRepository;
import com.waterbill.repository.WaterUsageLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class WaterUsageService {

    private static final Logger log = LoggerFactory.getLogger(WaterUsageService.class);

    private final WaterUsageLogRepository usageLogRepository;
    private final HouseholdRepository householdRepository;
    private final WaterMeterRepository waterMeterRepository;
    private final AlertEngineService alertEngineService;

    public WaterUsageService(WaterUsageLogRepository usageLogRepository,
                             HouseholdRepository householdRepository,
                             WaterMeterRepository waterMeterRepository,
                             AlertEngineService alertEngineService) {
        this.usageLogRepository = usageLogRepository;
        this.householdRepository = householdRepository;
        this.waterMeterRepository = waterMeterRepository;
        this.alertEngineService = alertEngineService;
    }

    /**
     * Records a manual daily usage log with duplicate detection and dynamic consumption calculation.
     */
    @Transactional
    public WaterUsageLog recordDailyUsage(UsageLogRequest request) {
        Household household = householdRepository.findById(request.getHouseholdId())
                .orElseThrow(() -> new IllegalArgumentException("Household not found with ID: " + request.getHouseholdId()));

        if (usageLogRepository.existsByHouseholdIdAndReadingDate(household.getId(), request.getReadingDate())) {
            throw new IllegalStateException(String.format("Duplicate reading: Log for flat %s on date %s already exists.",
                    household.getFlatNumber(), request.getReadingDate()));
        }

        WaterMeter meter = household.getWaterMeter();
        if (request.getMeterId() != null) {
            meter = waterMeterRepository.findById(request.getMeterId()).orElse(meter);
        }

        BigDecimal dailyConsumption = request.getDailyConsumptionKl();
        if (dailyConsumption == null) {
            if (meter != null && meter.getLastReadingValue() != null && request.getCurrentReadingKl().compareTo(meter.getLastReadingValue()) >= 0) {
                dailyConsumption = request.getCurrentReadingKl().subtract(meter.getLastReadingValue());
            } else {
                dailyConsumption = BigDecimal.ZERO;
            }
        }

        WaterUsageLog usageLog = new WaterUsageLog(
                household,
                meter,
                request.getReadingDate(),
                request.getCurrentReadingKl().setScale(3, RoundingMode.HALF_UP),
                dailyConsumption.setScale(3, RoundingMode.HALF_UP),
                request.getLogSource() != null ? request.getLogSource() : LogSource.MANUAL_ENTRY,
                request.getNotes()
        );

        if (meter != null) {
            if (meter.getLastReadingDate() == null || !request.getReadingDate().isBefore(meter.getLastReadingDate())) {
                meter.setLastReadingValue(request.getCurrentReadingKl());
                meter.setLastReadingDate(request.getReadingDate());
                waterMeterRepository.save(meter);
            }
        }

        WaterUsageLog saved = usageLogRepository.save(usageLog);

        // Evaluate alert conditions
        alertEngineService.evaluateUsageAlerts(household, request.getReadingDate(), dailyConsumption);

        return saved;
    }

    /**
     * Parses and ingests bulk CSV meter readings with validation, format checks, and duplicate detection.
     * Expected CSV headers: flat_number, reading_date, current_reading_kl, notes
     */
    @Transactional
    public CsvUploadResult processBulkCsv(Long apartmentId, MultipartFile file) {
        CsvUploadResult result = new CsvUploadResult();

        if (file == null || file.isEmpty()) {
            result.addError("Uploaded file is empty.");
            return result;
        }

        List<Household> households = householdRepository.findByApartmentId(apartmentId);
        Map<String, Household> flatToHouseholdMap = new HashMap<>();
        for (Household h : households) {
            flatToHouseholdMap.put(h.getFlatNumber().trim().toUpperCase(), h);
        }

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] header = reader.readNext();
            if (header == null) {
                result.addError("CSV file contains no header row.");
                return result;
            }

            int flatIdx = -1;
            int dateIdx = -1;
            int readingIdx = -1;
            int notesIdx = -1;

            for (int i = 0; i < header.length; i++) {
                String col = header[i].trim().toLowerCase().replace(" ", "_");
                if (col.contains("flat") || col.contains("household")) flatIdx = i;
                else if (col.contains("date")) dateIdx = i;
                else if (col.contains("reading") || col.contains("consumption")) readingIdx = i;
                else if (col.contains("note")) notesIdx = i;
            }

            if (flatIdx == -1 || dateIdx == -1 || readingIdx == -1) {
                result.addError("Invalid CSV headers. Required columns: flat_number, reading_date, current_reading_kl");
                return result;
            }

            String[] line;
            int rowNum = 1;
            Set<String> processedInBatch = new HashSet<>();

            while ((line = reader.readNext()) != null) {
                rowNum++;
                result.setTotalRows(result.getTotalRows() + 1);

                if (line.length <= Math.max(flatIdx, Math.max(dateIdx, readingIdx))) {
                    result.setFailedRows(result.getFailedRows() + 1);
                    result.addError(String.format("Row %d: Missing required column values.", rowNum));
                    continue;
                }

                String flatNumber = line[flatIdx].trim();
                String dateStr = line[dateIdx].trim();
                String readingStr = line[readingIdx].trim();
                String notes = (notesIdx >= 0 && notesIdx < line.length) ? line[notesIdx].trim() : "Bulk CSV Upload";

                if (flatNumber.isEmpty() || dateStr.isEmpty() || readingStr.isEmpty()) {
                    result.setFailedRows(result.getFailedRows() + 1);
                    result.addError(String.format("Row %d: Empty required fields.", rowNum));
                    continue;
                }

                Household household = flatToHouseholdMap.get(flatNumber.toUpperCase());
                if (household == null) {
                    result.setFailedRows(result.getFailedRows() + 1);
                    result.addError(String.format("Row %d: Flat '%s' not found in apartment.", rowNum, flatNumber));
                    continue;
                }

                LocalDate readingDate;
                try {
                    readingDate = LocalDate.parse(dateStr);
                } catch (DateTimeParseException e) {
                    result.setFailedRows(result.getFailedRows() + 1);
                    result.addError(String.format("Row %d: Invalid date format '%s'. Use yyyy-MM-dd.", rowNum, dateStr));
                    continue;
                }

                BigDecimal currentReading;
                try {
                    currentReading = new BigDecimal(readingStr);
                    if (currentReading.compareTo(BigDecimal.ZERO) < 0) {
                        result.setFailedRows(result.getFailedRows() + 1);
                        result.addError(String.format("Row %d: Reading cannot be negative (found '%s').", rowNum, readingStr));
                        continue;
                    }
                } catch (NumberFormatException e) {
                    result.setFailedRows(result.getFailedRows() + 1);
                    result.addError(String.format("Row %d: Invalid numeric reading '%s'.", rowNum, readingStr));
                    continue;
                }

                // Duplicate Check in Database
                if (usageLogRepository.existsByHouseholdIdAndReadingDate(household.getId(), readingDate)) {
                    result.setSkippedDuplicates(result.getSkippedDuplicates() + 1);
                    result.addWarning(String.format("Row %d: Skipped duplicate reading for flat '%s' on %s.", rowNum, flatNumber, readingDate));
                    continue;
                }

                // Duplicate Check in Current Batch
                String batchKey = household.getId() + "_" + readingDate;
                if (processedInBatch.contains(batchKey)) {
                    result.setSkippedDuplicates(result.getSkippedDuplicates() + 1);
                    result.addWarning(String.format("Row %d: Duplicate entry in CSV for flat '%s' on %s.", rowNum, flatNumber, readingDate));
                    continue;
                }
                processedInBatch.add(batchKey);

                // Compute daily consumption
                WaterMeter meter = household.getWaterMeter();
                BigDecimal dailyConsumption = BigDecimal.ZERO;
                if (meter != null && meter.getLastReadingValue() != null && currentReading.compareTo(meter.getLastReadingValue()) >= 0) {
                    dailyConsumption = currentReading.subtract(meter.getLastReadingValue());
                }

                WaterUsageLog logEntry = new WaterUsageLog(
                        household,
                        meter,
                        readingDate,
                        currentReading.setScale(3, RoundingMode.HALF_UP),
                        dailyConsumption.setScale(3, RoundingMode.HALF_UP),
                        LogSource.CSV_BULK_UPLOAD,
                        notes
                );

                if (meter != null) {
                    if (meter.getLastReadingDate() == null || !readingDate.isBefore(meter.getLastReadingDate())) {
                        meter.setLastReadingValue(currentReading);
                        meter.setLastReadingDate(readingDate);
                        waterMeterRepository.save(meter);
                    }
                }

                usageLogRepository.save(logEntry);
                result.setSuccessfulRows(result.getSuccessfulRows() + 1);

                // Trigger alert evaluation
                alertEngineService.evaluateUsageAlerts(household, readingDate, dailyConsumption);
            }

        } catch (CsvValidationException | java.io.IOException e) {
            log.error("Failed to parse CSV file: {}", e.getMessage(), e);
            result.addError("File parsing failed: " + e.getMessage());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<WaterUsageLog> getHouseholdUsageHistory(Long householdId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return usageLogRepository.findByHouseholdIdAndReadingDateBetweenOrderByReadingDateAsc(householdId, startDate, endDate);
        }
        return usageLogRepository.findByHouseholdIdOrderByReadingDateDesc(householdId);
    }

    @Transactional(readOnly = true)
    public List<WaterUsageLog> getRecentApartmentLogs(Long apartmentId) {
        return usageLogRepository.findTop50ByHouseholdApartmentIdOrderByReadingDateDesc(apartmentId);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<WaterUsageLog> getApartmentLogsPaged(Long apartmentId, org.springframework.data.domain.Pageable pageable) {
        return usageLogRepository.findByHouseholdApartmentIdOrderByReadingDateDesc(apartmentId, pageable);
    }

    @Transactional
    public WaterUsageLog updateUsageLog(Long id, BigDecimal newCurrentReadingKl, BigDecimal newDailyConsumptionKl, String notes) {
        WaterUsageLog logEntry = usageLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Water usage log not found with ID: " + id));

        if (newCurrentReadingKl != null) {
            logEntry.setCurrentReadingKl(newCurrentReadingKl.setScale(3, RoundingMode.HALF_UP));
        }

        if (newDailyConsumptionKl != null) {
            logEntry.setDailyConsumptionKl(newDailyConsumptionKl.setScale(3, RoundingMode.HALF_UP));
        } else if (newCurrentReadingKl != null) {
            Optional<WaterUsageLog> prevLog = usageLogRepository.findFirstByHouseholdIdAndReadingDateLessThanOrderByReadingDateDesc(
                    logEntry.getHousehold().getId(), logEntry.getReadingDate()
            );
            if (prevLog.isPresent() && prevLog.get().getCurrentReadingKl() != null) {
                BigDecimal diff = newCurrentReadingKl.subtract(prevLog.get().getCurrentReadingKl());
                logEntry.setDailyConsumptionKl(diff.max(BigDecimal.ZERO).setScale(3, RoundingMode.HALF_UP));
            }
        }

        if (notes != null) {
            logEntry.setNotes(notes);
        }

        WaterUsageLog saved = usageLogRepository.save(logEntry);

        // Update meter if this corresponds to the latest reading
        WaterMeter meter = logEntry.getMeter();
        if (meter != null && logEntry.getReadingDate().equals(meter.getLastReadingDate())) {
            meter.setLastReadingValue(logEntry.getCurrentReadingKl());
            waterMeterRepository.save(meter);
        }

        // Re-evaluate alerts
        alertEngineService.evaluateUsageAlerts(logEntry.getHousehold(), logEntry.getReadingDate(), logEntry.getDailyConsumptionKl());

        return saved;
    }
}
