package com.waterbill.service;

import com.waterbill.entity.Alert;
import com.waterbill.entity.Apartment;
import com.waterbill.entity.Household;
import com.waterbill.entity.Invoice;
import com.waterbill.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;
    private final boolean simulationMode;
    private final String senderEmail;

    public EmailNotificationService(
            JavaMailSender mailSender,
            @Value("${app.mail.simulation-mode:true}") boolean simulationMode,
            @Value("${spring.mail.username:billing@watermanagement.org}") String senderEmail) {
        this.mailSender = mailSender;
        this.simulationMode = simulationMode;
        this.senderEmail = (senderEmail == null || senderEmail.isBlank()) ? "billing@watermanagement.org" : senderEmail;
    }

    /**
     * Sends monthly bill notification with charges and due date to resident.
     */
    public void sendMonthlyBillNotification(Invoice invoice) {
        Household household = invoice.getHousehold();
        List<User> residents = household.getResidents();
        String toEmail = (residents != null && !residents.isEmpty() && residents.get(0).getEmail() != null)
                ? residents.get(0).getEmail()
                : "resident." + household.getFlatNumber() + "@apartment.local";

        String subject = String.format("Water Bill for %s - %s",
                household.getFlatNumber(), invoice.getBillingCycle().getCycleName());

        String content = String.format("""
                Dear Resident of Flat %s,
                
                Your water consumption bill for billing cycle %s has been generated.
                
                Summary:
                - Metered Consumption: %.3f kL
                - Metered Usage Charge: ₹%.2f
                - Shared Common Area Charge: ₹%.2f
                - Total Payable: ₹%.2f
                - Payment Due Date: %s
                
                Please log into the resident portal to view or download your detailed PDF invoice.
                
                Water Saving Tip of the Month:
                Replacing standard aerators with low-flow aerators can save up to 40%% of sink water!
                
                Sincerely,
                Water Management Board - %s
                """,
                household.getFlatNumber(),
                invoice.getBillingCycle().getCycleName(),
                invoice.getMeteredConsumptionKl(),
                invoice.getMeteredCharge(),
                invoice.getSharedCommonCharge(),
                invoice.getTotalAmount(),
                invoice.getBillingCycle().getDueDate(),
                household.getApartment().getName()
        );

        dispatchEmail(toEmail, subject, content);
    }

    /**
     * Sends overuse alert with conservation and reduction tips.
     */
    public void sendOveruseAlert(Household household, Alert alert) {
        List<User> residents = household.getResidents();
        String toEmail = (residents != null && !residents.isEmpty() && residents.get(0).getEmail() != null)
                ? residents.get(0).getEmail()
                : "resident." + household.getFlatNumber() + "@apartment.local";

        String subject = "Overuse Alert: " + alert.getTitle();
        String content = String.format("""
                Attention: Resident of Flat %s,
                
                %s
                
                Your daily threshold of %.3f kL was exceeded (Logged: %.3f kL on %s).
                
                Quick Water Reduction Tips:
                1. Inspect toilet cisterns for slow leaks by dropping food coloring into the tank.
                2. Turn off running water while brushing or lathering soap.
                3. Run washing machines and dishwashers only with full loads.
                4. Report any dripping taps or pipe joints to maintenance.
                
                Sincerely,
                Water Management Board
                """,
                household.getFlatNumber(),
                alert.getMessage(),
                alert.getThresholdKl(),
                alert.getConsumptionKl(),
                alert.getReadingDate()
        );

        dispatchEmail(toEmail, subject, content);
    }

    /**
     * Sends critical leak alert with emergency advice and notifies apartment admins.
     */
    public void sendLeakAlert(Household household, Alert alert) {
        List<User> residents = household.getResidents();
        String residentEmail = (residents != null && !residents.isEmpty() && residents.get(0).getEmail() != null)
                ? residents.get(0).getEmail()
                : "resident." + household.getFlatNumber() + "@apartment.local";

        String subject = "CRITICAL: " + alert.getTitle();
        String content = String.format("""
                URGENT: Potential Water Leak Detected in Flat %s
                
                %s
                
                Statistical Analysis:
                - Recorded Usage: %.3f kL
                - Deviation: %.2f standard deviations (σ) above household baseline average.
                
                Immediate Recommended Actions:
                1. Check if an appliance (washing machine, RO filter, garden hose) was accidentally left open.
                2. Listen for continuous water hiss in toilet tanks.
                3. If no appliances were active, locate your household main water valve and turn it off to prevent flooding.
                4. Contact emergency apartment maintenance immediately.
                
                Apartment Administration has been automatically notified.
                """,
                household.getFlatNumber(),
                alert.getMessage(),
                alert.getConsumptionKl(),
                alert.getSigmaValue()
        );

        dispatchEmail(residentEmail, subject, content);

        // Also notify admin
        String adminEmail = household.getApartment().getContactEmail();
        if (adminEmail != null && !adminEmail.isBlank()) {
            dispatchEmail(adminEmail, "[Admin Alert] " + subject,
                    "Anomalous leak pattern detected in Flat " + household.getFlatNumber() + ".\n" + content);
        }
    }

    /**
     * Sends anomaly summary report to apartment administrators.
     */
    public void sendAdminAnomalyReport(Apartment apartment, List<Alert> alerts) {
        if (apartment.getContactEmail() == null || apartment.getContactEmail().isBlank()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Weekly Water Anomaly & Leak Summary for ").append(apartment.getName()).append("\n\n");
        sb.append("Total active alerts: ").append(alerts.size()).append("\n\n");

        for (Alert a : alerts) {
            sb.append(String.format("- [%s] Flat %s (%s): %s (Reading: %.3f kL)\n",
                    a.getSeverity(),
                    a.getHousehold().getFlatNumber(),
                    a.getReadingDate(),
                    a.getTitle(),
                    a.getConsumptionKl()));
        }

        dispatchEmail(apartment.getContactEmail(), "Water Management: Weekly Anomaly Report", sb.toString());
    }

    /**
     * Sends One-Time Password (OTP) for passwordless authentication.
     */
    public void sendOtpEmail(String toEmail, String otpCode) {
        String subject = "Your JALTRACKER Login Code: " + otpCode;
        String content = String.format("""
                Hello,

                Your one-time login verification code for JALTRACKER is:

                ======================
                       %s
                ======================

                This code will expire in 10 minutes. Please do not share this code with anyone.

                If you did not request this login code, please ignore this email.

                Sincerely,
                JALTRACKER Security & Authentication
                """, otpCode);

        dispatchEmail(toEmail, subject, content);
    }

    private void dispatchEmail(String to, String subject, String body) {
        if (simulationMode) {
            log.info("\n========== [SIMULATED EMAIL NOTIFICATION] ==========\nTO: {}\nFROM: {}\nSUBJECT: {}\nBODY:\n{}\n===================================================",
                    to, senderEmail, subject, body);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (Exception ex) {
            log.warn("Failed to dispatch real email to {} (falling back to log): {}", to, ex.getMessage());
            log.info("\n========== [FALLBACK EMAIL LOG] ==========\nTO: {}\nSUBJECT: {}\nBODY:\n{}\n==========================================",
                    to, subject, body);
        }
    }
}
