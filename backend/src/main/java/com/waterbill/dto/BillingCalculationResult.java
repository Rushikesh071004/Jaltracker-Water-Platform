package com.waterbill.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BillingCalculationResult {

    private Long cycleId;
    private String cycleName;
    private BigDecimal totalBulkPurchasedVolumeKl = BigDecimal.ZERO;
    private BigDecimal totalBulkPurchasedCost = BigDecimal.ZERO;
    private BigDecimal totalMeteredConsumptionKl = BigDecimal.ZERO;
    private BigDecimal totalMeteredCharges = BigDecimal.ZERO;
    private BigDecimal totalSharedCommonCharges = BigDecimal.ZERO;
    private BigDecimal totalCycleBilling = BigDecimal.ZERO;
    private int householdsBilledCount;
    private List<HouseholdBillSummary> householdBills = new ArrayList<>();

    public BillingCalculationResult() {}

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }

    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }

    public BigDecimal getTotalBulkPurchasedVolumeKl() { return totalBulkPurchasedVolumeKl; }
    public void setTotalBulkPurchasedVolumeKl(BigDecimal totalBulkPurchasedVolumeKl) { this.totalBulkPurchasedVolumeKl = totalBulkPurchasedVolumeKl; }

    public BigDecimal getTotalBulkPurchasedCost() { return totalBulkPurchasedCost; }
    public void setTotalBulkPurchasedCost(BigDecimal totalBulkPurchasedCost) { this.totalBulkPurchasedCost = totalBulkPurchasedCost; }

    public BigDecimal getTotalMeteredConsumptionKl() { return totalMeteredConsumptionKl; }
    public void setTotalMeteredConsumptionKl(BigDecimal totalMeteredConsumptionKl) { this.totalMeteredConsumptionKl = totalMeteredConsumptionKl; }

    public BigDecimal getTotalMeteredCharges() { return totalMeteredCharges; }
    public void setTotalMeteredCharges(BigDecimal totalMeteredCharges) { this.totalMeteredCharges = totalMeteredCharges; }

    public BigDecimal getTotalSharedCommonCharges() { return totalSharedCommonCharges; }
    public void setTotalSharedCommonCharges(BigDecimal totalSharedCommonCharges) { this.totalSharedCommonCharges = totalSharedCommonCharges; }

    public BigDecimal getTotalCycleBilling() { return totalCycleBilling; }
    public void setTotalCycleBilling(BigDecimal totalCycleBilling) { this.totalCycleBilling = totalCycleBilling; }

    public int getHouseholdsBilledCount() { return householdsBilledCount; }
    public void setHouseholdsBilledCount(int householdsBilledCount) { this.householdsBilledCount = householdsBilledCount; }

    public List<HouseholdBillSummary> getHouseholdBills() { return householdBills; }
    public void setHouseholdBills(List<HouseholdBillSummary> householdBills) { this.householdBills = householdBills; }
}
