package com.smartmedical.servlet.supplier;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.smartmedical.dao.PurchaseDAO;
import com.smartmedical.dao.SupplierDAO;
import com.smartmedical.model.Supplier;
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
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/** URL: /suppliers/* */
@WebServlet("/suppliers/*")
public class SupplierServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(SupplierServlet.class.getName());
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final PurchaseDAO purchaseDAO = new PurchaseDAO();
    private final Gson        gson        = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if (path == null) path = "/";

        try {
            switch (path) {
                case "/", "/list" -> {
                    req.setAttribute("suppliers", supplierDAO.findAll());
                    req.getRequestDispatcher("/jsp/supplier/supplier-list.jsp").forward(req, resp);
                }
                case "/purchase/new" -> {
                    req.setAttribute("suppliers", supplierDAO.findAll());
                    req.getRequestDispatcher("/jsp/supplier/new-purchase.jsp").forward(req, resp);
                }
                case "/purchase/history" -> {
                    int supplierId = Integer.parseInt(req.getParameter("id"));
                    String from = req.getParameter("from") != null ? req.getParameter("from") :
                                  java.time.LocalDate.now().withDayOfMonth(1).toString();
                    String to   = req.getParameter("to") != null ? req.getParameter("to") :
                                  java.time.LocalDate.now().toString();
                    req.setAttribute("supplier", supplierDAO.findById(supplierId));
                    req.setAttribute("purchases", purchaseDAO.getPurchaseHistory(
                            supplierId, Date.valueOf(from), Date.valueOf(to)));
                    req.getRequestDispatcher("/jsp/supplier/purchase-history.jsp").forward(req, resp);
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

        if (!SessionUtil.hasRole(req, "ADMIN", "STAFF")) {
            JsonUtil.writeError(resp, 403, "Access denied");
            return;
        }
        User user = SessionUtil.getLoggedInUser(req);
        String path = req.getPathInfo();
        if (path == null) path = "/";

        try {
            switch (path) {
                case "/add" -> {
                    Supplier s = buildSupplier(req);
                    int id = supplierDAO.create(s, user.getUserId());
                    JsonUtil.writeSuccess(resp, "Supplier added", Map.of("supplierId", id));
                }
                case "/update" -> {
                    Supplier s = buildSupplier(req);
                    s.setSupplierId(Integer.parseInt(req.getParameter("supplierId")));
                    supplierDAO.update(s);
                    JsonUtil.writeSuccess(resp, "Supplier updated");
                }
                case "/delete" -> {
                    supplierDAO.deactivate(Integer.parseInt(req.getParameter("supplierId")));
                    JsonUtil.writeSuccess(resp, "Supplier deactivated");
                }
                case "/purchase/create" -> {
                    int supplierId   = Integer.parseInt(req.getParameter("supplierId"));
                    String invoiceNo = req.getParameter("invoiceNo");
                    String itemsJson = req.getParameter("items");

                    Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                    List<Map<String, Object>> items = gson.fromJson(itemsJson, listType);

                    // Convert Gson types
                    for (Map<String, Object> item : items) {
                        item.put("medicineId",  ((Double) item.get("medicineId")).intValue());
                        item.put("qty",         ((Double) item.get("qty")).intValue());
                        item.put("rate",        BigDecimal.valueOf((Double) item.get("rate")));
                        item.put("mrp",         BigDecimal.valueOf((Double) item.get("mrp")));
                        item.put("gstPct",      BigDecimal.valueOf((Double) item.getOrDefault("gstPct", 0.0)));
                        item.put("expiryDate",  Date.valueOf((String) item.get("expiryDate")));
                        item.put("minStock",    item.containsKey("minStock")
                                                ? ((Double) item.get("minStock")).intValue() : 10);
                    }

                    int purchaseId = purchaseDAO.createPurchase(supplierId, invoiceNo, items, user.getUserId());
                    JsonUtil.writeSuccess(resp, "Purchase recorded", Map.of("purchaseId", purchaseId));
                }
                default -> resp.sendError(404);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }

    private Supplier buildSupplier(HttpServletRequest req) {
        Supplier s = new Supplier();
        s.setName(req.getParameter("name"));
        s.setContactNo(req.getParameter("contactNo"));
        s.setGstin(req.getParameter("gstin"));
        s.setAddress(req.getParameter("address"));
        String cl = req.getParameter("creditLimit");
        s.setCreditLimit(cl != null && !cl.isBlank() ? new BigDecimal(cl) : BigDecimal.ZERO);
        return s;
    }
}
