package com.smartmedical.dao;

import com.smartmedical.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReportDAO — all 10 MIS reports from BRD Section 10
 */
public class ReportDAO {

    /** Daily Sales Report — supports single date or date range */
    public List<Map<String, Object>> getDailySalesReport(Date from, Date to) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT s.sale_id, s.bill_no, s.sale_date, c.shop_name, " +
                     "s.subtotal, s.discount, s.gst, s.net_total, s.payment_status " +
                     "FROM SALES s JOIN CUSTOMERS c ON c.customer_id = s.customer_id " +
                     "WHERE TRUNC(s.sale_date) BETWEEN TRUNC(?) AND TRUNC(?) ORDER BY s.sale_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(Map.of(
                    "saleId",      rs.getInt("sale_id"),
                    "billNo",      rs.getString("bill_no"),
                    "shopName",    rs.getString("shop_name"),
                    "subtotal",    rs.getBigDecimal("subtotal"),
                    "discount",    rs.getBigDecimal("discount"),
                    "gst",         rs.getBigDecimal("gst"),
                    "netTotal",    rs.getBigDecimal("net_total"),
                    "status",      rs.getString("payment_status")
                ));
            }
        }
        return list;
    }

    /** Monthly Sales Report */
    public Map<String, Object> getMonthlySalesReport(int month, int year) throws SQLException {
        String sql = "SELECT COUNT(*) AS bill_count, " +
                     "SUM(net_total) AS total_sales, " +
                     "AVG(net_total) AS avg_bill, " +
                     "SUM(gst) AS total_gst, " +
                     "SUM(discount) AS total_discount " +
                     "FROM SALES WHERE EXTRACT(MONTH FROM sale_date)=? " +
                     "AND EXTRACT(YEAR FROM sale_date)=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Map.of(
                    "billCount",     rs.getInt("bill_count"),
                    "totalSales",    rs.getBigDecimal("total_sales"),
                    "avgBill",       rs.getBigDecimal("avg_bill"),
                    "totalGst",      rs.getBigDecimal("total_gst"),
                    "totalDiscount", rs.getBigDecimal("total_discount")
                );
            }
        }
        return Map.of();
    }

    /** Profit Report — per medicine/category for date range */
    public List<Map<String, Object>> getProfitReport(Date from, Date to) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT m.name AS medicine_name, m.category, " +
                     "SUM(si.qty_sold) AS total_qty, " +
                     "SUM((si.mrp - si.purchase_rate) * si.qty_sold - si.discount) AS gross_profit, " +
                     "SUM(si.amount) AS total_revenue " +
                     "FROM SALE_ITEMS si " +
                     "JOIN MEDICINES m ON m.medicine_id = si.medicine_id " +
                     "JOIN SALES s ON s.sale_id = si.sale_id " +
                     "WHERE s.sale_date BETWEEN ? AND ? " +
                     "GROUP BY m.medicine_id, m.name, m.category " +
                     "ORDER BY gross_profit DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(Map.of(
                    "medicineName", rs.getString("medicine_name"),
                    "category",     rs.getString("category"),
                    "totalQty",     rs.getInt("total_qty"),
                    "grossProfit",  rs.getBigDecimal("gross_profit"),
                    "totalRevenue", rs.getBigDecimal("total_revenue")
                ));
            }
        }
        return list;
    }

    /** GST Summary — CGST + SGST per slab (for GSTR-1) */
    public List<Map<String, Object>> getGstSummary(int month, int year) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT si.gst_pct AS slab, " +
                     "SUM(si.mrp * si.qty_sold - si.discount) AS taxable_amount, " +
                     "SUM((si.mrp * si.qty_sold - si.discount) * si.gst_pct / 100) AS total_gst, " +
                     "SUM((si.mrp * si.qty_sold - si.discount) * si.gst_pct / 200) AS cgst, " +
                     "SUM((si.mrp * si.qty_sold - si.discount) * si.gst_pct / 200) AS sgst " +
                     "FROM SALE_ITEMS si JOIN SALES s ON s.sale_id = si.sale_id " +
                     "WHERE EXTRACT(MONTH FROM s.sale_date)=? AND EXTRACT(YEAR FROM s.sale_date)=? " +
                     "GROUP BY si.gst_pct ORDER BY si.gst_pct";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, month);
            ps.setInt(2, year);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(Map.of(
                    "slab",          rs.getBigDecimal("slab"),
                    "taxableAmount", rs.getBigDecimal("taxable_amount"),
                    "totalGst",      rs.getBigDecimal("total_gst"),
                    "cgst",          rs.getBigDecimal("cgst"),
                    "sgst",          rs.getBigDecimal("sgst")
                ));
            }
        }
        return list;
    }

    /** Salesman Report — sales + commission for date range */
    public List<Map<String, Object>> getSalesmanReport(Date from, Date to) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT sm.salesman_id, sm.name, sm.commission_pct, " +
                     "COUNT(s.sale_id) AS bill_count, " +
                     "SUM(s.net_total) AS total_sales, " +
                     "SUM(s.net_total) * sm.commission_pct / 100 AS commission " +
                     "FROM SALESMEN sm " +
                     "LEFT JOIN SALES s ON s.salesman_id = sm.salesman_id " +
                     "AND s.sale_date BETWEEN ? AND ? " +
                     "WHERE sm.is_active='Y' " +
                     "GROUP BY sm.salesman_id, sm.name, sm.commission_pct " +
                     "ORDER BY total_sales DESC NULLS LAST";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("salesmanId", rs.getInt("salesman_id"));
                row.put("name", rs.getString("name"));
                row.put("commissionPct", rs.getBigDecimal("commission_pct"));
                row.put("billCount", rs.getInt("bill_count"));
                row.put("totalSales", rs.getBigDecimal("total_sales"));
                row.put("commission", rs.getBigDecimal("commission"));
                list.add(row);
            }
        }
        return list;
    }

    /** Dashboard counts — expiry alerts, low stock, today's sales */
    public Map<String, Object> getDashboardCounts() throws SQLException {
        String sql = """
            SELECT
              (SELECT COUNT(*) FROM MEDICINE_BATCHES WHERE expiry_date <= SYSDATE+30 AND stock_qty>0) AS expiry_count,
              (SELECT COUNT(*) FROM MEDICINE_BATCHES WHERE stock_qty <= min_stock_alert AND is_active='Y') AS low_stock_count,
              (SELECT COUNT(*) FROM MEDICINE_BATCHES WHERE stock_qty=0 AND is_active='Y') AS out_of_stock_count,
              (SELECT NVL(SUM(net_total),0) FROM SALES WHERE TRUNC(sale_date)=TRUNC(SYSDATE)) AS today_sales,
              (SELECT COUNT(*) FROM SALES WHERE TRUNC(sale_date)=TRUNC(SYSDATE)) AS today_bills,
              (SELECT NVL(SUM(outstanding_balance),0) FROM CUSTOMERS WHERE is_active='Y') AS total_customer_outstanding
            FROM DUAL
            """;
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return Map.of(
                    "expiryCount",            rs.getInt("expiry_count"),
                    "lowStockCount",          rs.getInt("low_stock_count"),
                    "outOfStockCount",        rs.getInt("out_of_stock_count"),
                    "todaySales",             rs.getBigDecimal("today_sales"),
                    "todayBills",             rs.getInt("today_bills"),
                    "totalCustomerOutstanding", rs.getBigDecimal("total_customer_outstanding")
                );
            }
        }
        return Map.of();
    }
}