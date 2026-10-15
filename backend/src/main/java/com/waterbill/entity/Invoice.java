package com.waterbill.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "invoices")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_cycle_id")
    private BillingCycle billingCycle;

    @NotNull(message = "Household is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @NotBlank(message = "Invoice number is required")
    @Column(name = "invoice_number", nullable = false, unique = true, length = 100)
    private String invoiceNumber;

    @Column(name = "metered_consumption_kl", nullable = false, precision = 10, scale = 3)
    private BigDecimal meteredConsumptionKl = BigDecimal.ZERO;

    @Column(name = "metered_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal meteredCharge = BigDecimal.ZERO;

    @Column(name = "shared_common_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal sharedCommonCharge = BigDecimal.ZERO;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", message = "Total amount cannot be negative")
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 50)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_date")
    private OffsetDateTime paymentDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature", length = 255)
    private String razorpaySignature;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    public Invoice() {}

    public Invoice(BillingCycle billingCycle, Household household, String invoiceNumber,
                   BigDecimal meteredConsumptionKl, BigDecimal meteredCharge,
                   BigDecimal sharedCommonCharge, BigDecimal totalAmount) {
        this.billingCycle = billingCycle;
        this.household = household;
        this.invoiceNumber = invoiceNumber;
        this.meteredConsumptionKl = meteredConsumptionKl;
        this.meteredCharge = meteredCharge;
        this.sharedCommonCharge = sharedCommonCharge;
        this.totalAmount = totalAmount;
        this.paymentStatus = PaymentStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }

    public Household getHousehold() { return household; }
    public void setHousehold(Household household) { this.household = household; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public BigDecimal getMeteredConsumptionKl() { return meteredConsumptionKl; }
    public void setMeteredConsumptionKl(BigDecimal meteredConsumptionKl) { this.meteredConsumptionKl = meteredConsumptionKl; }

    public BigDecimal getMeteredCharge() { return meteredCharge; }
    public void setMeteredCharge(BigDecimal meteredCharge) { this.meteredCharge = meteredCharge; }

    public BigDecimal getSharedCommonCharge() { return sharedCommonCharge; }
    public void setSharedCommonCharge(BigDecimal sharedCommonCharge) { this.sharedCommonCharge = sharedCommonCharge; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public OffsetDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(OffsetDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getRazorpaySignature() { return razorpaySignature; }
    public void setRazorpaySignature(String razorpaySignature) { this.razorpaySignature = razorpaySignature; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
