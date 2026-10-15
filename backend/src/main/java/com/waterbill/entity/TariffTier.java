package com.waterbill.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tariff_tiers")
public class TariffTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Tariff plan reference is required")
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_plan_id", nullable = false)
    private TariffPlan tariffPlan;

    @NotBlank(message = "Tier name is required")
    @Column(name = "tier_name", nullable = false, length = 100)
    private String tierName;

    @NotNull(message = "Min volume is required")
    @DecimalMin(value = "0.0", message = "Min volume cannot be negative")
    @Column(name = "min_volume_kl", nullable = false, precision = 10, scale = 3)
    private BigDecimal minVolumeKl = BigDecimal.ZERO;

    @Column(name = "max_volume_kl", precision = 10, scale = 3)
    private BigDecimal maxVolumeKl; // Null means infinity / open upper boundary

    @NotNull(message = "Rate per kL is required")
    @DecimalMin(value = "0.0", message = "Rate cannot be negative")
    @Column(name = "rate_per_kl", nullable = false, precision = 10, scale = 2)
    private BigDecimal ratePerKl;

    @Column(name = "tier_order", nullable = false)
    private Integer tierOrder = 1;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public TariffTier() {}

    public TariffTier(TariffPlan tariffPlan, String tierName, BigDecimal minVolumeKl,
                      BigDecimal maxVolumeKl, BigDecimal ratePerKl, Integer tierOrder) {
        this.tariffPlan = tariffPlan;
        this.tierName = tierName;
        this.minVolumeKl = minVolumeKl;
        this.maxVolumeKl = maxVolumeKl;
        this.ratePerKl = ratePerKl;
        this.tierOrder = tierOrder;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TariffPlan getTariffPlan() { return tariffPlan; }
    public void setTariffPlan(TariffPlan tariffPlan) { this.tariffPlan = tariffPlan; }

    public String getTierName() { return tierName; }
    public void setTierName(String tierName) { this.tierName = tierName; }

    public BigDecimal getMinVolumeKl() { return minVolumeKl; }
    public void setMinVolumeKl(BigDecimal minVolumeKl) { this.minVolumeKl = minVolumeKl; }

    public BigDecimal getMaxVolumeKl() { return maxVolumeKl; }
    public void setMaxVolumeKl(BigDecimal maxVolumeKl) { this.maxVolumeKl = maxVolumeKl; }

    public BigDecimal getRatePerKl() { return ratePerKl; }
    public void setRatePerKl(BigDecimal ratePerKl) { this.ratePerKl = ratePerKl; }

    public Integer getTierOrder() { return tierOrder; }
    public void setTierOrder(Integer tierOrder) { this.tierOrder = tierOrder; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
