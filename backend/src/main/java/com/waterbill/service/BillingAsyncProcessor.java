package com.waterbill.service;

import com.waterbill.config.RabbitMQConfig;
import com.waterbill.entity.Invoice;
import com.waterbill.event.BillingFinalizeEvent;
import com.waterbill.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BillingAsyncProcessor {

    private static final Logger log = LoggerFactory.getLogger(BillingAsyncProcessor.class);

    private final InvoiceRepository invoiceRepository;
    private final PdfInvoiceService pdfInvoiceService;
    private final EmailNotificationService emailNotificationService;
    private final NotificationBroadcastService notificationBroadcastService;

    public BillingAsyncProcessor(InvoiceRepository invoiceRepository,
                                 PdfInvoiceService pdfInvoiceService,
                                 EmailNotificationService emailNotificationService,
                                 NotificationBroadcastService notificationBroadcastService) {
        this.invoiceRepository = invoiceRepository;
        this.pdfInvoiceService = pdfInvoiceService;
        this.emailNotificationService = emailNotificationService;
        this.notificationBroadcastService = notificationBroadcastService;
    }

    /**
     * RabbitMQ Listener: Asynchronously processes finalized billing invoices from queue
     */
    @RabbitListener(queues = RabbitMQConfig.BILLING_QUEUE)
    public void handleBillingQueueMessage(BillingFinalizeEvent event) {
        log.info("[RABBITMQ WORKER] Received BillingFinalizeEvent for Cycle ID: {}, Name: {}",
                event.getCycleId(), event.getCycleName());
        processEventInvoices(event);
    }

    /**
     * Fallback Async Executor: Used when RabbitMQ is offline or in local direct execution
     */
    @Async
    public void processEventInvoicesAsync(BillingFinalizeEvent event) {
        log.info("[ASYNC WORKER] Processing BillingFinalizeEvent asynchronously for Cycle ID: {}", event.getCycleId());
        processEventInvoices(event);
    }

    private void processEventInvoices(BillingFinalizeEvent event) {
        if (event.getCycleId() == null) return;

        List<Invoice> cycleInvoices = invoiceRepository.findByBillingCycleId(event.getCycleId());
        log.info("[WORKER] Generating PDF invoices and dispatching notifications for {} households...", cycleInvoices.size());

        for (Invoice invoice : cycleInvoices) {
            try {
                // Generate PDF invoice asynchronously
                byte[] pdfBytes = pdfInvoiceService.generateInvoicePdf(invoice);
                log.debug("[WORKER] PDF invoice generated for {} ({} bytes)", invoice.getInvoiceNumber(), pdfBytes.length);

                // Send email notification
                emailNotificationService.sendMonthlyBillNotification(invoice);
            } catch (Exception ex) {
                log.error("[WORKER ERROR] Failed to process invoice {} in background: {}", invoice.getInvoiceNumber(), ex.getMessage());
            }
        }

        java.math.BigDecimal totalAmount = cycleInvoices.stream()
                .map(Invoice::getTotalAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        notificationBroadcastService.notifyBillingCycleFinalized(event.getCycleId(), cycleInvoices.size(), totalAmount);
        log.info("[WORKER] Background invoice processing finished for Cycle ID: {}", event.getCycleId());
    }
}
