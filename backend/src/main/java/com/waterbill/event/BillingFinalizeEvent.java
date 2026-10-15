package com.waterbill.event;

import com.waterbill.dto.HouseholdBillSummary;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

public class BillingFinalizeEvent implements Serializable {

    private Long cycleId;
    private Long apartmentId;
    private String cycleName;
    private List<HouseholdBillSummary> householdBills;
    private OffsetDateTime timestamp;

    public BillingFinalizeEvent() {}

    public BillingFinalizeEvent(Long cycleId, Long apartmentId, String cycleName, List<HouseholdBillSummary> householdBills) {
        this.cycleId = cycleId;
        this.apartmentId = apartmentId;
        this.cycleName = cycleName;
        this.householdBills = householdBills;
        this.timestamp = OffsetDateTime.now();
    }

    public Long getCycleId() { return cycleId; }
    public void setCycleId(Long cycleId) { this.cycleId = cycleId; }

    public Long getApartmentId() { return apartmentId; }
    public void setApartmentId(Long apartmentId) { this.apartmentId = apartmentId; }

    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }

    public List<HouseholdBillSummary> getHouseholdBills() { return householdBills; }
    public void setHouseholdBills(List<HouseholdBillSummary> householdBills) { this.householdBills = householdBills; }

    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
}
