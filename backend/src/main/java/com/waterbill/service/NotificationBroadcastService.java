package com.waterbill.service;

import com.waterbill.dto.LiveNotificationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class NotificationBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(NotificationBroadcastService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationBroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Broadcast a notification to all subscribers of /topic/notifications.
     */
    public void broadcast(LiveNotificationDto notification) {
        try {
            log.info("Broadcasting live STOMP notification: type={}, title='{}'", notification.getType(), notification.getTitle());
            messagingTemplate.convertAndSend("/topic/notifications", notification);
        } catch (Exception ex) {
            log.error("Failed to broadcast WebSocket notification: {}", ex.getMessage());
        }
    }

    /**
     * Send a notification to a specific user topic (/topic/users/{userId}).
     */
    public void sendToUser(Long userId, LiveNotificationDto notification) {
        try {
            String destination = "/topic/users/" + userId;
            log.info("Sending live STOMP notification to user destination {}: type={}, title='{}'", destination, notification.getType(), notification.getTitle());
            messagingTemplate.convertAndSend(destination, notification);
        } catch (Exception ex) {
            log.error("Failed to send user-specific WebSocket notification: {}", ex.getMessage());
        }
    }

    /**
     * Live push notification when a resident's registration is approved or rejected by an admin.
     */
    public void notifyResidentVerification(Long userId, String username, String fullName, String flatNumber, boolean approved) {
        String type = approved ? "USER_APPROVAL" : "USER_REJECTION";
        String title = approved ? "Resident Approved" : "Resident Registration Rejected";
        String message = approved
                ? String.format("Resident %s (%s) from Flat %s has been APPROVED by Administrator.", fullName, username, flatNumber)
                : String.format("Resident %s (%s) registration was REJECTED by Administrator.", fullName, username);

        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("username", username);
        payload.put("fullName", fullName);
        payload.put("flatNumber", flatNumber);
        payload.put("status", approved ? "VERIFIED" : "REJECTED");

        LiveNotificationDto notification = new LiveNotificationDto(
                UUID.randomUUID().toString(),
                type,
                title,
                message,
                userId,
                username,
                payload
        );

        // Broadcast to admins
        broadcast(notification);

        // Also push directly to user's private topic if connected
        sendToUser(userId, notification);
    }

    /**
     * Live push notification when a payment is successfully processed.
     */
    public void notifyPaymentSuccess(Long invoiceId, String invoiceNumber, String flatNumber, BigDecimal amount, String paymentId, Long residentUserId) {
        String title = String.format("Payment Received: ₹%.2f", amount);
        String message = String.format("Invoice #%s for Flat %s was successfully paid online via Razorpay (Txn: %s).",
                invoiceNumber, flatNumber, paymentId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("invoiceId", invoiceId);
        payload.put("invoiceNumber", invoiceNumber);
        payload.put("flatNumber", flatNumber);
        payload.put("amount", amount);
        payload.put("paymentId", paymentId);
        payload.put("status", "PAID");

        LiveNotificationDto notification = new LiveNotificationDto(
                UUID.randomUUID().toString(),
                "PAYMENT_SUCCESS",
                title,
                message,
                residentUserId,
                null,
                payload
        );

        // Broadcast to all administrators
        broadcast(notification);

        // Broadcast to the resident if user ID is known
        if (residentUserId != null) {
            sendToUser(residentUserId, notification);
        }
    }

    /**
     * Live push notification when billing cycle is finalized.
     */
    public void notifyBillingCycleFinalized(Long cycleId, int billCount, BigDecimal totalBilled) {
        String title = "Billing Cycle Finalized";
        String message = String.format("Monthly billing cycle #%d finalized. %d invoices generated (Total: ₹%.2f).",
                cycleId, billCount, totalBilled);

        Map<String, Object> payload = new HashMap<>();
        payload.put("cycleId", cycleId);
        payload.put("billCount", billCount);
        payload.put("totalBilled", totalBilled);

        LiveNotificationDto notification = new LiveNotificationDto(
                UUID.randomUUID().toString(),
                "BILL_FINALIZED",
                title,
                message,
                null,
                null,
                payload
        );

        broadcast(notification);
    }
}
