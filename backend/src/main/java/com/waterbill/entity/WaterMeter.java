package com.waterbill.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "water_meters")
public class WaterMeter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Meter serial number is required")
    @Column(name = "meter_serial_number", nullable = false, unique = true, length = 100)
    private String meterSerialNumber;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", unique = true)
    private Household household;

    @Column(name = "installation_date")
    private LocalDate installationDate;

    @NotNull(message = "Initial reading is required")
    @Column(name = "initial_reading", nullable = false, precision = 12, scale = 3)
    private BigDecimal initialReading = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MeterStatus status = MeterStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "meter_type", nullable = false, length = 50)
    private MeterType meterType = MeterType.DIGITAL;

    @Column(name = "last_reading_value", precision = 12, scale = 3)
    private BigDecimal lastReadingValue = BigDecimal.ZERO;

    @Column(name = "last_reading_date")
    private LocalDate lastReadingDate;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
        if (this.lastReadingValue == null) {
            this.lastReadingValue = this.initialReading;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public WaterMeter() {}

    public WaterMeter(String meterSerialNumber, Household household, LocalDate installationDate,
                      BigDecimal initialReading, MeterStatus status, MeterType meterType) {
        this.meterSerialNumber = meterSerialNumber;
        this.household = household;
        this.installationDate = installationDate;
        this.initialReading = initialReading;
        this.status = status;
        this.meterType = meterType;
        this.lastReadingValue = initialReading;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMeterSerialNumber() { return meterSerialNumber; }
    public void setMeterSerialNumber(String meterSerialNumber) { this.meterSerialNumber = meterSerialNumber; }

    public Household getHousehold() { return household; }
    public void setHousehold(Household household) { this.household = household; }

    public LocalDate getInstallationDate() { return installationDate; }
    public void setInstallationDate(LocalDate installationDate) { this.installationDate = installationDate; }

    public BigDecimal getInitialReading() { return initialReading; }
    public void setInitialReading(BigDecimal initialReading) { this.initialReading = initialReading; }

    public MeterStatus getStatus() { return status; }
    public void setStatus(MeterStatus status) { this.status = status; }

    public MeterType getMeterType() { return meterType; }
    public void setMeterType(MeterType meterType) { this.meterType = meterType; }

    public BigDecimal getLastReadingValue() { return lastReadingValue; }
    public void setLastReadingValue(BigDecimal lastReadingValue) { this.lastReadingValue = lastReadingValue; }

    public LocalDate getLastReadingDate() { return lastReadingDate; }
    public void setLastReadingDate(LocalDate lastReadingDate) { this.lastReadingDate = lastReadingDate; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
