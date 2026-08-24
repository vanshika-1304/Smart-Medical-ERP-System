package com.smartmedical.servlet.inventory;

import com.smartmedical.dao.MedicineDAO;
import com.smartmedical.model.Medicine;
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
import java.math.BigDecimal;
import java.sql.Date;
import java.util.logging.Logger;

/**
 * InventoryServlet — URL: /inventory/*
 *   GET  /inventory          → Medicine list with alerts
 *   GET  /inventory/expiry   → Expiry alert report
 *   GET  /inventory/low-stock → Low stock report
 *   POST /inventory/add-medicine → Add new medicine
 *   POST /inventory/add-batch    → Add batch to existing medicine
 */
@WebServlet("/inventory/*")
public class InventoryServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(InventoryServlet.class.getName());
    private final MedicineDAO medicineDAO = new MedicineDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if (path == null) path = "/";

        try {
            switch (path) {
                case "/", "/list" -> {
                    req.setAttribute("medicines", medicineDAO.findAll());
                    req.getRequestDispatcher("/jsp/inventory/medicine-list.jsp").forward(req, resp);
                }
                case "/expiry" -> {
                    int days = req.getParameter("days") != null
                               ? Integer.parseInt(req.getParameter("days")) : 30;
                    req.setAttribute("expiryBatches", medicineDAO.getExpiryAlerts(days));
                    req.setAttribute("days", days);
                    req.getRequestDispatcher("/jsp/inventory/expiry-alert.jsp").forward(req, resp);
                }
                case "/low-stock" -> {
                    req.setAttribute("lowStockBatches", medicineDAO.getLowStockAlerts());
                    req.getRequestDispatcher("/jsp/inventory/low-stock.jsp").forward(req, resp);
                }
                case "/batches" -> {
                    int medicineId = Integer.parseInt(req.getParameter("id"));
                    JsonUtil.writeJson(resp, medicineDAO.getAllBatches(medicineId));
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
                case "/add-medicine" -> {
                    Medicine m = new Medicine();
                    m.setName(req.getParameter("name"));
                    m.setCategory(req.getParameter("category"));
                    m.setHsnCode(req.getParameter("hsnCode"));
                    m.setComposition(req.getParameter("composition"));
                    m.setCompany(req.getParameter("company"));
                    m.setGstPct(new BigDecimal(req.getParameter("gstPct")));
                    m.setRackLocation(req.getParameter("rackLocation"));
                    int id = medicineDAO.create(m, user.getUserId());
                    JsonUtil.writeSuccess(resp, "Medicine added", java.util.Map.of("medicineId", id));
                }
                case "/add-batch" -> {
                    MedicineBatch b = new MedicineBatch();
                    b.setMedicineId(Integer.parseInt(req.getParameter("medicineId")));
                    b.setBatchNo(req.getParameter("batchNo"));
                    b.setExpiryDate(Date.valueOf(req.getParameter("expiryDate")));
                    b.setMrp(new BigDecimal(req.getParameter("mrp")));
                    b.setPurchaseRate(new BigDecimal(req.getParameter("purchaseRate")));
                    b.setStockQty(Integer.parseInt(req.getParameter("stockQty")));
                    b.setMinStockAlert(req.getParameter("minStock") != null
                                      ? Integer.parseInt(req.getParameter("minStock")) : 10);
                    int id = medicineDAO.createBatch(b, user.getUserId());
                    JsonUtil.writeSuccess(resp, "Batch added", java.util.Map.of("batchId", id));
                }
                case "/update" -> {
                    Medicine m = new Medicine();
                    m.setMedicineId(Integer.parseInt(req.getParameter("medicineId")));
                    m.setName(req.getParameter("name"));
                    m.setCategory(req.getParameter("category"));
                    m.setHsnCode(req.getParameter("hsnCode"));
                    m.setComposition(req.getParameter("composition"));
                    m.setCompany(req.getParameter("company"));
                    m.setGstPct(new BigDecimal(req.getParameter("gstPct")));
                    m.setRackLocation(req.getParameter("rackLocation"));
                    medicineDAO.update(m);
                    JsonUtil.writeSuccess(resp, "Medicine updated");
                }
                case "/delete" -> {
                    medicineDAO.deactivate(Integer.parseInt(req.getParameter("medicineId")));
                    JsonUtil.writeSuccess(resp, "Medicine deactivated");
                }
                default -> resp.sendError(404);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }
}
