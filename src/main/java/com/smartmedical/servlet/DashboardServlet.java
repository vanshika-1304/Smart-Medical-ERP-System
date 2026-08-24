package com.smartmedical.servlet;

import com.smartmedical.dao.ReportDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(DashboardServlet.class.getName());
    private final ReportDAO reportDAO = new ReportDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            Map<String, Object> counts = reportDAO.getDashboardCounts();
            req.setAttribute("dashboard", counts);
            req.getRequestDispatcher("/jsp/dashboard.jsp").forward(req, resp);
        } catch (Exception e) {
            logger.severe("Dashboard error: " + e.getMessage());
            req.setAttribute("error", "Failed to load dashboard.");
            req.getRequestDispatcher("/jsp/dashboard.jsp").forward(req, resp);
        }
    }
}
