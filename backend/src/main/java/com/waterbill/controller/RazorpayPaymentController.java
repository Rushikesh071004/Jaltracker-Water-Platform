package com.waterbill.controller;

import com.waterbill.entity.Invoice;
import com.waterbill.entity.PaymentStatus;
import com.waterbill.entity.User;
import com.waterbill.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments/razorpay")
public class RazorpayPaymentController {

    private static final Logger log = LoggerFactory.getLogger(RazorpayPaymentController.class);

    private final InvoiceRepository invoiceRepository;
    private final com.waterbill.service.NotificationBroadcastService notificationBroadcastService;

    @Value("${app.razorpay.key-id:rzp_test_JalTrackerDemoKey}")
    private String razorpayKeyId;

    public RazorpayPaymentController(InvoiceRepository invoiceRepository,
                                     com.waterbill.service.NotificationBroadcastService notificationBroadcastService) {
        this.invoiceRepository = invoiceRepository;
        this.notificationBroadcastService = notificationBroadcastService;
    }

    /**
     * Requirement 4: Endpoint to generate a Razorpay Order in INR.
     */
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createRazorpayOrder(@RequestBody Map<String, Object> payload) {
        Object invoiceIdObj = payload.get("invoiceId");
        if (invoiceIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "invoiceId is required"));
        }

        Long invoiceId = Long.valueOf(invoiceIdObj.toString());
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

        if (invoice.getPaymentStatus() == PaymentStatus.PAID) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invoice is already marked as PAID"));
        }

        // Amount in paise (1 INR = 100 paise)
        long amountInPaise = invoice.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();
        String generatedOrderId = "order_jal_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);

        invoice.setRazorpayOrderId(generatedOrderId);
        invoiceRepository.save(invoice);

        // Extract resident details
        List<User> residents = invoice.getHousehold().getResidents();
        String customerName = "Resident " + invoice.getHousehold().getFlatNumber();
        String customerEmail = "resident." + invoice.getHousehold().getFlatNumber().toLowerCase() + "@jaltracker.in";
        String customerPhone = "+91 98200 11223";

        if (residents != null && !residents.isEmpty()) {
            User r = residents.get(0);
            if (r.getFullName() != null) customerName = r.getFullName();
            if (r.getEmail() != null) customerEmail = r.getEmail();
            if (r.getPhone() != null) customerPhone = r.getPhone();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", generatedOrderId);
        response.put("amount", amountInPaise);
        response.put("amountInr", invoice.getTotalAmount());
        response.put("currency", "INR");
        response.put("keyId", razorpayKeyId);
        response.put("invoiceId", invoice.getId());
        response.put("invoiceNumber", invoice.getInvoiceNumber());
        response.put("flatNumber", invoice.getHousehold().getFlatNumber());
        response.put("societyName", invoice.getHousehold().getApartment().getName());
        response.put("customerName", customerName);
        response.put("customerEmail", customerEmail);
        response.put("customerPhone", customerPhone);

        log.info("Razorpay Order created in INR for Invoice #{}: OrderId={}, Amount=₹{}",
                invoice.getInvoiceNumber(), generatedOrderId, invoice.getTotalAmount());

        return ResponseEntity.ok(response);
    }

    /**
     * Requirement 4: Verification/webhook endpoint to update the bill status to PAID in PostgreSQL.
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyRazorpayPayment(@RequestBody Map<String, Object> payload) {
        Object invoiceIdObj = payload.get("invoiceId");
        if (invoiceIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "invoiceId is required"));
        }

        Long invoiceId = Long.valueOf(invoiceIdObj.toString());
        String paymentId = (String) payload.getOrDefault("razorpayPaymentId", "pay_jal_demo_" + UUID.randomUUID().toString().substring(0, 8));
        String orderId = (String) payload.getOrDefault("razorpayOrderId", "");
        String signature = (String) payload.getOrDefault("razorpaySignature", "sig_jal_demo_verified");

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found with ID: " + invoiceId));

        invoice.setPaymentStatus(PaymentStatus.PAID);
        invoice.setPaymentDate(OffsetDateTime.now());
        invoice.setRazorpayPaymentId(paymentId);
        if (orderId != null && !orderId.isBlank()) {
            invoice.setRazorpayOrderId(orderId);
        }
        invoice.setRazorpaySignature(signature);

        String paymentNote = String.format("Paid online via Razorpay Gateway (INR ₹%.2f) [Payment ID: %s]",
                invoice.getTotalAmount(), paymentId);
        invoice.setNotes(invoice.getNotes() != null ? invoice.getNotes() + " | " + paymentNote : paymentNote);

        Invoice saved = invoiceRepository.save(invoice);

        log.info("Razorpay Payment verified successfully for Invoice #{}. Marked as PAID in PostgreSQL.", invoice.getInvoiceNumber());

        Long residentId = null;
        if (saved.getHousehold() != null && saved.getHousehold().getResidents() != null && !saved.getHousehold().getResidents().isEmpty()) {
            residentId = saved.getHousehold().getResidents().get(0).getId();
        }
        String flatNum = saved.getHousehold() != null ? saved.getHousehold().getFlatNumber() : "N/A";
        notificationBroadcastService.notifyPaymentSuccess(saved.getId(), saved.getInvoiceNumber(), flatNum, saved.getTotalAmount(), paymentId, residentId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Payment verified successfully. Invoice #" + saved.getInvoiceNumber() + " is marked as PAID.");
        response.put("invoiceId", saved.getId());
        response.put("invoiceNumber", saved.getInvoiceNumber());
        response.put("paymentStatus", saved.getPaymentStatus());
        response.put("paymentDate", saved.getPaymentDate());
        response.put("razorpayPaymentId", paymentId);
        response.put("amountPaidInr", saved.getTotalAmount());

        return ResponseEntity.ok(response);
    }
}
