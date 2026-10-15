package com.waterbill.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class HouseholdBillSummary {

    private Long householdId;
    private String flatNumber;
    private String residentName;
    private String residentEmail;
    private BigDecimal areaSqft;
    private Boolean isMetered;
    private BigDecimal meteredConsumptionKl = BigDecimal.ZERO;
    private List<TierChargeBreakdown> tierBreakdowns = new ArrayList<>();
    private BigDecimal meteredCharge = BigDecimal.ZERO;
    private BigDecimal sharedCommonCharge = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private String invoiceNumber;

    public HouseholdBillSummary() {}

    public Long getHouseholdId() { return householdId; }
    public void setHouseholdId(Long householdId) { this.householdId = householdId; }

    public String getFlatNumber() { return flatNumber; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public String getResidentEmail() { return residentEmail; }
    public void setResidentEmail(String residentEmail) { this.residentEmail = residentEmail; }

    public BigDecimal getAreaSqft() { return areaSqft; }
    public void setAreaSqft(BigDecimal areaSqft) { this.areaSqft = areaSqft; }

    public Boolean getIsMetered() { return isMetered; }
    public void setIsMetered(Boolean metered) { isMetered = metered; }

    public BigDecimal getMeteredConsumptionKl() { return meteredConsumptionKl; }
    public void setMeteredConsumptionKl(BigDecimal meteredConsumptionKl) { this.meteredConsumptionKl = meteredConsumptionKl; }

    public List<TierChargeBreakdown> getTierBreakdowns() { return tierBreakdowns; }
    public void setTierBreakdowns(List<TierChargeBreakdown> tierBreakdowns) { this.tierBreakdowns = tierBreakdowns; }

    public BigDecimal getMeteredCharge() { return meteredCharge; }
    public void setMeteredCharge(BigDecimal meteredCharge) { this.meteredCharge = meteredCharge; }

    public BigDecimal getSharedCommonCharge() { return sharedCommonCharge; }
    public void setSharedCommonCharge(BigDecimal sharedCommonCharge) { this.sharedCommonCharge = sharedCommonCharge; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
}
