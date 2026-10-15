package com.waterbill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class HouseholdRequest {

    @NotNull(message = "Apartment ID is required")
    private Long apartmentId;

    @NotBlank(message = "Flat number is required")
    private String flatNumber;

    private String blockOrWing;
    private Integer floorNumber;

    @NotNull(message = "Area (sqft) is required")
    @DecimalMin(value = "1.0", message = "Area must be at least 1 sqft")
    private BigDecimal areaSqft;

    @NotNull(message = "Number of occupants is required")
    @Positive(message = "Number of occupants must be at least 1")
    private Integer numberOfOccupants = 1;

    @NotNull(message = "Metered flag is required")
    private Boolean isMetered = true;

    private BigDecimal dailyUsageThresholdKl = BigDecimal.valueOf(1.500);

    public HouseholdRequest() {}

    public Long getApartmentId() { return apartmentId; }
    public void setApartmentId(Long apartmentId) { this.apartmentId = apartmentId; }

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
    public void setIsMetered(Boolean metered) { isMetered = metered; }

    public BigDecimal getDailyUsageThresholdKl() { return dailyUsageThresholdKl; }
    public void setDailyUsageThresholdKl(BigDecimal dailyUsageThresholdKl) { this.dailyUsageThresholdKl = dailyUsageThresholdKl; }
}
