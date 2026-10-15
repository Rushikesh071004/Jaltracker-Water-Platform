package com.waterbill.dto;

import com.waterbill.entity.PurchaseType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BulkWaterPurchaseRequest {

    @NotNull(message = "Billing cycle ID is required")
    private Long billingCycleId;

    @NotNull(message = "Apartment ID is required")
    private Long apartmentId;

    @NotNull(message = "Purchase date is required")
    private LocalDate purchaseDate;

    @NotBlank(message = "Supplier name is required")
    private String supplierName;

    @NotNull(message = "Purchase type is required")
    private PurchaseType purchaseType = PurchaseType.TANKER;

    @NotNull(message = "Volume in kL is required")
    @DecimalMin(value = "0.001", message = "Volume must be greater than zero")
    private BigDecimal volumeKl;

    @NotNull(message = "Unit cost per kL is required")
    @DecimalMin(value = "0.01", message = "Unit cost must be greater than zero")
    private BigDecimal unitCostPerKl;

    private String invoiceReference;
    private String notes;

    public BulkWaterPurchaseRequest() {}

    public Long getBillingCycleId() { return billingCycleId; }
    public void setBillingCycleId(Long billingCycleId) { this.billingCycleId = billingCycleId; }

    public Long getApartmentId() { return apartmentId; }
    public void setApartmentId(Long apartmentId) { this.apartmentId = apartmentId; }

    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public PurchaseType getPurchaseType() { return purchaseType; }
    public void setPurchaseType(PurchaseType purchaseType) { this.purchaseType = purchaseType; }

    public BigDecimal getVolumeKl() { return volumeKl; }
    public void setVolumeKl(BigDecimal volumeKl) { this.volumeKl = volumeKl; }

    public BigDecimal getUnitCostPerKl() { return unitCostPerKl; }
    public void setUnitCostPerKl(BigDecimal unitCostPerKl) { this.unitCostPerKl = unitCostPerKl; }

    public String getInvoiceReference() { return invoiceReference; }
    public void setInvoiceReference(String invoiceReference) { this.invoiceReference = invoiceReference; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
