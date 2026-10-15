package com.waterbill.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TariffPlanRequest {

    @NotNull(message = "Apartment ID is required")
    private Long apartmentId;

    @NotBlank(message = "Tariff plan name is required")
    private String name;

    private String description;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private Boolean isActive = true;

    @NotEmpty(message = "At least one tariff tier is required")
    @Valid
    private List<TariffTierRequest> tiers = new ArrayList<>();

    public TariffPlanRequest() {}

    public Long getApartmentId() { return apartmentId; }
    public void setApartmentId(Long apartmentId) { this.apartmentId = apartmentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }

    public List<TariffTierRequest> getTiers() { return tiers; }
    public void setTiers(List<TariffTierRequest> tiers) { this.tiers = tiers; }
}
