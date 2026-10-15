package com.waterbill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TariffTierRequest {

    private Long id;

    @NotBlank(message = "Tier name is required")
    private String tierName;

    @NotNull(message = "Min volume is required")
    @DecimalMin(value = "0.0", message = "Min volume cannot be negative")
    private BigDecimal minVolumeKl;

    private BigDecimal maxVolumeKl; // Null for unbounded

    @NotNull(message = "Rate per kL is required")
    @DecimalMin(value = "0.0", message = "Rate cannot be negative")
    private BigDecimal ratePerKl;

    private Integer tierOrder = 1;

    public TariffTierRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
}
