package com.waterbill.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "households", uniqueConstraints = {
    @UniqueConstraint(name = "uq_apartment_household", columnNames = {"apartment_id", "flat_number", "block_or_wing"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Apartment is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @NotBlank(message = "Flat number is required")
    @Column(name = "flat_number", nullable = false, length = 50)
    private String flatNumber;

    @Column(name = "block_or_wing", length = 50)
    private String blockOrWing;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @NotNull(message = "Area is required")
    @DecimalMin(value = "1.0", message = "Area must be at least 1 sqft")
    @Column(name = "area_sqft", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaSqft = BigDecimal.valueOf(1000.00);

    @Positive(message = "Number of occupants must be at least 1")
    @Column(name = "number_of_occupants", nullable = false)
    private Integer numberOfOccupants = 1;

    @Column(name = "is_metered", nullable = false)
    private Boolean isMetered = true;

    @Column(name = "daily_usage_threshold_kl", precision = 10, scale = 3)
    private BigDecimal dailyUsageThresholdKl = BigDecimal.valueOf(1.500);

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @OneToOne(mappedBy = "household", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private WaterMeter waterMeter;

    @JsonIgnore
    @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WaterUsageLog> usageLogs = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invoice> invoices = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "household", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alert> alerts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "household")
    private List<User> residents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Household() {}

    public Household(Apartment apartment, String flatNumber, String blockOrWing, Integer floorNumber,
                     BigDecimal areaSqft, Integer numberOfOccupants, Boolean isMetered, BigDecimal dailyUsageThresholdKl) {
        this.apartment = apartment;
        this.flatNumber = flatNumber;
        this.blockOrWing = blockOrWing;
        this.floorNumber = floorNumber;
        this.areaSqft = areaSqft;
        this.numberOfOccupants = numberOfOccupants;
        this.isMetered = isMetered;
        this.dailyUsageThresholdKl = dailyUsageThresholdKl;
    }

    public Household(Apartment apartment, String flatNumber, BigDecimal areaSqft, Boolean isMetered) {
        this.apartment = apartment;
        this.flatNumber = flatNumber;
        this.blockOrWing = "Tower A";
        this.floorNumber = 1;
        this.areaSqft = areaSqft != null ? areaSqft : new BigDecimal("1000.00");
        this.numberOfOccupants = 2;
        this.isMetered = isMetered != null ? isMetered : true;
        this.dailyUsageThresholdKl = new BigDecimal("1.500");
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Apartment getApartment() { return apartment; }
    public void setApartment(Apartment apartment) { this.apartment = apartment; }

    public String getFlatNumber() { return flatNumber; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }

    public String getBlockOrWing() { return blockOrWing; }
    public void setBlockOrWing(String blockOrWing) { this.blockOrWing = blockOrWing; }

    public Integer getFloorNumber() { return floorNumber; }
    public void setFloorNumber(Integer floorNumber) { this.floorNumber = floorNumber; }

    public BigDecimal getAreaSqft() { return areaSqft; }
    public void setAreaSqft(BigDecimal areaSqft) { this.areaSqft = areaSqft; }

    public Integer getNumberOfOccupants() { return numberOfOccupants; }
    public void setNumberOfOccupants(Integer numberOfOccupants) { this.numberOfOccupants = numberOfOccupants; }

    public Boolean getIsMetered() { return isMetered; }
    public void setIsMetered(Boolean isMetered) { this.isMetered = isMetered; }

    public BigDecimal getDailyUsageThresholdKl() { return dailyUsageThresholdKl; }
    public void setDailyUsageThresholdKl(BigDecimal dailyUsageThresholdKl) { this.dailyUsageThresholdKl = dailyUsageThresholdKl; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public WaterMeter getWaterMeter() { return waterMeter; }
    public void setWaterMeter(WaterMeter waterMeter) { this.waterMeter = waterMeter; }

    public List<WaterUsageLog> getUsageLogs() { return usageLogs; }
    public void setUsageLogs(List<WaterUsageLog> usageLogs) { this.usageLogs = usageLogs; }

    public List<Invoice> getInvoices() { return invoices; }
    public void setInvoices(List<Invoice> invoices) { this.invoices = invoices; }

    public List<Alert> getAlerts() { return alerts; }
    public void setAlerts(List<Alert> alerts) { this.alerts = alerts; }

    public List<User> getResidents() { return residents; }
    public void setResidents(List<User> residents) { this.residents = residents; }
}
