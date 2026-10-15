package com.waterbill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BillingCycleRequest {

    @NotNull(message = "Apartment ID is required")
    private Long apartmentId;

    @NotBlank(message = "Cycle name is required")
    private String cycleName;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private BigDecimal totalCommonAreaVolumeKl = BigDecimal.ZERO;

    public BillingCycleRequest() {}

    public Long getApartmentId() { return apartmentId; }
    public void setApartmentId(Long apartmentId) { this.apartmentId = apartmentId; }

    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public BigDecimal getTotalCommonAreaVolumeKl() { return totalCommonAreaVolumeKl; }
    public void setTotalCommonAreaVolumeKl(BigDecimal totalCommonAreaVolumeKl) { this.totalCommonAreaVolumeKl = totalCommonAreaVolumeKl; }
}
