package com.smartmedical.servlet.payment;

import com.smartmedical.dao.CustomerDAO;
import com.smartmedical.dao.PaymentDAO;
import com.smartmedical.dao.SupplierDAO;
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
import java.util.logging.Logger;

/** URL: /payments/* */
@WebServlet("/payments/*")
public class PaymentServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(PaymentServlet.class.getName());
    private final PaymentDAO  paymentDAO  = new PaymentDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if (path == null) path = "/";

        try {
            switch (path) {
                case "/", "/receipt" -> {
                    req.setAttribute("customers", customerDAO.findAll());
                    req.getRequestDispatcher("/jsp/payment/receipt.jsp").forward(req, resp);
                }
                case "/supplier-payment" -> {
                    req.setAttribute("suppliers", supplierDAO.findAll());
                    req.getRequestDispatcher("/jsp/payment/supplier-payment.jsp").forward(req, resp);
                }
                case "/outstanding" -> {
                    req.setAttribute("aging", paymentDAO.getCustomerOutstandingAging());
                    req.getRequestDispatcher("/jsp/payment/outstanding.jsp").forward(req, resp);
                }
                case "/ledger" -> {
                    int customerId = Integer.parseInt(req.getParameter("customerId"));
                    req.setAttribute("customer", customerDAO.findById(customerId));
                    req.setAttribute("ledger", paymentDAO.getCustomerLedger(customerId));
                    req.getRequestDispatcher("/jsp/payment/ledger.jsp").forward(req, resp);
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
                case "/receipt" -> {
                    int customerId     = Integer.parseInt(req.getParameter("customerId"));
                    BigDecimal amount  = new BigDecimal(req.getParameter("amount"));
                    String mode        = req.getParameter("paymentMode");
                    String refNo       = req.getParameter("referenceNo");
                    String saleIdStr   = req.getParameter("saleId");
                    Integer saleId     = (saleIdStr != null && !saleIdStr.isBlank())
                                         ? Integer.parseInt(saleIdStr) : null;

                    int paymentId = paymentDAO.recordCustomerReceipt(
                            customerId, amount, mode, refNo, saleId, user.getUserId());
                    JsonUtil.writeSuccess(resp, "Receipt recorded", java.util.Map.of("paymentId", paymentId));
                }
                case "/supplier-payment" -> {
                    int supplierId       = Integer.parseInt(req.getParameter("supplierId"));
                    BigDecimal amount    = new BigDecimal(req.getParameter("amount"));
                    String mode          = req.getParameter("paymentMode");
                    String refNo         = req.getParameter("referenceNo");
                    String purchIdStr    = req.getParameter("purchaseId");
                    Integer purchaseId   = (purchIdStr != null && !purchIdStr.isBlank())
                                           ? Integer.parseInt(purchIdStr) : null;

                    int paymentId = paymentDAO.recordSupplierPayment(
                            supplierId, amount, mode, refNo, purchaseId, user.getUserId());
                    JsonUtil.writeSuccess(resp, "Payment recorded", java.util.Map.of("paymentId", paymentId));
                }
                default -> resp.sendError(404);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }
}
