package com.smartmedical.servlet.salesman;

import com.smartmedical.dao.SalesmanDAO;
import com.smartmedical.model.Route;
import com.smartmedical.model.Salesman;
import com.smartmedical.model.User;
import com.smartmedical.util.JsonUtil;
import com.smartmedical.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Map;
import java.util.logging.Logger;

/**
 * SalesmanServlet — URL: /salesmen/*
 *   GET  /salesmen          → Salesman list (FR-60)
 *   GET  /salesmen/routes   → Route list (FR-61)
 *   GET  /salesmen/report   → Performance report (FR-63)
 *   GET  /salesmen/route-report → Route-wise report (FR-64)
 *   POST /salesmen/add      → Add salesman
 *   POST /salesmen/update   → Update salesman
 *   POST /salesmen/delete   → Deactivate salesman
 *   POST /salesmen/routes/add    → Add route
 *   POST /salesmen/routes/update → Update route
 *   POST /salesmen/routes/delete → Deactivate route
 */
@WebServlet("/salesmen/*")
public class SalesmanServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SalesmanServlet.class.getName());
    private final SalesmanDAO salesmanDAO = new SalesmanDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!SessionUtil.hasRole(req, "ADMIN")) {
            resp.sendError(403, "Access denied");
            return;
        }

        String path = req.getPathInfo();
        if (path == null) path = "/";

        try {
            switch (path) {
                case "/", "/list" -> {
                    req.setAttribute("salesmen", salesmanDAO.findAll());
                    req.getRequestDispatcher("/jsp/salesman/salesman-list.jsp").forward(req, resp);
                }
                case "/routes" -> {
                    req.setAttribute("routes", salesmanDAO.findAllRoutes());
                    req.setAttribute("salesmen", salesmanDAO.findAll());
                    req.getRequestDispatcher("/jsp/salesman/route-list.jsp").forward(req, resp);
                }
                case "/report" -> {
                    String from = req.getParameter("from") != null ? req.getParameter("from")
                                  : LocalDate.now().withDayOfMonth(1).toString();
                    String to   = req.getParameter("to") != null ? req.getParameter("to")
                                  : LocalDate.now().toString();
                    req.setAttribute("reportData", salesmanDAO.getSalesmanPerformance(
                            Date.valueOf(from), Date.valueOf(to)));
                    req.setAttribute("from", from);
                    req.setAttribute("to", to);
                    req.getRequestDispatcher("/jsp/salesman/salesman-report.jsp").forward(req, resp);
                }
                case "/route-report" -> {
                    String from = req.getParameter("from") != null ? req.getParameter("from")
                                  : LocalDate.now().withDayOfMonth(1).toString();
                    String to   = req.getParameter("to") != null ? req.getParameter("to")
                                  : LocalDate.now().toString();
                    req.setAttribute("reportData", salesmanDAO.getRouteWiseReport(
                            Date.valueOf(from), Date.valueOf(to)));
                    req.getRequestDispatcher("/jsp/salesman/route-report.jsp").forward(req, resp);
                }
                default -> resp.sendError(404);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
            resp.sendError(500);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!SessionUtil.hasRole(req, "ADMIN")) {
            JsonUtil.writeError(resp, 403, "Access denied");
            return;
        }
        User user = SessionUtil.getLoggedInUser(req);
        String path = req.getPathInfo();
        if (path == null) path = "/";

        try {
            switch (path) {
                case "/add" -> {
                    Salesman s = buildSalesman(req);
                    int id = salesmanDAO.create(s, user.getUserId());
                    JsonUtil.writeSuccess(resp, "Salesman added", Map.of("salesmanId", id));
                }
                case "/update" -> {
                    Salesman s = buildSalesman(req);
                    s.setSalesmanId(Integer.parseInt(req.getParameter("salesmanId")));
                    salesmanDAO.update(s);
                    JsonUtil.writeSuccess(resp, "Salesman updated");
                }
                case "/delete" -> {
                    salesmanDAO.deactivate(Integer.parseInt(req.getParameter("salesmanId")));
                    JsonUtil.writeSuccess(resp, "Salesman deactivated");
                }
                case "/routes/add" -> {
                    Route r = buildRoute(req);
                    int id = salesmanDAO.createRoute(r, user.getUserId());
                    JsonUtil.writeSuccess(resp, "Route added", Map.of("routeId", id));
                }
                case "/routes/update" -> {
                    Route r = buildRoute(req);
                    r.setRouteId(Integer.parseInt(req.getParameter("routeId")));
                    salesmanDAO.updateRoute(r);
                    JsonUtil.writeSuccess(resp, "Route updated");
                }
                case "/routes/delete" -> {
                    salesmanDAO.deactivateRoute(Integer.parseInt(req.getParameter("routeId")));
                    JsonUtil.writeSuccess(resp, "Route deactivated");
                }
                default -> resp.sendError(404);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }

    private Salesman buildSalesman(HttpServletRequest req) {
        Salesman s = new Salesman();
        s.setName(req.getParameter("name"));
        s.setPhone(req.getParameter("phone"));
        String cp = req.getParameter("commissionPct");
        s.setCommissionPct(cp != null && !cp.isBlank() ? new BigDecimal(cp) : BigDecimal.ZERO);
        return s;
    }

    private Route buildRoute(HttpServletRequest req) {
        Route r = new Route();
        r.setRouteName(req.getParameter("routeName"));
        r.setArea(req.getParameter("area"));
        String sid = req.getParameter("salesmanId");
        r.setSalesmanId(sid != null && !sid.isBlank() ? Integer.parseInt(sid) : 0);
        return r;
    }
}
