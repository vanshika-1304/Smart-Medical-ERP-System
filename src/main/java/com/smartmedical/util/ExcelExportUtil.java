package com.smartmedical.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * ExcelExportUtil — Apache POI XLSX export
 * BRD Section 10: Daily Sales, Monthly Sales, Profit, GST Summary,
 *                  Customer Outstanding, Salesman Report
 */
public class ExcelExportUtil {

    private ExcelExportUtil() {}

    // ── Daily Sales Report ────────────────────────────────────────────────────

    public static byte[] dailySalesReport(List<Map<String, Object>> sales, String date) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Daily Sales");

        CellStyle titleStyle  = createTitleStyle(wb);
        CellStyle headerStyle = createHeaderStyle(wb);
        CellStyle numStyle    = createNumberStyle(wb);
        CellStyle altStyle    = createAltRowStyle(wb);

        int row = 0;

        // Title
        Row titleRow = sheet.createRow(row++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Daily Sales Report — " + date);
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
        row++;

        // Headers
        String[] headers = {"#", "Bill No", "Customer", "Subtotal (₹)", "Discount (₹)", "GST (₹)", "Net Total (₹)"};
        Row hRow = sheet.createRow(row++);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }

        // Data
        BigDecimal grandTotal = BigDecimal.ZERO;
        int sno = 1;
        for (Map<String, Object> s : sales) {
            Row r = sheet.createRow(row++);
            CellStyle rowStyle = sno % 2 == 0 ? altStyle : null;
            CellStyle rowNumStyle = sno % 2 == 0 ? createAltNumStyle(wb) : numStyle;
            Cell c0 = r.createCell(0); c0.setCellValue(sno++); if (rowStyle != null) c0.setCellStyle(rowStyle);
            Cell c1 = r.createCell(1); c1.setCellValue(str(s.get("billNo"))); if (rowStyle != null) c1.setCellStyle(rowStyle);
            Cell c2 = r.createCell(2); c2.setCellValue(str(s.get("shopName"))); if (rowStyle != null) c2.setCellStyle(rowStyle);
            setNum(r.createCell(3), s.get("subtotal"), rowNumStyle);
            setNum(r.createCell(4), s.get("discount"), rowNumStyle);
            setNum(r.createCell(5), s.get("gst"), rowNumStyle);
            setNum(r.createCell(6), s.get("netTotal"), rowNumStyle);
            grandTotal = grandTotal.add(bd(s.get("netTotal")));
        }

        // Grand total
        Row gtRow = sheet.createRow(row++);
        CellStyle boldStyle = createBoldStyle(wb);
        for (int i = 0; i < 7; i++) { Cell c = gtRow.createCell(i); c.setCellStyle(boldStyle); }
        gtRow.getCell(5).setCellValue("GRAND TOTAL:");
        Cell gtCell = gtRow.getCell(6);
        gtCell.setCellValue(grandTotal.doubleValue());

