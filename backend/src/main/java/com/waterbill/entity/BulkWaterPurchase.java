package com.waterbill.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bulk_water_purchases")
public class BulkWaterPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Billing cycle is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_cycle_id", nullable = false)
    private BillingCycle billingCycle;

    @NotNull(message = "Apartment is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @NotNull(message = "Purchase date is required")
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @NotBlank(message = "Supplier name is required")
    @Column(name = "supplier_name", nullable = false, length = 150)
    private String supplierName;

    @NotNull(message = "Purchase type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "purchase_type", nullable = false, length = 50)
    private PurchaseType purchaseType = PurchaseType.TANKER;

    @NotNull(message = "Volume is required")
    @DecimalMin(value = "0.001", message = "Volume must be greater than zero")
    @Column(name = "volume_kl", nullable = false, precision = 10, scale = 3)
    private BigDecimal volumeKl;

    @NotNull(message = "Unit cost is required")
    @DecimalMin(value = "0.01", message = "Unit cost must be greater than zero")
    @Column(name = "unit_cost_per_kl", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCostPerKl;

    @Column(name = "total_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "invoice_reference", length = 100)
    private String invoiceReference;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    @PreUpdate
    protected void calculateTotal() {
        if (this.volumeKl != null && this.unitCostPerKl != null) {
            this.totalCost = this.volumeKl.multiply(this.unitCostPerKl);
        }
        if (this.createdAt == null) {
            this.createdAt = OffsetDateTime.now();
        }
    }

    public BulkWaterPurchase() {}

    public BulkWaterPurchase(BillingCycle billingCycle, Apartment apartment, LocalDate purchaseDate,
                             String supplierName, PurchaseType purchaseType, BigDecimal volumeKl,
                             BigDecimal unitCostPerKl, String invoiceReference, String notes) {
        this.billingCycle = billingCycle;
        this.apartment = apartment;
        this.purchaseDate = purchaseDate;
        this.supplierName = supplierName;
        this.purchaseType = purchaseType;
        this.volumeKl = volumeKl;
        this.unitCostPerKl = unitCostPerKl;
        this.totalCost = volumeKl != null && unitCostPerKl != null ? volumeKl.multiply(unitCostPerKl) : BigDecimal.ZERO;
        this.invoiceReference = invoiceReference;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }

    public Apartment getApartment() { return apartment; }
    public void setApartment(Apartment apartment) { this.apartment = apartment; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public PurchaseType getPurchaseType() { return purchaseType; }
    public void setPurchaseType(PurchaseType purchaseType) { this.purchaseType = purchaseType; }

    public BigDecimal getVolumeKl() { return volumeKl; }
    public void setVolumeKl(BigDecimal volumeKl) {
        this.volumeKl = volumeKl;
        if (this.unitCostPerKl != null) {
            this.totalCost = volumeKl.multiply(this.unitCostPerKl);
        }
    }

    public BigDecimal getUnitCostPerKl() { return unitCostPerKl; }
    public void setUnitCostPerKl(BigDecimal unitCostPerKl) {
        this.unitCostPerKl = unitCostPerKl;
        if (this.volumeKl != null) {
            this.totalCost = this.volumeKl.multiply(unitCostPerKl);
        }
    }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public String getInvoiceReference() { return invoiceReference; }
    public void setInvoiceReference(String invoiceReference) { this.invoiceReference = invoiceReference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
