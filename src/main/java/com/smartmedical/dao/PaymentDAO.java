package com.smartmedical.dao;

import com.smartmedical.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PaymentDAO — handles:
 *  - Customer receipt recording (reduces customer outstanding)
 *  - Supplier payment recording (reduces supplier outstanding)
 *  - Outstanding aging report
 */
public class PaymentDAO {

    /**
     * Record a receipt from a customer (FR-50, FR-52)
     * party_type = 'C'
     */
    public int recordCustomerReceipt(int customerId, BigDecimal amount, String mode,
                                     String refNo, Integer saleId, int createdBy)
            throws SQLException {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            int paymentId = insertPayment(con, 'C', customerId, amount, mode, refNo, saleId, null, createdBy);

            // Reduce customer outstanding (FR-52)
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE CUSTOMERS SET outstanding_balance = outstanding_balance - ? " +
                    "WHERE customer_id = ?")) {
                ps.setBigDecimal(1, amount);
                ps.setInt(2, customerId);
                ps.executeUpdate();
            }

            // Update sale payment_status if bill-wise (FR-51)
            if (saleId != null) {
                updateSalePaymentStatus(con, saleId, amount);
            }

            con.commit();
            return paymentId;
        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            DBConnection.close(con);
        }
    }

    /**
     * Record a payment to a supplier (FR-53, FR-54)
     * party_type = 'S'
     */
    public int recordSupplierPayment(int supplierId, BigDecimal amount, String mode,
                                     String refNo, Integer purchaseId, int createdBy)
            throws SQLException {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            int paymentId = insertPayment(con, 'S', supplierId, amount, mode, refNo, null, purchaseId, createdBy);

            // Reduce supplier outstanding (FR-54)
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE SUPPLIERS SET outstanding_balance = outstanding_balance - ? " +
                    "WHERE supplier_id = ?")) {
                ps.setBigDecimal(1, amount);
                ps.setInt(2, supplierId);
                ps.executeUpdate();
            }

            if (purchaseId != null) {
                updatePurchasePaymentStatus(con, purchaseId, amount);
            }

            con.commit();
            return paymentId;
        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            DBConnection.close(con);
        }
    }

    /**
     * Outstanding aging report (FR-57)
     * Buckets: 0-30, 31-60, 61-90, 90+ days
     */
    public List<Map<String, Object>> getCustomerOutstandingAging() throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT c.customer_id, c.shop_name, c.owner_name, c.phone,
                   SUM(CASE WHEN SYSDATE - s.sale_date <= 30 THEN s.net_total ELSE 0 END) AS bucket_0_30,
                   SUM(CASE WHEN SYSDATE - s.sale_date BETWEEN 31 AND 60 THEN s.net_total ELSE 0 END) AS bucket_31_60,
                   SUM(CASE WHEN SYSDATE - s.sale_date BETWEEN 61 AND 90 THEN s.net_total ELSE 0 END) AS bucket_61_90,
                   SUM(CASE WHEN SYSDATE - s.sale_date > 90 THEN s.net_total ELSE 0 END) AS bucket_90plus,
                   c.outstanding_balance AS total_outstanding
            FROM CUSTOMERS c
            LEFT JOIN SALES s ON s.customer_id = c.customer_id
                AND s.payment_status IN ('PENDING','PARTIAL')
            WHERE c.is_active = 'Y'
            GROUP BY c.customer_id, c.shop_name, c.owner_name, c.phone, c.outstanding_balance
            HAVING c.outstanding_balance > 0
            ORDER BY c.outstanding_balance DESC
            """;
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("customerId", rs.getInt("customer_id"));
                row.put("shopName", rs.getString("shop_name"));
                row.put("ownerName", rs.getString("owner_name"));
                row.put("phone", rs.getString("phone"));
                row.put("bucket0_30", rs.getBigDecimal("bucket_0_30"));
                row.put("bucket31_60", rs.getBigDecimal("bucket_31_60"));
                row.put("bucket61_90", rs.getBigDecimal("bucket_61_90"));
                row.put("bucket90plus", rs.getBigDecimal("bucket_90plus"));
                row.put("totalOutstanding", rs.getBigDecimal("total_outstanding"));
                list.add(row);
            }
        }
        return list;
    }

    /** Customer ledger — all bills and payments */
    public List<Map<String, Object>> getCustomerLedger(int customerId) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        // Bills
        String sqlBills = "SELECT 'BILL' AS txn_type, bill_no AS ref, sale_date AS txn_date, " +
                          "net_total AS debit, 0 AS credit, payment_status " +
                          "FROM SALES WHERE customer_id = ? " +
                          "UNION ALL " +
                          "SELECT 'RECEIPT', reference_no, payment_date, 0, amount, 'PAID' " +
                          "FROM PAYMENTS WHERE party_type='C' AND party_id = ? " +
                          "ORDER BY txn_date DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlBills)) {
            ps.setInt(1, customerId);
            ps.setInt(2, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(Map.of(
                    "txnType",  rs.getString("txn_type"),
                    "ref",      rs.getString("ref") != null ? rs.getString("ref") : "",
                    "txnDate",  rs.getDate("txn_date").toString(),
                    "debit",    rs.getBigDecimal("debit"),
                    "credit",   rs.getBigDecimal("credit"),
                    "status",   rs.getString("payment_status")
                ));
            }
        }
        return list;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private int insertPayment(Connection con, char partyType, int partyId,
                               BigDecimal amount, String mode, String refNo,
                               Integer saleId, Integer purchaseId, int createdBy)
            throws SQLException {
        String sql = "INSERT INTO PAYMENTS (party_type, party_id, payment_mode, amount, " +
                     "reference_no, payment_date, sale_id, purchase_id, created_by) " +
                     "VALUES (?,?,?,?,?,SYSDATE,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql, new String[]{"PAYMENT_ID"})) {
            ps.setString(1, String.valueOf(partyType));
            ps.setInt(2, partyId);
            ps.setString(3, mode);
            ps.setBigDecimal(4, amount);
            ps.setString(5, refNo);
            if (saleId != null) ps.setInt(6, saleId); else ps.setNull(6, Types.INTEGER);
            if (purchaseId != null) ps.setInt(7, purchaseId); else ps.setNull(7, Types.INTEGER);
            ps.setInt(8, createdBy);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        throw new SQLException("Failed to insert payment");
    }

    private void updateSalePaymentStatus(Connection con, int saleId, BigDecimal paidNow)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT net_total, " +
                "(SELECT NVL(SUM(amount),0) FROM PAYMENTS WHERE sale_id=? AND party_type='C') AS paid " +
                "FROM SALES WHERE sale_id=?")) {
            ps.setInt(1, saleId);
            ps.setInt(2, saleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal net  = rs.getBigDecimal("net_total");
                BigDecimal paid = rs.getBigDecimal("paid").add(paidNow);
                String status = paid.compareTo(net) >= 0 ? "PAID" :
                                paid.compareTo(BigDecimal.ZERO) > 0 ? "PARTIAL" : "PENDING";
                try (PreparedStatement upd = con.prepareStatement(
                        "UPDATE SALES SET payment_status=? WHERE sale_id=?")) {
                    upd.setString(1, status);
                    upd.setInt(2, saleId);
                    upd.executeUpdate();
                }
            }
        }
    }

    private void updatePurchasePaymentStatus(Connection con, int purchaseId, BigDecimal paidNow)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT total_amount, " +
                "(SELECT NVL(SUM(amount),0) FROM PAYMENTS WHERE purchase_id=? AND party_type='S') AS paid " +
                "FROM PURCHASES WHERE purchase_id=?")) {
            ps.setInt(1, purchaseId);
            ps.setInt(2, purchaseId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal total = rs.getBigDecimal("total_amount");
                BigDecimal paid  = rs.getBigDecimal("paid").add(paidNow);
                String status = paid.compareTo(total) >= 0 ? "PAID" :
                                paid.compareTo(BigDecimal.ZERO) > 0 ? "PARTIAL" : "PENDING";
                try (PreparedStatement upd = con.prepareStatement(
                        "UPDATE PURCHASES SET payment_status=? WHERE purchase_id=?")) {
                    upd.setString(1, status);
                    upd.setInt(2, purchaseId);
                    upd.executeUpdate();
                }
            }
        }
    }
}
