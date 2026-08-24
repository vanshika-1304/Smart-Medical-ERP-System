package com.smartmedical.dao;

import com.smartmedical.model.Route;
import com.smartmedical.model.Salesman;
import com.smartmedical.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SalesmanDAO {

    public List<Salesman> findAll() throws SQLException {
        List<Salesman> list = new ArrayList<>();
        String sql = "SELECT * FROM SALESMEN WHERE is_active='Y' ORDER BY name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapSalesman(rs));
        }
        return list;
    }

    public Salesman findById(int id) throws SQLException {
        String sql = "SELECT * FROM SALESMEN WHERE salesman_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapSalesman(rs);
        }
        return null;
    }

    public int create(Salesman s, int createdBy) throws SQLException {
        String sql = "INSERT INTO SALESMEN (name, phone, commission_pct, is_active, created_by) VALUES (?,?,?,'Y',?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"SALESMAN_ID"})) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getPhone());
            ps.setBigDecimal(3, s.getCommissionPct());
            ps.setInt(4, createdBy);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    public void update(Salesman s) throws SQLException {
        String sql = "UPDATE SALESMEN SET name=?, phone=?, commission_pct=? WHERE salesman_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setString(2, s.getPhone());
            ps.setBigDecimal(3, s.getCommissionPct());
            ps.setInt(4, s.getSalesmanId());
            ps.executeUpdate();
        }
    }

    public void deactivate(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE SALESMEN SET is_active='N' WHERE salesman_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Route> findAllRoutes() throws SQLException {
        List<Route> list = new ArrayList<>();
        String sql = "SELECT r.*, s.name AS salesman_name FROM ROUTES r " +
                     "LEFT JOIN SALESMEN s ON s.salesman_id = r.salesman_id " +
                     "WHERE r.is_active='Y' ORDER BY r.route_name";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRoute(rs));
        }
        return list;
    }

    public Route findRouteById(int id) throws SQLException {
        String sql = "SELECT r.*, s.name AS salesman_name FROM ROUTES r " +
                     "LEFT JOIN SALESMEN s ON s.salesman_id = r.salesman_id WHERE r.route_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRoute(rs);
        }
        return null;
    }

    public int createRoute(Route r, int createdBy) throws SQLException {
        String sql = "INSERT INTO ROUTES (route_name, area, salesman_id, is_active, created_by) VALUES (?,?,?,'Y',?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"ROUTE_ID"})) {
            ps.setString(1, r.getRouteName());
            ps.setString(2, r.getArea());
            if (r.getSalesmanId() > 0) ps.setInt(3, r.getSalesmanId());
            else ps.setNull(3, Types.INTEGER);
            ps.setInt(4, createdBy);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    public void updateRoute(Route r) throws SQLException {
        String sql = "UPDATE ROUTES SET route_name=?, area=?, salesman_id=? WHERE route_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getRouteName());
            ps.setString(2, r.getArea());
            if (r.getSalesmanId() > 0) ps.setInt(3, r.getSalesmanId());
            else ps.setNull(3, Types.INTEGER);
            ps.setInt(4, r.getRouteId());
            ps.executeUpdate();
        }
    }

    public void deactivateRoute(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE ROUTES SET is_active='N' WHERE route_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> getSalesmanPerformance(Date from, Date to) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT sm.salesman_id, sm.name, sm.commission_pct, " +
                     "COUNT(s.sale_id) AS bill_count, NVL(SUM(s.net_total),0) AS total_sales, " +
                     "NVL(SUM(s.net_total),0) * sm.commission_pct / 100 AS commission " +
                     "FROM SALESMEN sm LEFT JOIN SALES s ON s.salesman_id = sm.salesman_id " +
                     "AND s.sale_date BETWEEN ? AND ? WHERE sm.is_active='Y' " +
                     "GROUP BY sm.salesman_id, sm.name, sm.commission_pct ORDER BY total_sales DESC NULLS LAST";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, from); ps.setDate(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(Map.of("salesmanId", rs.getInt("salesman_id"),
                    "name", rs.getString("name"),
                    "commissionPct", rs.getBigDecimal("commission_pct"),
                    "billCount", rs.getInt("bill_count"),
                    "totalSales", rs.getBigDecimal("total_sales"),
                    "commission", rs.getBigDecimal("commission")));
            }
        }
        return list;
    }

    public List<Map<String, Object>> getRouteWiseReport(Date from, Date to) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT r.route_id, r.route_name, r.area, COUNT(s.sale_id) AS bill_count, " +
                     "NVL(SUM(s.net_total),0) AS total_sales FROM ROUTES r " +
                     "LEFT JOIN CUSTOMERS c ON c.route_id = r.route_id " +
                     "LEFT JOIN SALES s ON s.customer_id = c.customer_id AND s.sale_date BETWEEN ? AND ? " +
                     "WHERE r.is_active='Y' GROUP BY r.route_id, r.route_name, r.area " +
                     "ORDER BY total_sales DESC NULLS LAST";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, from); ps.setDate(2, to);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String area = rs.getString("area");
                list.add(Map.of("routeId", rs.getInt("route_id"),
                    "routeName", rs.getString("route_name"),
                    "area", area != null ? area : "",
                    "billCount", rs.getInt("bill_count"),
                    "totalSales", rs.getBigDecimal("total_sales")));
            }
        }
        return list;
    }

    private Salesman mapSalesman(ResultSet rs) throws SQLException {
        Salesman s = new Salesman();
        s.setSalesmanId(rs.getInt("salesman_id"));
        s.setName(rs.getString("name"));
        s.setPhone(rs.getString("phone"));
        s.setCommissionPct(rs.getBigDecimal("commission_pct"));
        s.setActive("Y".equals(rs.getString("is_active")));
        return s;
    }

    private Route mapRoute(ResultSet rs) throws SQLException {
        Route r = new Route();
        r.setRouteId(rs.getInt("route_id"));
        r.setRouteName(rs.getString("route_name"));
        r.setArea(rs.getString("area"));
        r.setSalesmanId(rs.getInt("salesman_id"));
        r.setSalesmanName(rs.getString("salesman_name"));
        r.setActive("Y".equals(rs.getString("is_active")));
        return r;
    }
}
