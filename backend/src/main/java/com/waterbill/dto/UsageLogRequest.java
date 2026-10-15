package com.waterbill.dto;

import com.waterbill.entity.LogSource;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class UsageLogRequest {

    @NotNull(message = "Household ID is required")
    private Long householdId;

    private Long meterId;

    @NotNull(message = "Reading date is required")
    private LocalDate readingDate;

    @NotNull(message = "Current reading is required")
    @DecimalMin(value = "0.0", message = "Reading cannot be negative")
    private BigDecimal currentReadingKl;

    private BigDecimal dailyConsumptionKl;

    private LogSource logSource = LogSource.MANUAL_ENTRY;

    private String notes;

    public UsageLogRequest() {}

    public Long getHouseholdId() { return householdId; }
    public void setHouseholdId(Long householdId) { this.householdId = householdId; }

    public Long getMeterId() { return meterId; }
    public void setMeterId(Long meterId) { this.meterId = meterId; }

    public LocalDate getReadingDate() { return readingDate; }
    public void setReadingDate(LocalDate readingDate) { this.readingDate = readingDate; }

    public BigDecimal getCurrentReadingKl() { return currentReadingKl; }
    public void setCurrentReadingKl(BigDecimal currentReadingKl) { this.currentReadingKl = currentReadingKl; }

    public BigDecimal getDailyConsumptionKl() { return dailyConsumptionKl; }
    public void setDailyConsumptionKl(BigDecimal dailyConsumptionKl) { this.dailyConsumptionKl = dailyConsumptionKl; }

    public LogSource getLogSource() { return logSource; }
    public void setLogSource(LogSource logSource) { this.logSource = logSource; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
