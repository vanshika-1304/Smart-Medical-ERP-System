package com.smartmedical.dao;

import com.smartmedical.model.Supplier;
import com.smartmedical.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public List<Supplier> findAll() throws SQLException {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM SUPPLIERS WHERE is_active='Y' ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Supplier findById(int id) throws SQLException {
        String sql = "SELECT * FROM SUPPLIERS WHERE supplier_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public int create(Supplier s, int createdBy) throws SQLException {
        String sql = "INSERT INTO SUPPLIERS (name, contact_no, gstin, address, credit_limit, created_by) " +
                     "VALUES (?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"SUPPLIER_ID"})) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getContactNo());
            ps.setString(3, s.getGstin());
            ps.setString(4, s.getAddress());
            ps.setBigDecimal(5, s.getCreditLimit());
            ps.setInt(6, createdBy);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    public void update(Supplier s) throws SQLException {
        String sql = "UPDATE SUPPLIERS SET name=?, contact_no=?, gstin=?, address=?, " +
                     "credit_limit=? WHERE supplier_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getContactNo());
            ps.setString(3, s.getGstin());
            ps.setString(4, s.getAddress());
            ps.setBigDecimal(5, s.getCreditLimit());
            ps.setInt(6, s.getSupplierId());
            ps.executeUpdate();
        }
    }

    public void deactivate(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE SUPPLIERS SET is_active='N' WHERE supplier_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setSupplierId(rs.getInt("supplier_id"));
        s.setName(rs.getString("name"));
        s.setContactNo(rs.getString("contact_no"));
        s.setGstin(rs.getString("gstin"));
        s.setAddress(rs.getString("address"));
        s.setCreditLimit(rs.getBigDecimal("credit_limit"));
        s.setOutstandingBalance(rs.getBigDecimal("outstanding_balance"));
        s.setActive("Y".equals(rs.getString("is_active")));
        return s;
    }
}
