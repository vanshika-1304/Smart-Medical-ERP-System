package com.smartmedical.dao;

import com.smartmedical.model.MedicineBatch;
import com.smartmedical.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SaleDAO — handles complete sale creation in a single ACID transaction:
 *  1. Insert SALES header
 *  2. Insert SALE_ITEMS lines
 *  3. Decrease MEDICINE_BATCHES stock (FEFO)
 *  4. Increase CUSTOMERS.outstanding_balance
 */
public class SaleDAO {

    private final MedicineDAO medicineDAO = new MedicineDAO();

    /**
     * @param customerId    customer
     * @param salesmanId    salesman (0 if none)
     * @param items         List of Map with keys: medicineId, batchId, qty, discount
     * @param createdBy     logged-in user id
     * @return new sale_id
     */
    public int createSale(int customerId, int salesmanId,
                          List<Map<String, Object>> items, int createdBy)
            throws SQLException {

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // ── 1. Credit limit check ────────────────────────────────────────
            BigDecimal outstanding = getOutstanding(con, customerId);
            BigDecimal creditLimit = getCreditLimit(con, customerId);
            // Only block if credit limit is explicitly set (> 0) AND outstanding exceeds it
            if (creditLimit.compareTo(BigDecimal.ZERO) > 0 
                    && outstanding.compareTo(creditLimit) >= 0) {
                throw new SQLException("CREDIT_LIMIT_EXCEEDED: Customer has exceeded credit limit.");
            }

            // ── 2. Generate bill number ──────────────────────────────────────
            String billNo = generateBillNo(con);

            // ── 3. Calculate totals ──────────────────────────────────────────
            BigDecimal subtotal = BigDecimal.ZERO;
            BigDecimal totalGst = BigDecimal.ZERO;
            BigDecimal totalDiscount = BigDecimal.ZERO;

            // Build enriched item list
            record SaleLineCalc(int medicineId, int batchId, int qty,
                                BigDecimal mrp, BigDecimal purchaseRate,
                                BigDecimal discountAmt, BigDecimal gstPct,
                                BigDecimal amount) {}

            List<SaleLineCalc> lines = new ArrayList<>();

            for (Map<String, Object> item : items) {
                int medicineId = (int) item.get("medicineId");
                int batchId    = (int) item.get("batchId");
                int qty        = (int) item.get("qty");
                BigDecimal discPct = (BigDecimal) item.getOrDefault("discount", BigDecimal.ZERO);

                MedicineBatch batch = medicineDAO.findBatchById(batchId);
                if (batch == null) throw new SQLException("Batch not found: " + batchId);

                // GST calculation (BRD 4.2):
                // Net = (MRP × Qty) − Discount + GST
                // GST = (MRP × Qty - Discount) × GST% / 100
                BigDecimal base       = batch.getMrp().multiply(BigDecimal.valueOf(qty));
                BigDecimal discAmt    = base.multiply(discPct)
                                            .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                BigDecimal taxable    = base.subtract(discAmt);
                BigDecimal gstPct     = getGstPct(con, medicineId);
                BigDecimal gstAmt     = taxable.multiply(gstPct)
                                               .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                BigDecimal lineAmount = taxable.add(gstAmt);

                subtotal       = subtotal.add(taxable);
                totalGst       = totalGst.add(gstAmt);
                totalDiscount  = totalDiscount.add(discAmt);

                lines.add(new SaleLineCalc(medicineId, batchId, qty,
                        batch.getMrp(), batch.getPurchaseRate(),
                        discAmt, gstPct, lineAmount));
            }

            BigDecimal netTotal = subtotal.add(totalGst);

            // ── 4. Insert SALES header ───────────────────────────────────────
            int saleId;
            String sqlSale = "INSERT INTO SALES (customer_id, salesman_id, bill_no, sale_date, " +
                             "subtotal, discount, gst, net_total, payment_status, created_by) " +
                             "VALUES (?,?,?,SYSDATE,?,?,?,?,'PENDING',?)";
            try (PreparedStatement ps = con.prepareStatement(sqlSale, new String[]{"SALE_ID"})) {
                ps.setInt(1, customerId);
                if (salesmanId > 0) ps.setInt(2, salesmanId); else ps.setNull(2, Types.INTEGER);
                ps.setString(3, billNo);
                ps.setBigDecimal(4, subtotal);
                ps.setBigDecimal(5, totalDiscount);
                ps.setBigDecimal(6, totalGst);
                ps.setBigDecimal(7, netTotal);
                ps.setInt(8, createdBy);
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (!keys.next()) throw new SQLException("Failed to get sale_id");
                saleId = keys.getInt(1);
            }

            // ── 5. Insert SALE_ITEMS + deduct stock ──────────────────────────
            String sqlItem = "INSERT INTO SALE_ITEMS (sale_id, medicine_id, batch_id, qty_sold, " +
                             "mrp, purchase_rate, discount, gst_pct, amount) VALUES (?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sqlItem)) {
                for (var line : lines) {
                    ps.setInt(1, saleId);
                    ps.setInt(2, line.medicineId());
                    ps.setInt(3, line.batchId());
                    ps.setInt(4, line.qty());
                    ps.setBigDecimal(5, line.mrp());
                    ps.setBigDecimal(6, line.purchaseRate());
                    ps.setBigDecimal(7, line.discountAmt());
                    ps.setBigDecimal(8, line.gstPct());
                    ps.setBigDecimal(9, line.amount());
                    ps.addBatch();

                    // Deduct stock (FEFO already chosen by caller via batchId)
                    medicineDAO.decreaseStock(line.batchId(), line.qty(), con);
                }
                ps.executeBatch();
            }

