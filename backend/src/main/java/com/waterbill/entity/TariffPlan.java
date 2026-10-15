package com.waterbill.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tariff_plans")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TariffPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Apartment is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @NotBlank(message = "Tariff plan name is required")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Effective from date is required")
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @JsonManagedReference
    @OneToMany(mappedBy = "tariffPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("tierOrder ASC")
    private List<TariffTier> tiers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public TariffPlan() {}

    public TariffPlan(Apartment apartment, String name, String description, LocalDate effectiveFrom, Boolean isActive) {
        this.apartment = apartment;
        this.name = name;
        this.description = description;
        this.effectiveFrom = effectiveFrom;
        this.isActive = isActive;
    }

    public void addTier(TariffTier tier) {
        tiers.add(tier);
        tier.setTariffPlan(this);
    }

    public void removeTier(TariffTier tier) {
        tiers.remove(tier);
        tier.setTariffPlan(null);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Apartment getApartment() { return apartment; }
    public void setApartment(Apartment apartment) { this.apartment = apartment; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }

    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<TariffTier> getTiers() { return tiers; }
    public void setTiers(List<TariffTier> tiers) { this.tiers = tiers; }
}
