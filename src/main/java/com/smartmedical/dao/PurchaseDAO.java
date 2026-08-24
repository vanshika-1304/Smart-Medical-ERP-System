package com.smartmedical.dao;

import com.smartmedical.model.MedicineBatch;
import com.smartmedical.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * PurchaseDAO — ACID transaction:
 *  1. Insert PURCHASES header
 *  2. Insert PURCHASE_ITEMS
 *  3. Create or update MEDICINE_BATCHES (auto batch creation on purchase)
 *  4. Increase supplier outstanding
 */
public class PurchaseDAO {

    private final MedicineDAO medicineDAO = new MedicineDAO();

    public int createPurchase(int supplierId, String invoiceNo,
                              List<Map<String, Object>> items, int createdBy) throws SQLException {

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            BigDecimal totalAmount = BigDecimal.ZERO;

            // ── 1. Insert PURCHASES header ───────────────────────────────────
            int purchaseId;
            String sqlPurchase = "INSERT INTO PURCHASES (supplier_id, invoice_no, purchase_date, " +
                                 "total_amount, payment_status, created_by) " +
                                 "VALUES (?,?,SYSDATE,0,'PENDING',?)";
            try (PreparedStatement ps = con.prepareStatement(sqlPurchase,
                    new String[]{"PURCHASE_ID"})) {
                ps.setInt(1, supplierId);
                ps.setString(2, invoiceNo);
                ps.setInt(3, createdBy);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("Failed to get purchase_id");
                purchaseId = keys.getInt(1);
            }

            // ── 2. Process each item ─────────────────────────────────────────
            String sqlItem = "INSERT INTO PURCHASE_ITEMS (purchase_id, medicine_id, batch_id, " +
                             "qty, rate, mrp, gst_pct, amount) VALUES (?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sqlItem)) {
                for (Map<String, Object> item : items) {
                    int medicineId      = (int) item.get("medicineId");
                    String batchNo      = (String) item.get("batchNo");
                    Date expiryDate     = (Date) item.get("expiryDate");
                    int qty             = (int) item.get("qty");
                    BigDecimal rate     = (BigDecimal) item.get("rate");
                    BigDecimal mrp      = (BigDecimal) item.get("mrp");
                    BigDecimal gstPct   = (BigDecimal) item.getOrDefault("gstPct", BigDecimal.ZERO);
                    int minStock        = (int) item.getOrDefault("minStock", 10);

                    BigDecimal amount = rate.multiply(BigDecimal.valueOf(qty));
                    totalAmount = totalAmount.add(amount);

                    // Auto batch creation (FR-36)
                    int batchId = getOrCreateBatch(con, medicineId, batchNo, expiryDate,
                                                   mrp, rate, minStock, createdBy);

                    // Increase stock (FR-37)
                    medicineDAO.increaseStock(batchId, qty, con);

                    ps.setInt(1, purchaseId);
                    ps.setInt(2, medicineId);
                    ps.setInt(3, batchId);
                    ps.setInt(4, qty);
                    ps.setBigDecimal(5, rate);
                    ps.setBigDecimal(6, mrp);
                    ps.setBigDecimal(7, gstPct);
                    ps.setBigDecimal(8, amount);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // ── 3. Update total_amount in header ─────────────────────────────
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE PURCHASES SET total_amount = ? WHERE purchase_id = ?")) {
                ps.setBigDecimal(1, totalAmount);
                ps.setInt(2, purchaseId);
                ps.executeUpdate();
            }

            // ── 4. Increase supplier outstanding (FR-38) ─────────────────────
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE SUPPLIERS SET outstanding_balance = outstanding_balance + ? " +
                    "WHERE supplier_id = ?")) {
                ps.setBigDecimal(1, totalAmount);
                ps.setInt(2, supplierId);
                ps.executeUpdate();
            }

            con.commit();
            return purchaseId;

        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            DBConnection.close(con);
        }
    }

    /** If batch exists for this medicine+batchNo, return its id; else create it */
    private int getOrCreateBatch(Connection con, int medicineId, String batchNo,
                                  Date expiryDate, BigDecimal mrp, BigDecimal rate,
                                  int minStock, int createdBy) throws SQLException {

        try (PreparedStatement ps = con.prepareStatement(
                "SELECT batch_id FROM MEDICINE_BATCHES WHERE medicine_id=? AND batch_no=?")) {
            ps.setInt(1, medicineId);
            ps.setString(2, batchNo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("batch_id");
        }

        // Create new batch
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO MEDICINE_BATCHES (medicine_id, batch_no, expiry_date, mrp, " +
                "purchase_rate, stock_qty, min_stock_alert, created_by) VALUES (?,?,?,?,?,0,?,?)",
                new String[]{"BATCH_ID"})) {
            ps.setInt(1, medicineId);
            ps.setString(2, batchNo);
            ps.setDate(3, expiryDate);
            ps.setBigDecimal(4, mrp);
            ps.setBigDecimal(5, rate);
            ps.setInt(6, minStock);
            ps.setInt(7, createdBy);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        throw new SQLException("Failed to create batch");
    }

    public List<Map<String, Object>> getPurchaseHistory(int supplierId, Date from, Date to)
            throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT p.*, s.name AS supplier_name FROM PURCHASES p " +
                     "JOIN SUPPLIERS s ON s.supplier_id = p.supplier_id " +
                     "WHERE p.supplier_id = ? AND p.purchase_date BETWEEN ? AND ? " +
                     "ORDER BY p.purchase_date DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            ps.setDate(2, from);
            ps.setDate(3, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(Map.of(
                    "purchaseId",    rs.getInt("purchase_id"),
                    "invoiceNo",     rs.getString("invoice_no"),
                    "purchaseDate",  rs.getDate("purchase_date").toString(),
                    "supplierName",  rs.getString("supplier_name"),
                    "totalAmount",   rs.getBigDecimal("total_amount"),
                    "paymentStatus", rs.getString("payment_status")
                ));
            }
        }
        return list;
    }
}
