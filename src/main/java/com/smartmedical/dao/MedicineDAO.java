package com.smartmedical.dao;

import com.smartmedical.model.Medicine;
import com.smartmedical.model.MedicineBatch;
import com.smartmedical.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {

    // ── Medicine Master ──────────────────────────────────────────────────────

    public List<Medicine> findAll() throws SQLException {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM MEDICINES WHERE is_active = 'Y' ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapMedicine(rs));
        }
        return list;
    }

    public Medicine findById(int id) throws SQLException {
        String sql = "SELECT * FROM MEDICINES WHERE medicine_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapMedicine(rs);
        }
        return null;
    }

    /** Live search for billing — name, company, composition */
    public List<Medicine> search(String keyword) throws SQLException {
        List<Medicine> list = new ArrayList<>();
        String sql = "SELECT * FROM MEDICINES WHERE is_active = 'Y' AND " +
                     "(UPPER(name) LIKE UPPER(?) OR UPPER(composition) LIKE UPPER(?)) " +
                     "ORDER BY name FETCH FIRST 20 ROWS ONLY";
        String kw = "%" + keyword + "%";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kw);
            ps.setString(2, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapMedicine(rs));
        }
        return list;
    }

    public int create(Medicine m, int createdBy) throws SQLException {
        String sql = "INSERT INTO MEDICINES (name, category, hsn_code, composition, company, " +
                     "gst_pct, rack_location, created_by) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"MEDICINE_ID"})) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getCategory());
            ps.setString(3, m.getHsnCode());
            ps.setString(4, m.getComposition());
            ps.setString(5, m.getCompany());
            ps.setBigDecimal(6, m.getGstPct());
            ps.setString(7, m.getRackLocation());
            ps.setInt(8, createdBy);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    public void update(Medicine m) throws SQLException {
        String sql = "UPDATE MEDICINES SET name=?, category=?, hsn_code=?, composition=?, " +
                     "company=?, gst_pct=?, rack_location=? WHERE medicine_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getCategory());
            ps.setString(3, m.getHsnCode());
            ps.setString(4, m.getComposition());
            ps.setString(5, m.getCompany());
            ps.setBigDecimal(6, m.getGstPct());
            ps.setString(7, m.getRackLocation());
            ps.setInt(8, m.getMedicineId());
            ps.executeUpdate();
        }
    }

    public void deactivate(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE MEDICINES SET is_active='N' WHERE medicine_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── Medicine Batches ─────────────────────────────────────────────────────

    /** FEFO — First Expiry First Out: get batches ordered by earliest expiry */
    public List<MedicineBatch> getBatchesFEFO(int medicineId) throws SQLException {
        List<MedicineBatch> list = new ArrayList<>();
        String sql = "SELECT b.*, m.name AS medicine_name FROM MEDICINE_BATCHES b " +
                     "JOIN MEDICINES m ON m.medicine_id = b.medicine_id " +
                     "WHERE b.medicine_id = ? AND b.is_active = 'Y' " +
                     "AND b.stock_qty > 0 AND b.expiry_date > SYSDATE " +
                     "ORDER BY b.expiry_date ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapBatch(rs));
        }
        return list;
    }

    /** All batches including expired / zero stock */
    public List<MedicineBatch> getAllBatches(int medicineId) throws SQLException {
        List<MedicineBatch> list = new ArrayList<>();
        String sql = "SELECT b.*, m.name AS medicine_name FROM MEDICINE_BATCHES b " +
                     "JOIN MEDICINES m ON m.medicine_id = b.medicine_id " +
                     "WHERE b.medicine_id = ? ORDER BY b.expiry_date";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapBatch(rs));
        }
        return list;
    }

    public MedicineBatch findBatchById(int batchId) throws SQLException {
        String sql = "SELECT b.*, m.name AS medicine_name FROM MEDICINE_BATCHES b " +
                     "JOIN MEDICINES m ON m.medicine_id = b.medicine_id WHERE b.batch_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, batchId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapBatch(rs);
        }
        return null;
    }

    public int createBatch(MedicineBatch b, int createdBy) throws SQLException {
        // Use RETURNING clause for Oracle IDENTITY columns — getGeneratedKeys() is unreliable
        // with GENERATED ALWAYS AS IDENTITY in some Oracle JDBC versions
        String sql = "INSERT INTO MEDICINE_BATCHES (medicine_id, batch_no, expiry_date, mrp, " +
                     "purchase_rate, stock_qty, min_stock_alert, created_by) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection()) {
            // Use int[] column index instead of column name — avoids case-sensitivity issue
            PreparedStatement ps = con.prepareStatement(sql, new int[]{1});
            ps.setInt(1, b.getMedicineId());
            ps.setString(2, b.getBatchNo());
            ps.setDate(3, b.getExpiryDate());
            ps.setBigDecimal(4, b.getMrp());
            ps.setBigDecimal(5, b.getPurchaseRate());
            ps.setInt(6, b.getStockQty());
            ps.setInt(7, b.getMinStockAlert());
            ps.setInt(8, createdBy);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int newId = keys.getInt(1);
                ps.close();
                return newId;
            }
            ps.close();
        }
        return -1;
    }

    /** Increase stock on purchase */
    public void increaseStock(int batchId, int qty, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE MEDICINE_BATCHES SET stock_qty = stock_qty + ? WHERE batch_id = ?")) {
            ps.setInt(1, qty);
            ps.setInt(2, batchId);
            ps.executeUpdate();
        }
    }

    /** Decrease stock on sale — throws if insufficient */
    public void decreaseStock(int batchId, int qty, Connection con) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE MEDICINE_BATCHES SET stock_qty = stock_qty - ? " +
                "WHERE batch_id = ? AND stock_qty >= ?")) {
            ps.setInt(1, qty);
            ps.setInt(2, batchId);
            ps.setInt(3, qty);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Insufficient stock for batch_id=" + batchId);
        }
    }

    /** Expiry alerts — medicines expiring within given days */
    public List<MedicineBatch> getExpiryAlerts(int days) throws SQLException {
        List<MedicineBatch> list = new ArrayList<>();
        String sql = "SELECT b.*, m.name AS medicine_name FROM MEDICINE_BATCHES b " +
                     "JOIN MEDICINES m ON m.medicine_id = b.medicine_id " +
                     "WHERE b.expiry_date <= SYSDATE + ? AND b.stock_qty > 0 AND b.is_active='Y' " +
                     "ORDER BY b.expiry_date ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, days);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapBatch(rs));
        }
        return list;
    }

    /** Low stock alerts */
    public List<MedicineBatch> getLowStockAlerts() throws SQLException {
        List<MedicineBatch> list = new ArrayList<>();
        String sql = "SELECT b.*, m.name AS medicine_name FROM MEDICINE_BATCHES b " +
                     "JOIN MEDICINES m ON m.medicine_id = b.medicine_id " +
                     "WHERE b.stock_qty <= b.min_stock_alert AND b.is_active='Y' " +
                     "ORDER BY b.stock_qty ASC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapBatch(rs));
        }
        return list;
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

    private Medicine mapMedicine(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setMedicineId(rs.getInt("medicine_id"));
        m.setName(rs.getString("name"));
        m.setCategory(rs.getString("category"));
        m.setHsnCode(rs.getString("hsn_code"));
        m.setComposition(rs.getString("composition"));
        m.setCompany(rs.getString("company"));
        m.setGstPct(rs.getBigDecimal("gst_pct"));
        m.setRackLocation(rs.getString("rack_location"));
        m.setActive("Y".equals(rs.getString("is_active")));
        return m;
    }

    private MedicineBatch mapBatch(ResultSet rs) throws SQLException {
        MedicineBatch b = new MedicineBatch();
        b.setBatchId(rs.getInt("batch_id"));
        b.setMedicineId(rs.getInt("medicine_id"));
        b.setMedicineName(rs.getString("medicine_name"));
        b.setBatchNo(rs.getString("batch_no"));
        b.setExpiryDate(rs.getDate("expiry_date"));
        b.setMrp(rs.getBigDecimal("mrp"));
        b.setPurchaseRate(rs.getBigDecimal("purchase_rate"));
        b.setStockQty(rs.getInt("stock_qty"));
        b.setMinStockAlert(rs.getInt("min_stock_alert"));
        b.setActive("Y".equals(rs.getString("is_active")));
        return b;
    }
}
