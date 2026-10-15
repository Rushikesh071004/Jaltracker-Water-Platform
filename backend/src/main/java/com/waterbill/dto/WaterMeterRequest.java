package com.waterbill.dto;

import com.waterbill.entity.MeterStatus;
import com.waterbill.entity.MeterType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class WaterMeterRequest {

    @NotBlank(message = "Meter serial number is required")
    private String meterSerialNumber;

    private Long householdId;
    private LocalDate installationDate;

    @NotNull(message = "Initial reading is required")
    @DecimalMin(value = "0.0", message = "Initial reading cannot be negative")
    private BigDecimal initialReading = BigDecimal.ZERO;

    private MeterStatus status = MeterStatus.ACTIVE;
    private MeterType meterType = MeterType.DIGITAL;

    public WaterMeterRequest() {}

    public String getMeterSerialNumber() { return meterSerialNumber; }
    public void setMeterSerialNumber(String meterSerialNumber) { this.meterSerialNumber = meterSerialNumber; }

    public Long getHouseholdId() { return householdId; }
    public void setHouseholdId(Long householdId) { this.householdId = householdId; }

    public LocalDate getInstallationDate() { return installationDate; }
    public void setInstallationDate(LocalDate installationDate) { this.installationDate = installationDate; }

    public BigDecimal getInitialReading() { return initialReading; }
    public void setInitialReading(BigDecimal initialReading) { this.initialReading = initialReading; }

    public MeterStatus getStatus() { return status; }
    public void setStatus(MeterStatus status) { this.status = status; }

    public MeterType getMeterType() { return meterType; }
    public void setMeterType(MeterType meterType) { this.meterType = meterType; }
}
