package com.waterbill.dto;

import java.math.BigDecimal;

public class TierChargeBreakdown {

    private String tierName;
    private BigDecimal minVolumeKl;
    private BigDecimal maxVolumeKl;
    private BigDecimal volumeInTierKl;
    private BigDecimal ratePerKl;
    private BigDecimal subtotal;

    public TierChargeBreakdown() {}

    public TierChargeBreakdown(String tierName, BigDecimal minVolumeKl, BigDecimal maxVolumeKl,
                               BigDecimal volumeInTierKl, BigDecimal ratePerKl, BigDecimal subtotal) {
        this.tierName = tierName;
        this.minVolumeKl = minVolumeKl;
        this.maxVolumeKl = maxVolumeKl;
        this.volumeInTierKl = volumeInTierKl;
        this.ratePerKl = ratePerKl;
        this.subtotal = subtotal;
    }

    public String getTierName() { return tierName; }
    public void setTierName(String tierName) { this.tierName = tierName; }

    public BigDecimal getMinVolumeKl() { return minVolumeKl; }
    public void setMinVolumeKl(BigDecimal minVolumeKl) { this.minVolumeKl = minVolumeKl; }

    public BigDecimal getMaxVolumeKl() { return maxVolumeKl; }
    public void setMaxVolumeKl(BigDecimal maxVolumeKl) { this.maxVolumeKl = maxVolumeKl; }

    public BigDecimal getVolumeInTierKl() { return volumeInTierKl; }
    public void setVolumeInTierKl(BigDecimal volumeInTierKl) { this.volumeInTierKl = volumeInTierKl; }

    public BigDecimal getRatePerKl() { return ratePerKl; }
    public void setRatePerKl(BigDecimal ratePerKl) { this.ratePerKl = ratePerKl; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
