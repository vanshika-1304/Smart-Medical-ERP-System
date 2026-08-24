package com.smartmedical.servlet.billing;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartmedical.dao.CustomerDAO;
import com.smartmedical.dao.MedicineDAO;
import com.smartmedical.dao.SaleDAO;
import com.smartmedical.dao.SalesmanDAO;
import com.smartmedical.model.Customer;
import com.smartmedical.model.MedicineBatch;
import com.smartmedical.model.User;
import com.smartmedical.util.JsonUtil;
import com.smartmedical.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * BillingServlet — URL: /billing/*
 *   GET  /billing          → Show new bill form
 *   GET  /billing/list     → List of bills
 *   POST /billing/create   → Create new sale (JSON body)
 *   POST /billing/return   → Process sale return
 *   GET  /billing/search-medicine → AJAX medicine search
 *   GET  /billing/batches  → AJAX FEFO batches for a medicine
 */
@WebServlet("/billing/*")
public class BillingServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(BillingServlet.class.getName());

    private final MedicineDAO medicineDAO  = new MedicineDAO();
    private final CustomerDAO customerDAO  = new CustomerDAO();
    private final SaleDAO     saleDAO      = new SaleDAO();
    private final SalesmanDAO salesmanDAO  = new SalesmanDAO();
    private final Gson        gson         = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if (path == null) path = "/";

        switch (path) {
            case "/", "/new" -> {
                // Roles: ADMIN, STAFF only
                if (!SessionUtil.hasRole(req, "ADMIN", "STAFF")) {
                    resp.sendError(403, "Access denied");
                    return;
                }
                try {
                    req.setAttribute("customers", customerDAO.findAll());
                    req.setAttribute("salesmen",  salesmanDAO.findAll());
                    req.getRequestDispatcher("/jsp/billing/new-bill.jsp").forward(req, resp);
                } catch (Exception e) {
                    logger.severe(e.getMessage());
                    resp.sendError(500);
                }
            }
            case "/list" -> {
                try {
                    java.sql.Date from = java.sql.Date.valueOf(
                            req.getParameter("from") != null ? req.getParameter("from") :
                            java.time.LocalDate.now().toString());
                    java.sql.Date to = java.sql.Date.valueOf(
                            req.getParameter("to") != null ? req.getParameter("to") :
                            java.time.LocalDate.now().toString());
                    req.setAttribute("sales", saleDAO.getSalesByDateRange(from, to));
                    req.getRequestDispatcher("/jsp/billing/bill-list.jsp").forward(req, resp);
                } catch (Exception e) {
                    logger.severe(e.getMessage());
                    resp.sendError(500);
                }
            }
            case "/search-medicine" -> {
                // AJAX: returns JSON list of medicines
                String keyword = req.getParameter("q");
                try {
                    JsonUtil.writeJson(resp, medicineDAO.search(keyword));
                } catch (Exception e) {
                    JsonUtil.writeError(resp, 500, "Search failed");
                }
            }
            case "/batches" -> {
                // AJAX: FEFO batches for a medicine
                int medicineId = Integer.parseInt(req.getParameter("medicineId"));
                try {
                    List<MedicineBatch> batches = medicineDAO.getBatchesFEFO(medicineId);
                    JsonUtil.writeJson(resp, batches);
                } catch (Exception e) {
                    JsonUtil.writeError(resp, 500, "Batch fetch failed");
                }
            }
            default -> resp.sendError(404);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if (path == null) path = "/";
        User user = SessionUtil.getLoggedInUser(req);

        switch (path) {
            case "/create" -> {
                if (!SessionUtil.hasRole(req, "ADMIN", "STAFF")) {
                    JsonUtil.writeError(resp, 403, "Access denied");
                    return;
                }
                try {
                    int customerId  = Integer.parseInt(req.getParameter("customerId"));
                    String smParam  = req.getParameter("salesmanId");
                    int salesmanId  = (smParam != null && !smParam.trim().isEmpty()) 
                                      ? Integer.parseInt(smParam) : 0;

                    // Credit limit check first (FR-18)
                    Customer customer = customerDAO.findById(customerId);
                    if (customer.isCreditLimitExceeded()) {
                        JsonUtil.writeError(resp, 400, "CREDIT_LIMIT_EXCEEDED");
                        return;
                    }

                    // Parse items from JSON body
                    String itemsJson = req.getParameter("items");
                    Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                    List<Map<String, Object>> items = gson.fromJson(itemsJson, listType);

                    // Convert numeric types from Gson (doubles → int/BigDecimal)
                    for (Map<String, Object> item : items) {
                        item.put("medicineId", ((Double) item.get("medicineId")).intValue());
                        item.put("batchId",    ((Double) item.get("batchId")).intValue());
                        item.put("qty",        ((Double) item.get("qty")).intValue());
                        item.put("discount",   BigDecimal.valueOf((Double) item.getOrDefault("discount", 0.0)));
                    }

                    int saleId = saleDAO.createSale(customerId, salesmanId, items, user.getUserId());
                    JsonUtil.writeSuccess(resp, "Bill created", Map.of("saleId", saleId));

                } catch (Exception e) {
                    logger.severe("Create sale error: " + e.getMessage());
                    String msg = e.getMessage().contains("CREDIT_LIMIT") ?
                                 "Customer credit limit exceeded." : "Failed to create bill: " + e.getMessage();
                    JsonUtil.writeError(resp, 400, msg);
                }
            }
            case "/return" -> {
                if (!SessionUtil.hasRole(req, "ADMIN", "STAFF")) {
                    JsonUtil.writeError(resp, 403, "Access denied");
                    return;
                }
                try {
                    int saleId = Integer.parseInt(req.getParameter("saleId"));
                    saleDAO.processSaleReturn(saleId, user.getUserId());
                    JsonUtil.writeSuccess(resp, "Sale returned and stock restored.");
                } catch (Exception e) {
                    JsonUtil.writeError(resp, 500, "Return failed: " + e.getMessage());
                }
            }
            default -> resp.sendError(404);
        }
    }
}
