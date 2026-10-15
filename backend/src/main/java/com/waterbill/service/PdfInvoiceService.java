package com.waterbill.service;

import com.waterbill.entity.Invoice;
import com.waterbill.entity.TariffPlan;
import com.waterbill.entity.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class PdfInvoiceService {

    private final TariffBillingService tariffBillingService;

    public PdfInvoiceService(TariffBillingService tariffBillingService) {
        this.tariffBillingService = tariffBillingService;
    }

    /**
     * Generates a professionally formatted PDF invoice for a household billing record.
     */
    public byte[] generateInvoicePdf(Invoice invoice) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontOblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float margin = 50;
                float y = 790;
                float pageWidth = page.getMediaBox().getWidth();

                // Top Header Banner
                cs.setNonStrokingColor(26 / 255f, 115 / 255f, 232 / 255f); // Primary Blue
                cs.addRect(margin, y - 45, pageWidth - (2 * margin), 50);
                cs.fill();

                // Title Text
                cs.beginText();
                cs.setNonStrokingColor(1f, 1f, 1f); // White
                cs.setFont(fontBold, 18);
                cs.newLineAtOffset(margin + 15, y - 25);
                cs.showText("JALTRACKER - WATER UTILITY & BILLING INVOICE");
                cs.endText();

                cs.beginText();
                cs.setFont(fontRegular, 10);
                cs.newLineAtOffset(margin + 15, y - 40);
                cs.showText(invoice.getHousehold().getApartment().getName() + " | Smart Society Water Management");
                cs.endText();

                y -= 70;

                // Invoice Metadata Box
                cs.setNonStrokingColor(0.2f, 0.2f, 0.2f); // Dark Charcoal text

                // Left Column: Resident Info
                cs.beginText();
                cs.setFont(fontBold, 12);
                cs.newLineAtOffset(margin, y);
                cs.showText("BILL TO:");
                cs.endText();

                List<User> residents = invoice.getHousehold().getResidents();
                String residentName = (residents != null && !residents.isEmpty()) ? residents.get(0).getFullName() : "Valued Resident";
                String residentEmail = (residents != null && !residents.isEmpty()) ? residents.get(0).getEmail() : "N/A";

                y -= 16;
                writeText(cs, fontRegular, 10, margin, y, "Resident: " + residentName);
                y -= 14;
                writeText(cs, fontRegular, 10, margin, y, "Flat: " + invoice.getHousehold().getFlatNumber() + " (" + invoice.getHousehold().getBlockOrWing() + ")");
                y -= 14;
                writeText(cs, fontRegular, 10, margin, y, "Area: " + invoice.getHousehold().getAreaSqft() + " sqft  |  Occupants: " + invoice.getHousehold().getNumberOfOccupants());
                y -= 14;
                writeText(cs, fontRegular, 10, margin, y, "Email: " + residentEmail);

                // Right Column: Invoice Details (Aligned around x = 340)
                float rightCol = 340;
                float metaY = y + 58;
                writeText(cs, fontBold, 12, rightCol, metaY, "INVOICE DETAILS");
                metaY -= 16;
                writeText(cs, fontRegular, 10, rightCol, metaY, "Invoice #: " + invoice.getInvoiceNumber());
                metaY -= 14;
                writeText(cs, fontRegular, 10, rightCol, metaY, "Cycle: " + invoice.getBillingCycle().getCycleName());
                metaY -= 14;
                writeText(cs, fontRegular, 10, rightCol, metaY, "Period: " + invoice.getBillingCycle().getStartDate() + " to " + invoice.getBillingCycle().getEndDate());
                metaY -= 14;
                writeText(cs, fontRegular, 10, rightCol, metaY, "Due Date: " + invoice.getBillingCycle().getDueDate());
                metaY -= 14;
                writeText(cs, fontBold, 10, rightCol, metaY, "Status: " + invoice.getPaymentStatus());

                y -= 30;

                // Horizontal Separator Line
                cs.setStrokingColor(0.8f, 0.8f, 0.8f);
                cs.setLineWidth(1f);
                cs.moveTo(margin, y);
                cs.lineTo(pageWidth - margin, y);
                cs.stroke();

                y -= 25;

                // Metered Consumption & Tariff Section
                writeText(cs, fontBold, 13, margin, y, "1. Metered Water Consumption Charges");
                y -= 18;

                boolean isMetered = Boolean.TRUE.equals(invoice.getHousehold().getIsMetered());
                if (isMetered) {
                    writeText(cs, fontRegular, 10, margin, y,
                            String.format("Total Metered Usage for this Period: %.3f kL (1 kL = 1,000 Liters)",
                                    invoice.getMeteredConsumptionKl()));
                    y -= 20;

                    // Table Header
                    drawTableHeader(cs, fontBold, margin, y);
                    y -= 20;

                    // Fetch Tier Breakdown
                    try {
                        TariffPlan plan = tariffBillingService.getActiveTariffForApartment(invoice.getHousehold().getApartment().getId());
                        TariffBillingService.TierCalculationResult tierResult =
                                tariffBillingService.calculateTieredCharge(invoice.getMeteredConsumptionKl(), plan.getTiers());

                        for (var tier : tierResult.getBreakdowns()) {
                            String range = String.format("%.1f - %s kL",
                                    tier.getMinVolumeKl(),
                                    tier.getMaxVolumeKl() != null ? tier.getMaxVolumeKl().toString() : "Infinity");

                            drawTableRow(cs, fontRegular, margin, y,
                                    tier.getTierName(),
                                    range,
                                    String.format("%.3f kL", tier.getVolumeInTierKl()),
                                    String.format("Rs. %.2f", tier.getRatePerKl()),
                                    String.format("Rs. %.2f", tier.getSubtotal()));
                            y -= 18;
                        }
                    } catch (Exception e) {
                        drawTableRow(cs, fontRegular, margin, y, "Metered Consumption", "Standard",
                                String.format("%.3f kL", invoice.getMeteredConsumptionKl()), "-",
                                String.format("Rs. %.2f", invoice.getMeteredCharge()));
                        y -= 18;
                    }

                    // Metered Subtotal
                    y -= 6;
                    writeText(cs, fontBold, 10, 380, y, "Metered Subtotal: Rs. " + invoice.getMeteredCharge());
                } else {
                    writeText(cs, fontOblique, 10, margin, y, "Flat rate unit (Unmetered). Metered consumption charge is Rs. 0.00.");
                }

                y -= 30;

                // Shared Common Area Water Costs Section
                writeText(cs, fontBold, 13, margin, y, "2. Shared Apartment Water Cost Allocation");
                y -= 16;
                String allocationRationale = isMetered
                        ? "Proportionally apportioned based on household's share of total metered consumption."
                        : "Flat area proportional allocation based on unit square footage (unmetered fallback).";
                writeText(cs, fontRegular, 9, margin, y, allocationRationale);
                y -= 16;
                writeText(cs, fontRegular, 10, margin, y, "Shared Common Area & Tanker Bulk Water Share: Rs. " + invoice.getSharedCommonCharge());

                y -= 35;

                // Total Summary Box
                cs.setNonStrokingColor(240 / 255f, 244 / 255f, 250 / 255f); // Light gray/blue box
                cs.addRect(margin, y - 40, pageWidth - (2 * margin), 45);
                cs.fill();

                cs.setNonStrokingColor(0f, 0f, 0f);
                writeText(cs, fontBold, 14, margin + 15, y - 25, "TOTAL AMOUNT PAYABLE:");
                writeText(cs, fontBold, 16, 400, y - 25, "Rs. " + invoice.getTotalAmount());

                y -= 65;

                // Payment Instructions & Bank Details
                writeText(cs, fontBold, 11, margin, y, "PAYMENT INSTRUCTIONS & REMITTANCE (JALTRACKER)");
                y -= 14;
                writeText(cs, fontRegular, 9, margin, y, "1. Online Instant Payment: Pay via Razorpay (UPI / GPay / PhonePe / Card) in Resident Portal");
                y -= 12;
                writeText(cs, fontRegular, 9, margin, y, "2. Bank Transfer / NEFT: A/C 987654321001, IFSC: SBIN0001234, Bank: State Bank of India");
                y -= 12;
                writeText(cs, fontRegular, 9, margin, y, "3. Reference: " + invoice.getInvoiceNumber() + " | Settle before " + invoice.getBillingCycle().getDueDate() + " to prevent late charges.");

                y -= 25;

                // Footer with Eco Water Conservation Note
                cs.setNonStrokingColor(46 / 255f, 125 / 255f, 50 / 255f); // Eco Green
                writeText(cs, fontBold, 9, margin, y, "WATER CONSERVATION COMMITMENT");
                y -= 12;
                cs.setNonStrokingColor(0.3f, 0.3f, 0.3f);
                writeText(cs, fontOblique, 8, margin, y,
                        "Thank you for being a responsible water consumer. Reporting leaks promptly saves thousands of liters monthly!");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private void writeText(PDPageContentStream cs, PDType1Font font, float fontSize, float x, float y, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text != null ? text : "");
        cs.endText();
    }

    private void drawTableHeader(PDPageContentStream cs, PDType1Font font, float startX, float y) throws IOException {
        cs.setNonStrokingColor(230 / 255f, 235 / 255f, 245 / 255f);
        cs.addRect(startX, y - 5, 495, 18);
        cs.fill();

        cs.setNonStrokingColor(0f, 0f, 0f);
        writeText(cs, font, 9, startX + 5, y, "Tier Description");
        writeText(cs, font, 9, startX + 160, y, "Tier Range");
        writeText(cs, font, 9, startX + 260, y, "Volume");
        writeText(cs, font, 9, startX + 340, y, "Rate / kL");
        writeText(cs, font, 9, startX + 420, y, "Subtotal");
    }

    private void drawTableRow(PDPageContentStream cs, PDType1Font font, float startX, float y,
                              String col1, String col2, String col3, String col4, String col5) throws IOException {
        cs.setNonStrokingColor(0.2f, 0.2f, 0.2f);
        writeText(cs, font, 9, startX + 5, y, col1);
        writeText(cs, font, 9, startX + 160, y, col2);
        writeText(cs, font, 9, startX + 260, y, col3);
        writeText(cs, font, 9, startX + 340, y, col4);
        writeText(cs, font, 9, startX + 420, y, col5);
    }
}
