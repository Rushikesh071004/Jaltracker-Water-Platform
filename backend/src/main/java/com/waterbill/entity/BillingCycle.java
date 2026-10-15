package com.waterbill.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "billing_cycles", uniqueConstraints = {
    @UniqueConstraint(name = "uq_apartment_cycle_name", columnNames = {"apartment_id", "cycle_name"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BillingCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Apartment is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @NotBlank(message = "Cycle name is required")
    @Column(name = "cycle_name", nullable = false, length = 100)
    private String cycleName;

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CycleStatus status = CycleStatus.OPEN;

    @Column(name = "total_bulk_water_cost", precision = 12, scale = 2)
    private BigDecimal totalBulkWaterCost = BigDecimal.ZERO;

    @Column(name = "total_common_area_volume_kl", precision = 12, scale = 3)
    private BigDecimal totalCommonAreaVolumeKl = BigDecimal.ZERO;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "finalized_at")
    private OffsetDateTime finalizedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "billingCycle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BulkWaterPurchase> bulkWaterPurchases = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "billingCycle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invoice> invoices = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public BillingCycle() {}

    public BillingCycle(Apartment apartment, String cycleName, LocalDate startDate, LocalDate endDate, LocalDate dueDate) {
        this.apartment = apartment;
        this.cycleName = cycleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.dueDate = dueDate;
        this.status = CycleStatus.OPEN;
        this.totalBulkWaterCost = BigDecimal.ZERO;
        this.totalCommonAreaVolumeKl = BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Apartment getApartment() { return apartment; }
    public void setApartment(Apartment apartment) { this.apartment = apartment; }

    public String getCycleName() { return cycleName; }
    public void setCycleName(String cycleName) { this.cycleName = cycleName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public CycleStatus getStatus() { return status; }
    public void setStatus(CycleStatus status) { this.status = status; }

    public BigDecimal getTotalBulkWaterCost() { return totalBulkWaterCost; }
    public void setTotalBulkWaterCost(BigDecimal totalBulkWaterCost) { this.totalBulkWaterCost = totalBulkWaterCost; }

    public BigDecimal getTotalCommonAreaVolumeKl() { return totalCommonAreaVolumeKl; }
    public void setTotalCommonAreaVolumeKl(BigDecimal totalCommonAreaVolumeKl) { this.totalCommonAreaVolumeKl = totalCommonAreaVolumeKl; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getFinalizedAt() { return finalizedAt; }
    public void setFinalizedAt(OffsetDateTime finalizedAt) { this.finalizedAt = finalizedAt; }

    public List<BulkWaterPurchase> getBulkWaterPurchases() { return bulkWaterPurchases; }
    public void setBulkWaterPurchases(List<BulkWaterPurchase> bulkWaterPurchases) { this.bulkWaterPurchases = bulkWaterPurchases; }

    public List<Invoice> getInvoices() { return invoices; }
    public void setInvoices(List<Invoice> invoices) { this.invoices = invoices; }
}
