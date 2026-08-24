package com.smartmedical.servlet.reports;

import com.smartmedical.dao.PaymentDAO;
import com.smartmedical.dao.ReportDAO;
import com.smartmedical.util.ExcelExportUtil;
import com.smartmedical.util.PdfExportUtil;
import com.smartmedical.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet("/reports/*")
public class ReportServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(ReportServlet.class.getName());
    private final ReportDAO  reportDAO  = new ReportDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!SessionUtil.hasRole(req, "ADMIN", "OWNER", "STAFF")) {
            resp.sendError(403); return;
        }

        String path   = req.getPathInfo(); if (path == null) path = "/";
        String format = req.getParameter("format");

        try {
            switch (path) {
                case "/daily" -> {
                    String d = req.getParameter("date") != null ? req.getParameter("date") : LocalDate.now().toString();
                    // Support date range from bill-list (from/to params override single date)
                    String from = req.getParameter("from") != null ? req.getParameter("from") : d;
                    String to   = req.getParameter("to")   != null ? req.getParameter("to")   : d;
                    List<Map<String,Object>> sales = reportDAO.getDailySalesReport(Date.valueOf(from), Date.valueOf(to));
                    String label = from.equals(to) ? from : (from + " to " + to);
                    if ("pdf".equals(format)) { writePdf(resp, PdfExportUtil.generateDailySalesReport(sales, label), "daily_sales.pdf"); }
                    else if ("excel".equals(format)) { writeExcel(resp, ExcelExportUtil.dailySalesReport(sales, label), "daily_sales.xlsx"); }
                    else { req.setAttribute("sales", sales); req.setAttribute("selectedDate", from);
                           req.getRequestDispatcher("/jsp/reports/daily-sales.jsp").forward(req, resp); }
                }
                case "/monthly" -> {
                    int month = req.getParameter("month") != null ? Integer.parseInt(req.getParameter("month")) : LocalDate.now().getMonthValue();
                    int year  = req.getParameter("year")  != null ? Integer.parseInt(req.getParameter("year"))  : LocalDate.now().getYear();
                    req.setAttribute("summary", reportDAO.getMonthlySalesReport(month, year));
                    req.setAttribute("month", month); req.setAttribute("year", year);
                    req.getRequestDispatcher("/jsp/reports/monthly-sales.jsp").forward(req, resp);
                }
                case "/profit" -> {
                    String from = req.getParameter("from") != null ? req.getParameter("from") : LocalDate.now().withDayOfMonth(1).toString();
                    String to   = req.getParameter("to")   != null ? req.getParameter("to")   : LocalDate.now().toString();
                    List<Map<String,Object>> data = reportDAO.getProfitReport(Date.valueOf(from), Date.valueOf(to));
                    if ("excel".equals(format)) { writeExcel(resp, ExcelExportUtil.profitReport(data, from, to), "profit.xlsx"); }
                    else { req.setAttribute("profitData", data); req.setAttribute("from", from); req.setAttribute("to", to);
                           req.getRequestDispatcher("/jsp/reports/profit.jsp").forward(req, resp); }
                }
                case "/gst" -> {
                    int month = req.getParameter("month") != null ? Integer.parseInt(req.getParameter("month")) : LocalDate.now().getMonthValue();
                    int year  = req.getParameter("year")  != null ? Integer.parseInt(req.getParameter("year"))  : LocalDate.now().getYear();
                    List<Map<String,Object>> gstData = reportDAO.getGstSummary(month, year);
                    if ("excel".equals(format)) { writeExcel(resp, ExcelExportUtil.gstSummaryReport(gstData, month, year), "gst_summary.xlsx"); }
                    else { req.setAttribute("gstData", gstData); req.setAttribute("month", month); req.setAttribute("year", year);
                           req.getRequestDispatcher("/jsp/reports/gst-summary.jsp").forward(req, resp); }
                }
                case "/salesman" -> {
                    String from = req.getParameter("from") != null ? req.getParameter("from") : LocalDate.now().withDayOfMonth(1).toString();
                    String to   = req.getParameter("to")   != null ? req.getParameter("to")   : LocalDate.now().toString();
                    List<Map<String,Object>> data = reportDAO.getSalesmanReport(Date.valueOf(from), Date.valueOf(to));
                    if ("excel".equals(format)) { writeExcel(resp, ExcelExportUtil.salesmanReport(data, from, to), "salesman.xlsx"); }
                    else { req.setAttribute("salesmanData", data); req.setAttribute("from", from); req.setAttribute("to", to);
                           req.getRequestDispatcher("/jsp/reports/salesman.jsp").forward(req, resp); }
                }
                case "/outstanding" -> {
                    List<Map<String,Object>> aging = paymentDAO.getCustomerOutstandingAging();
                    if ("pdf".equals(format)) { writePdf(resp, PdfExportUtil.generateOutstandingReport(aging), "outstanding.pdf"); }
                    else if ("excel".equals(format)) { writeExcel(resp, ExcelExportUtil.outstandingAgingReport(aging), "outstanding.xlsx"); }
                    else { req.setAttribute("aging", aging);
                           req.getRequestDispatcher("/jsp/reports/outstanding.jsp").forward(req, resp); }
                }
                default -> resp.sendError(404);
            }
        } catch (Exception e) {
            logger.severe("Report error: " + e.getMessage());
            resp.sendError(500);
        }
    }

    private void writePdf(HttpServletResponse resp, byte[] data, String filename) throws IOException {
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        resp.getOutputStream().write(data);
    }

    private void writeExcel(HttpServletResponse resp, byte[] data, String filename) throws IOException {
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        resp.getOutputStream().write(data);
    }
}