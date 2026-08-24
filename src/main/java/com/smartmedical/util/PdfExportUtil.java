package com.smartmedical.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * PdfExportUtil — iText 7 based PDF generation
 * FR-16: GST-compliant invoice PDF
 * BRD Section 10: All report PDFs
 */
public class PdfExportUtil {

    private static final Logger logger = Logger.getLogger(PdfExportUtil.class.getName());
    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(26, 115, 232);  // #1a73e8
    private static final DeviceRgb LIGHT_GRAY   = new DeviceRgb(245, 247, 250);

    private PdfExportUtil() {}

    /**
     * Generate GST-compliant invoice PDF for a sale
     * FR-15, FR-16
     */
    public static byte[] generateInvoicePdf(Map<String, Object> sale, List<Map<String, Object>> items,
                                             Map<String, Object> company) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf  = new PdfDocument(writer);
        Document doc     = new Document(pdf, PageSize.A4);
        doc.setMargins(30, 40, 30, 40);

        // ── Header ──────────────────────────────────────────────────────────
        Table header = new Table(UnitValue.createPercentArray(new float[]{60, 40}))
                .setWidth(UnitValue.createPercentValue(100));

        // Company info
        Cell companyCell = new Cell().setBorder(Border.NO_BORDER);
        companyCell.add(new Paragraph((String) company.getOrDefault("name", "Smart Medical Agency"))
                .setBold().setFontSize(16).setFontColor(HEADER_COLOR));
        companyCell.add(new Paragraph("GSTIN: " + company.getOrDefault("gstin", ""))
                .setFontSize(9).setFontColor(ColorConstants.GRAY));
        companyCell.add(new Paragraph((String) company.getOrDefault("address", ""))
                .setFontSize(9));
        companyCell.add(new Paragraph("Phone: " + company.getOrDefault("phone", ""))
                .setFontSize(9));
        header.addCell(companyCell);

        // Invoice info
        Cell invoiceCell = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        invoiceCell.add(new Paragraph("TAX INVOICE").setBold().setFontSize(14)
                .setFontColor(HEADER_COLOR));
        invoiceCell.add(new Paragraph("Bill No: " + sale.get("billNo")).setFontSize(10).setBold());
        invoiceCell.add(new Paragraph("Date: " + sale.get("saleDate")).setFontSize(9));
        invoiceCell.add(new Paragraph("Status: " + sale.get("paymentStatus")).setFontSize(9));
        header.addCell(invoiceCell);
        doc.add(header);