            // ── 6. Update customer outstanding ──────────────────────────────
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE CUSTOMERS SET outstanding_balance = outstanding_balance + ? " +
                    "WHERE customer_id = ?")) {
                ps.setBigDecimal(1, netTotal);
                ps.setInt(2, customerId);
                ps.executeUpdate();
            }

            con.commit();
            return saleId;

        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            DBConnection.close(con);
        }
    }

    /** Process sale return — reverses stock and outstanding */
    public void processSaleReturn(int saleId, int createdBy) throws SQLException {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Get sale items
            String sql = "SELECT * FROM SALE_ITEMS WHERE sale_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, saleId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int batchId = rs.getInt("batch_id");
                    int qty     = rs.getInt("qty_sold");
                    medicineDAO.increaseStock(batchId, qty, con); // restore stock
                }
            }

            // Get net_total, reduce outstanding
            BigDecimal netTotal;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT net_total, customer_id FROM SALES WHERE sale_id = ?")) {
                ps.setInt(1, saleId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) throw new SQLException("Sale not found: " + saleId);
                netTotal = rs.getBigDecimal("net_total");
                int customerId = rs.getInt("customer_id");

                try (PreparedStatement upd = con.prepareStatement(
                        "UPDATE CUSTOMERS SET outstanding_balance = outstanding_balance - ? " +
                        "WHERE customer_id = ?")) {
                    upd.setBigDecimal(1, netTotal);
                    upd.setInt(2, customerId);
                    upd.executeUpdate();
                }
            }

            // Mark sale as returned
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE SALES SET payment_status = 'RETURNED' WHERE sale_id = ?")) {
                ps.setInt(1, saleId);
                ps.executeUpdate();
            }

            con.commit();
        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            DBConnection.close(con);
        }
    }

    public List<Map<String, Object>> getSalesByDateRange(Date from, Date to) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT s.*, c.shop_name, sm.name AS salesman_name " +
                     "FROM SALES s " +
                     "JOIN CUSTOMERS c ON c.customer_id = s.customer_id " +
                     "LEFT JOIN SALESMEN sm ON sm.salesman_id = s.salesman_id " +
                     "WHERE s.sale_date BETWEEN ? AND ? ORDER BY s.sale_date DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, from);
            ps.setDate(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(Map.of(
                    "saleId",       rs.getInt("sale_id"),
                    "billNo",       rs.getString("bill_no"),
                    "saleDate",     rs.getDate("sale_date").toString(),
                    "shopName",     rs.getString("shop_name"),
                    "salesmanName", rs.getString("salesman_name") != null ? rs.getString("salesman_name") : "",
                    "netTotal",     rs.getBigDecimal("net_total"),
                    "paymentStatus",rs.getString("payment_status")
                ));
            }
        }
        return list;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private BigDecimal getOutstanding(Connection con, int customerId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT outstanding_balance FROM CUSTOMERS WHERE customer_id = ?")) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getBigDecimal("outstanding_balance") : BigDecimal.ZERO;
        }
    }

    private BigDecimal getCreditLimit(Connection con, int customerId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT credit_limit FROM CUSTOMERS WHERE customer_id = ?")) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getBigDecimal("credit_limit") : BigDecimal.ZERO;
        }
    }

    private BigDecimal getGstPct(Connection con, int medicineId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT gst_pct FROM MEDICINES WHERE medicine_id = ?")) {
            ps.setInt(1, medicineId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getBigDecimal("gst_pct") : BigDecimal.ZERO;
        }
    }

    private String generateBillNo(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT bill_no_seq.NEXTVAL FROM DUAL")) {
            rs.next();
            return "BILL-" + String.format("%06d", rs.getLong(1));
        }
    }
}
