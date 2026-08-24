package com.smartmedical.dao;

import com.smartmedical.model.Customer;
import com.smartmedical.model.Supplier;
import com.smartmedical.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT c.*, r.route_name FROM CUSTOMERS c " +
                     "LEFT JOIN ROUTES r ON r.route_id = c.route_id " +
                     "WHERE c.is_active='Y' ORDER BY c.shop_name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Customer findById(int id) throws SQLException {
        String sql = "SELECT c.*, r.route_name FROM CUSTOMERS c " +
                     "LEFT JOIN ROUTES r ON r.route_id = c.route_id WHERE c.customer_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public List<Customer> search(String keyword) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT c.*, r.route_name FROM CUSTOMERS c " +
                     "LEFT JOIN ROUTES r ON r.route_id = c.route_id " +
                     "WHERE c.is_active='Y' AND " +
                     "(UPPER(c.shop_name) LIKE UPPER(?) OR UPPER(c.owner_name) LIKE UPPER(?) " +
                     "OR c.phone LIKE ?) ORDER BY c.shop_name FETCH FIRST 20 ROWS ONLY";
        String kw = "%" + keyword + "%";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public int create(Customer c, int createdBy) throws SQLException {
        String sql = "INSERT INTO CUSTOMERS (shop_name, owner_name, phone, gstin, address, " +
                     "route_id, credit_limit, created_by) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"CUSTOMER_ID"})) {
            ps.setString(1, c.getShopName());
            ps.setString(2, c.getOwnerName());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getGstin());
            ps.setString(5, c.getAddress());
            if (c.getRouteId() > 0) ps.setInt(6, c.getRouteId()); else ps.setNull(6, Types.INTEGER);
            ps.setBigDecimal(7, c.getCreditLimit());
            ps.setInt(8, createdBy);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    public void update(Customer c) throws SQLException {
        String sql = "UPDATE CUSTOMERS SET shop_name=?, owner_name=?, phone=?, gstin=?, " +
                     "address=?, route_id=?, credit_limit=? WHERE customer_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getShopName());
            ps.setString(2, c.getOwnerName());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getGstin());
            ps.setString(5, c.getAddress());
            if (c.getRouteId() > 0) ps.setInt(6, c.getRouteId()); else ps.setNull(6, Types.INTEGER);
            ps.setBigDecimal(7, c.getCreditLimit());
            ps.setInt(8, c.getCustomerId());
            ps.executeUpdate();
        }
    }

    public void deactivate(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE CUSTOMERS SET is_active='N' WHERE customer_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setShopName(rs.getString("shop_name"));
        c.setOwnerName(rs.getString("owner_name"));
        c.setPhone(rs.getString("phone"));
        c.setGstin(rs.getString("gstin"));
        c.setAddress(rs.getString("address"));
        c.setRouteId(rs.getInt("route_id"));
        c.setRouteName(rs.getString("route_name"));
        c.setCreditLimit(rs.getBigDecimal("credit_limit"));
        c.setOutstandingBalance(rs.getBigDecimal("outstanding_balance"));
        c.setActive("Y".equals(rs.getString("is_active")));
        return c;
    }
}