        autoSize(sheet, 7);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return out.toByteArray();
    }

    // ── GST Summary Report ────────────────────────────────────────────────────

    public static byte[] gstSummaryReport(List<Map<String, Object>> gstData, int month, int year) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("GST Summary");

        CellStyle titleStyle  = createTitleStyle(wb);
        CellStyle headerStyle = createHeaderStyle(wb);
        CellStyle numStyle    = createNumberStyle(wb);

        int row = 0;
        Row titleRow = sheet.createRow(row++);
        Cell t = titleRow.createCell(0);
        t.setCellValue("GST Summary — " + month + "/" + year + " (GSTR-1 Format)");
        t.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
        row++;

        String[] headers = {"GST Slab (%)", "Taxable Amount (₹)", "CGST (₹)", "SGST (₹)", "Total GST (₹)"};
        Row hRow = sheet.createRow(row++);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }

        BigDecimal totalGst = BigDecimal.ZERO;
        for (Map<String, Object> g : gstData) {
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(str(g.get("slab")) + "%");
            setNum(r.createCell(1), g.get("taxableAmount"), numStyle);
            setNum(r.createCell(2), g.get("cgst"), numStyle);
            setNum(r.createCell(3), g.get("sgst"), numStyle);
            setNum(r.createCell(4), g.get("totalGst"), numStyle);
            totalGst = totalGst.add(bd(g.get("totalGst")));
        }

        // Total row
        Row tot = sheet.createRow(row);
        CellStyle bold = createBoldStyle(wb);
        tot.createCell(3).setCellValue("TOTAL GST:");
        tot.getCell(3).setCellStyle(bold);
        Cell tc = tot.createCell(4);
        tc.setCellValue(totalGst.doubleValue());
        tc.setCellStyle(bold);

        autoSize(sheet, 5);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return out.toByteArray();
    }

    // ── Customer Outstanding Aging ────────────────────────────────────────────

    public static byte[] outstandingAgingReport(List<Map<String, Object>> aging) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Outstanding Aging");

        CellStyle titleStyle  = createTitleStyle(wb);
        CellStyle headerStyle = createHeaderStyle(wb);
        CellStyle numStyle    = createNumberStyle(wb);

        int row = 0;
        Row titleRow = sheet.createRow(row++);
        Cell t = titleRow.createCell(0);
        t.setCellValue("Customer Outstanding Aging Report");
        t.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
        row++;

        String[] hdrs = {"Customer", "Phone", "0-30 Days", "31-60 Days", "61-90 Days", "90+ Days", "Total Outstanding"};
        Row hRow = sheet.createRow(row++);
        for (int i = 0; i < hdrs.length; i++) {
            Cell c = hRow.createCell(i); c.setCellValue(hdrs[i]); c.setCellStyle(headerStyle);
        }

        for (Map<String, Object> a : aging) {
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(str(a.get("shopName")));
            r.createCell(1).setCellValue(str(a.get("phone")));
            setNum(r.createCell(2), a.get("bucket0_30"), numStyle);
            setNum(r.createCell(3), a.get("bucket31_60"), numStyle);
            setNum(r.createCell(4), a.get("bucket61_90"), numStyle);
            setNum(r.createCell(5), a.get("bucket90plus"), numStyle);
            setNum(r.createCell(6), a.get("totalOutstanding"), numStyle);
        }

        autoSize(sheet, 7);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return out.toByteArray();
    }

    // ── Salesman Report ───────────────────────────────────────────────────────

    public static byte[] salesmanReport(List<Map<String, Object>> data, String from, String to) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Salesman Report");

        CellStyle titleStyle  = createTitleStyle(wb);
        CellStyle headerStyle = createHeaderStyle(wb);
        CellStyle numStyle    = createNumberStyle(wb);

        int row = 0;
        Row titleRow = sheet.createRow(row++);
        Cell t = titleRow.createCell(0);
        t.setCellValue("Salesman Performance Report (" + from + " to " + to + ")");
        t.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        row++;

        String[] hdrs = {"Salesman", "Bills", "Total Sales (₹)", "Commission %", "Commission (₹)"};
        Row hRow = sheet.createRow(row++);
        for (int i = 0; i < hdrs.length; i++) {
            Cell c = hRow.createCell(i); c.setCellValue(hdrs[i]); c.setCellStyle(headerStyle);
        }

        for (Map<String, Object> d : data) {
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(str(d.get("name")));
            r.createCell(1).setCellValue(asInt(d.get("billCount")));
            setNum(r.createCell(2), d.get("totalSales"), numStyle);
            setNum(r.createCell(3), d.get("commissionPct"), numStyle);
            setNum(r.createCell(4), d.get("commission"), numStyle);
        }

        autoSize(sheet, 5);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return out.toByteArray();
    }

    // ── Profit Report ─────────────────────────────────────────────────────────

    public static byte[] profitReport(List<Map<String, Object>> data, String from, String to) throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Profit Report");

        CellStyle titleStyle  = createTitleStyle(wb);
        CellStyle headerStyle = createHeaderStyle(wb);
        CellStyle numStyle    = createNumberStyle(wb);

        int row = 0;
        Row titleRow = sheet.createRow(row++);
        Cell t = titleRow.createCell(0);
        t.setCellValue("Profit Report (" + from + " to " + to + ")");
        t.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
        row++;

        String[] hdrs = {"Medicine", "Category", "Qty Sold", "Revenue (₹)", "Gross Profit (₹)"};
        Row hRow = sheet.createRow(row++);
        for (int i = 0; i < hdrs.length; i++) {
            Cell c = hRow.createCell(i); c.setCellValue(hdrs[i]); c.setCellStyle(headerStyle);
        }

        BigDecimal totalProfit = BigDecimal.ZERO;
        for (Map<String, Object> d : data) {
            Row r = sheet.createRow(row++);
            r.createCell(0).setCellValue(str(d.get("medicineName")));
            r.createCell(1).setCellValue(str(d.get("category")));
            r.createCell(2).setCellValue(asInt(d.get("totalQty")));
            setNum(r.createCell(3), d.get("totalRevenue"), numStyle);
            setNum(r.createCell(4), d.get("grossProfit"), numStyle);
            totalProfit = totalProfit.add(bd(d.get("grossProfit")));
        }

        Row totRow = sheet.createRow(row);
        CellStyle bold = createBoldStyle(wb);
        totRow.createCell(3).setCellValue("TOTAL PROFIT:");
        totRow.getCell(3).setCellStyle(bold);
        Cell tc = totRow.createCell(4);
        tc.setCellValue(totalProfit.doubleValue());
        tc.setCellStyle(bold);

        autoSize(sheet, 5);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return out.toByteArray();
    }

    // ── Style Helpers ─────────────────────────────────────────────────────────

    private static CellStyle createTitleStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true); font.setFontHeightInPoints((short) 14);
        font.setColor(new XSSFColor(new byte[]{(byte)26, (byte)115, (byte)232}, null));
        s.setFont(font);
        return s;
    }

    private static CellStyle createHeaderStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(new byte[]{(byte)26, (byte)115, (byte)232}, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = wb.createFont();
        font.setBold(true); font.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(font);
        s.setBorderBottom(BorderStyle.THIN);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private static CellStyle createNumberStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        DataFormat df = wb.createDataFormat();
        s.setDataFormat(df.getFormat("#,##0.00"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        return s;
    }

    private static CellStyle createAltRowStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setFillForegroundColor(new XSSFColor(new byte[]{(byte)245, (byte)247, (byte)250}, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private static CellStyle createBoldStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        XSSFFont font = wb.createFont();
        font.setBold(true);
        s.setFont(font);
        s.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat df = wb.createDataFormat();
        s.setDataFormat(df.getFormat("#,##0.00"));
        return s;
    }

    private static CellStyle createAltNumStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        DataFormat df = wb.createDataFormat();
        s.setDataFormat(df.getFormat("#,##0.00"));
        s.setAlignment(HorizontalAlignment.RIGHT);
        s.setFillForegroundColor(new XSSFColor(new byte[]{(byte)245, (byte)247, (byte)250}, null));
        s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return s;
    }

    private static void autoSize(Sheet sheet, int cols) {
        for (int i = 0; i < cols; i++) sheet.autoSizeColumn(i);
    }

    private static void setNum(Cell c, Object val, CellStyle style) {
        c.setCellValue(bd(val).doubleValue());
        c.setCellStyle(style);
    }

    private static BigDecimal bd(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal b) return b;
        return new BigDecimal(val.toString());
    }

    private static String str(Object val) {
        return val != null ? val.toString() : "";
    }

    private static int asInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}