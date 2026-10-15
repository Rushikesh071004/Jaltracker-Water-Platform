package com.waterbill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class ManualInvoiceRequest {

    private Long householdId;
    private Long residentUserId;
    private Long billingCycleId;

    private BigDecimal meteredConsumptionKl = BigDecimal.ZERO;

    private BigDecimal meteredCharge = BigDecimal.ZERO;

    private BigDecimal sharedCommonCharge = BigDecimal.ZERO;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than zero")
    private BigDecimal totalAmount;

    private String notes;
    private LocalDate dueDate;

    public ManualInvoiceRequest() {}

    public Long getHouseholdId() { return householdId; }
    public void setHouseholdId(Long householdId) { this.householdId = householdId; }

    public Long getResidentUserId() { return residentUserId; }
    public void setResidentUserId(Long residentUserId) { this.residentUserId = residentUserId; }

    public Long getBillingCycleId() { return billingCycleId; }
    public void setBillingCycleId(Long billingCycleId) { this.billingCycleId = billingCycleId; }

    public BigDecimal getMeteredConsumptionKl() { return meteredConsumptionKl; }
    public void setMeteredConsumptionKl(BigDecimal meteredConsumptionKl) { this.meteredConsumptionKl = meteredConsumptionKl; }

    public BigDecimal getMeteredCharge() { return meteredCharge; }
    public void setMeteredCharge(BigDecimal meteredCharge) { this.meteredCharge = meteredCharge; }

    public BigDecimal getSharedCommonCharge() { return sharedCommonCharge; }
    public void setSharedCommonCharge(BigDecimal sharedCommonCharge) { this.sharedCommonCharge = sharedCommonCharge; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
