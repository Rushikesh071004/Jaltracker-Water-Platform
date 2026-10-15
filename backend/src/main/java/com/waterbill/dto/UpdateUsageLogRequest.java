package com.waterbill.dto;

import java.math.BigDecimal;

public class UpdateUsageLogRequest {

    private BigDecimal currentReadingKl;
    private BigDecimal dailyConsumptionKl;
    private String notes;

    public UpdateUsageLogRequest() {}

    public BigDecimal getCurrentReadingKl() { return currentReadingKl; }
    public void setCurrentReadingKl(BigDecimal currentReadingKl) { this.currentReadingKl = currentReadingKl; }

    public BigDecimal getDailyConsumptionKl() { return dailyConsumptionKl; }
    public void setDailyConsumptionKl(BigDecimal dailyConsumptionKl) { this.dailyConsumptionKl = dailyConsumptionKl; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