        // Divider
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine())
                .setMarginTop(5).setMarginBottom(10));

        // ── Bill To ─────────────────────────────────────────────────────────
        Table billTo = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginBottom(10);

        Cell toCell = new Cell().setBorder(Border.NO_BORDER)
                .setBackgroundColor(LIGHT_GRAY).setPadding(8);
        toCell.add(new Paragraph("Bill To:").setBold().setFontSize(9).setFontColor(HEADER_COLOR));
        toCell.add(new Paragraph((String) sale.getOrDefault("shopName", "")).setBold().setFontSize(11));
        toCell.add(new Paragraph("Owner: " + sale.getOrDefault("ownerName", "")).setFontSize(9));
        toCell.add(new Paragraph("GSTIN: " + sale.getOrDefault("customerGstin", "")).setFontSize(9));
        toCell.add(new Paragraph("Salesman: " + sale.getOrDefault("salesmanName", "N/A")).setFontSize(9));
        billTo.addCell(toCell);
        billTo.addCell(new Cell().setBorder(Border.NO_BORDER));
        doc.add(billTo);

        // ── Items Table ──────────────────────────────────────────────────────
        Table itemTable = new Table(UnitValue.createPercentArray(new float[]{5, 25, 12, 8, 8, 8, 8, 8, 8}))
                .setWidth(UnitValue.createPercentValue(100));

        // Table header
        String[] headers = {"#", "Medicine", "Batch/Expiry", "HSN", "Qty", "MRP", "Disc%", "GST%", "Amount"};
        for (String h : headers) {
            itemTable.addHeaderCell(new Cell()
                    .setBackgroundColor(HEADER_COLOR)
                    .add(new Paragraph(h).setFontColor(ColorConstants.WHITE).setFontSize(8).setBold())
                    .setPadding(5));
        }

        // Table rows
        int sno = 1;
        for (Map<String, Object> item : items) {
            boolean odd = sno % 2 != 0;
            DeviceRgb rowBg = odd ? new DeviceRgb(255, 255, 255) : LIGHT_GRAY;

            itemTable.addCell(styledCell(String.valueOf(sno++), rowBg));
            itemTable.addCell(styledCell((String) item.getOrDefault("medicineName", ""), rowBg));
            String batchInfo = item.getOrDefault("batchNo", "") + "\nExp: " + item.getOrDefault("expiryDate", "");
            itemTable.addCell(styledCell(batchInfo, rowBg));
            itemTable.addCell(styledCell((String) item.getOrDefault("hsnCode", ""), rowBg));
            itemTable.addCell(styledCell(String.valueOf(item.getOrDefault("qtySold", 0)), rowBg));
            itemTable.addCell(styledCell(formatAmt(item.get("mrp")), rowBg));
            itemTable.addCell(styledCell(formatAmt(item.get("discount")), rowBg));
            itemTable.addCell(styledCell(formatAmt(item.get("gstPct")) + "%", rowBg));
            itemTable.addCell(styledCell(formatAmt(item.get("amount")), rowBg));
        }
        doc.add(itemTable);

        // ── Totals ───────────────────────────────────────────────────────────
        Table totals = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(100)).setMarginTop(8);

        // CGST/SGST breakdown (BRD 4.2)
        BigDecimal gst      = getBD(sale.get("gst"));
        BigDecimal cgst     = gst.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        BigDecimal sgst     = cgst;
        BigDecimal subtotal = getBD(sale.get("subtotal"));
        BigDecimal discount = getBD(sale.get("discount"));
        BigDecimal netTotal = getBD(sale.get("netTotal"));

        Cell blank = new Cell().setBorder(Border.NO_BORDER)
                .add(new Paragraph("Amount in Words:\n" + amountInWords(netTotal))
                        .setFontSize(8).setItalic());
        totals.addCell(blank);

        Cell amtCell = new Cell().setBorder(new SolidBorder(HEADER_COLOR, 1)).setPadding(8);
        amtCell.add(totalRow("Subtotal:",     "₹" + subtotal));
        amtCell.add(totalRow("Discount:",     "- ₹" + discount));
        amtCell.add(totalRow("CGST:",         "₹" + cgst));
        amtCell.add(totalRow("SGST:",         "₹" + sgst));
        amtCell.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.DashedLine()));
        amtCell.add(totalRow("NET TOTAL:",    "₹" + netTotal, true));
        totals.addCell(amtCell);
        doc.add(totals);

        // ── Footer ───────────────────────────────────────────────────────────
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine()).setMarginTop(15));
        doc.add(new Paragraph("This is a computer generated invoice. No signature required.")
                .setFontSize(8).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));

        doc.close();
        return baos.toByteArray();
    }

    /**
     * Generate Daily Sales Report PDF
     */
    public static byte[] generateDailySalesReport(List<Map<String, Object>> sales, String date)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf  = new PdfDocument(writer);
        Document doc     = new Document(pdf, PageSize.A4.rotate()); // Landscape
        doc.setMargins(30, 40, 30, 40);

        // Title
        doc.add(new Paragraph("Daily Sales Report — " + date)
                .setBold().setFontSize(16).setFontColor(HEADER_COLOR).setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph("Smart Medical ERP System")
                .setFontSize(10).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine()).setMarginBottom(10));

        // Table
        Table table = new Table(UnitValue.createPercentArray(new float[]{8, 18, 22, 12, 12, 12, 16}))
                .setWidth(UnitValue.createPercentValue(100));

        String[] hdrs = {"#", "Bill No", "Customer", "Subtotal", "Discount", "GST", "Net Total"};
        for (String h : hdrs) {
            table.addHeaderCell(new Cell()
                    .setBackgroundColor(HEADER_COLOR)
                    .add(new Paragraph(h).setFontColor(ColorConstants.WHITE).setFontSize(9).setBold())
                    .setPadding(5));
        }

        BigDecimal grandTotal = BigDecimal.ZERO;
        int i = 1;
        for (Map<String, Object> s : sales) {
            DeviceRgb bg = i % 2 != 0 ? new DeviceRgb(255,255,255) : LIGHT_GRAY;
            table.addCell(styledCell(String.valueOf(i++), bg));
            table.addCell(styledCell(asString(s.get("billNo")), bg));
            table.addCell(styledCell(asString(s.get("shopName")), bg));
            table.addCell(styledCell("₹" + formatAmt(s.get("subtotal")), bg));
            table.addCell(styledCell("₹" + formatAmt(s.get("discount")), bg));
            table.addCell(styledCell("₹" + formatAmt(s.get("gst")), bg));
            table.addCell(styledCell("₹" + formatAmt(s.get("netTotal")), bg));
            grandTotal = grandTotal.add(getBD(s.get("netTotal")));
        }

        // Grand total row
        for (int j = 0; j < 6; j++) {
            table.addCell(new Cell().setBackgroundColor(new DeviceRgb(26,115,232))
                    .add(new Paragraph(j == 5 ? "GRAND TOTAL:" : "")
                            .setFontColor(ColorConstants.WHITE).setBold().setFontSize(9)));
        }
        table.addCell(new Cell().setBackgroundColor(HEADER_COLOR)
                .add(new Paragraph("₹" + grandTotal)
                        .setFontColor(ColorConstants.WHITE).setBold().setFontSize(10)));

        doc.add(table);
        doc.add(new Paragraph("Total Bills: " + sales.size() + "    Grand Total: ₹" + grandTotal)
                .setFontSize(10).setBold().setMarginTop(10));
        doc.close();
        return baos.toByteArray();
    }

    /**
     * Generate Outstanding Aging Report PDF
     */
    public static byte[] generateOutstandingReport(List<Map<String, Object>> aging) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf  = new PdfDocument(writer);
        Document doc     = new Document(pdf, PageSize.A4.rotate());
        doc.setMargins(30, 40, 30, 40);

        doc.add(new Paragraph("Customer Outstanding Aging Report")
                .setBold().setFontSize(16).setFontColor(HEADER_COLOR).setTextAlignment(TextAlignment.CENTER));
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine()).setMarginBottom(10));

        Table table = new Table(UnitValue.createPercentArray(new float[]{22, 15, 14, 14, 14, 14, 15}))
                .setWidth(UnitValue.createPercentValue(100));

        String[] hdrs = {"Customer", "Phone", "0-30 Days", "31-60 Days", "61-90 Days", "90+ Days", "Total"};
        for (String h : hdrs) {
            table.addHeaderCell(new Cell().setBackgroundColor(HEADER_COLOR)
                    .add(new Paragraph(h).setFontColor(ColorConstants.WHITE).setFontSize(9).setBold()).setPadding(5));
        }

        int i = 1;
        for (Map<String, Object> row : aging) {
            DeviceRgb bg = i++ % 2 != 0 ? new DeviceRgb(255,255,255) : LIGHT_GRAY;
            table.addCell(styledCell(asString(row.get("shopName")), bg));
            table.addCell(styledCell(asString(row.get("phone")), bg));
            table.addCell(styledCell("₹" + formatAmt(row.get("bucket0_30")), bg));
            table.addCell(styledCell("₹" + formatAmt(row.get("bucket31_60")), bg));
            table.addCell(styledCell("₹" + formatAmt(row.get("bucket61_90")), bg));
            table.addCell(styledCell("₹" + formatAmt(row.get("bucket90plus")), bg));
            table.addCell(styledCell("₹" + formatAmt(row.get("totalOutstanding")), bg));
        }
        doc.add(table);
        doc.close();
        return baos.toByteArray();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static Cell styledCell(String text, DeviceRgb bg) {
        return new Cell().setBackgroundColor(bg)
                .add(new Paragraph(text != null ? text : "").setFontSize(8))
                .setPadding(4);
    }

    private static Paragraph totalRow(String label, String value) {
        return totalRow(label, value, false);
    }

    private static Paragraph totalRow(String label, String value, boolean bold) {
        Paragraph p = new Paragraph(label + "  " + value).setFontSize(9);
        if (bold) p.setBold().setFontSize(11).setFontColor(HEADER_COLOR);
        return p;
    }

    private static String formatAmt(Object val) {
        if (val == null) return "0.00";
        if (val instanceof BigDecimal bd) return String.format("%.2f", bd);
        if (val instanceof Number n) return String.format("%.2f", n.doubleValue());
        return val.toString();
    }

    private static String asString(Object val) {
        return val == null ? "" : val.toString();
    }

    private static BigDecimal getBD(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal bd) return bd;
        return new BigDecimal(val.toString());
    }

    private static String amountInWords(BigDecimal amount) {
        // Simplified — real implementation would use full rupees-in-words library
    	return "Rupees " + amount.setScale(0, RoundingMode.FLOOR).longValue() + " and " +
               amount.remainder(BigDecimal.ONE).multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue() +
               " paise only";
    }
}