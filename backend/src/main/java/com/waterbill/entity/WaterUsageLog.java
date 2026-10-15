package com.waterbill.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

@Entity
@Table(name = "water_usage_logs", uniqueConstraints = {
    @UniqueConstraint(name = "uq_household_reading_date", columnNames = {"household_id", "reading_date"})
})
@Audited
public class WaterUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @NotNull(message = "Household is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id")
    private WaterMeter meter;

    @NotNull(message = "Reading date is required")
    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    @NotNull(message = "Current reading is required")
    @DecimalMin(value = "0.0", message = "Reading cannot be negative")
    @Column(name = "current_reading_kl", nullable = false, precision = 12, scale = 3)
    private BigDecimal currentReadingKl;

    @NotNull(message = "Daily consumption is required")
    @DecimalMin(value = "0.0", message = "Daily consumption cannot be negative")
    @Column(name = "daily_consumption_kl", nullable = false, precision = 10, scale = 3)
    private BigDecimal dailyConsumptionKl;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_source", nullable = false, length = 50)
    private LogSource logSource = LogSource.MANUAL_ENTRY;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public WaterUsageLog() {}

    public WaterUsageLog(Household household, WaterMeter meter, LocalDate readingDate,
                         BigDecimal currentReadingKl, BigDecimal dailyConsumptionKl,
                         LogSource logSource, String notes) {
        this.household = household;
        this.meter = meter;
        this.readingDate = readingDate;
        this.currentReadingKl = currentReadingKl;
        this.dailyConsumptionKl = dailyConsumptionKl;
        this.logSource = logSource;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Household getHousehold() { return household; }
    public void setHousehold(Household household) { this.household = household; }

    public WaterMeter getMeter() { return meter; }
    public void setMeter(WaterMeter meter) { this.meter = meter; }

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

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
