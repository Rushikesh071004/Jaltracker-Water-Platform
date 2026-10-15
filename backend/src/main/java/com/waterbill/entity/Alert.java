package com.waterbill.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "alerts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Household is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false, length = 50)
    private AlertType alertType = AlertType.OVERUSE_THRESHOLD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlertSeverity severity = AlertSeverity.WARNING;

    @NotBlank(message = "Title is required")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "Message is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @NotNull(message = "Reading date is required")
    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    @NotNull(message = "Consumption value is required")
    @Column(name = "consumption_kl", nullable = false, precision = 10, scale = 3)
    private BigDecimal consumptionKl;

    @Column(name = "threshold_kl", precision = 10, scale = 3)
    private BigDecimal thresholdKl;

    @Column(name = "sigma_value", precision = 6, scale = 2)
    private BigDecimal sigmaValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AlertStatus status = AlertStatus.ACTIVE;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public Alert() {}

    public Alert(Household household, AlertType alertType, AlertSeverity severity,
                 String title, String message, LocalDate readingDate,
                 BigDecimal consumptionKl, BigDecimal thresholdKl, BigDecimal sigmaValue) {
        this.household = household;
        this.alertType = alertType;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.readingDate = readingDate;
        this.consumptionKl = consumptionKl;
        this.thresholdKl = thresholdKl;
        this.sigmaValue = sigmaValue;
        this.status = AlertStatus.ACTIVE;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Household getHousehold() { return household; }
    public void setHousehold(Household household) { this.household = household; }

    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDate getReadingDate() { return readingDate; }
    public void setReadingDate(LocalDate readingDate) { this.readingDate = readingDate; }

    public BigDecimal getConsumptionKl() { return consumptionKl; }
    public void setConsumptionKl(BigDecimal consumptionKl) { this.consumptionKl = consumptionKl; }

    public BigDecimal getThresholdKl() { return thresholdKl; }
    public void setThresholdKl(BigDecimal thresholdKl) { this.thresholdKl = thresholdKl; }

    public BigDecimal getSigmaValue() { return sigmaValue; }
    public void setSigmaValue(BigDecimal sigmaValue) { this.sigmaValue = sigmaValue; }

    public AlertStatus getStatus() { return status; }
    public void setStatus(AlertStatus status) { this.status = status; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
