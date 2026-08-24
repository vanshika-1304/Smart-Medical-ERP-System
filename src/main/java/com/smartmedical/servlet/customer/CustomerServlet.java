package com.smartmedical.servlet.customer;

import com.smartmedical.dao.CustomerDAO;
import com.smartmedical.dao.PaymentDAO;
import com.smartmedical.model.Customer;
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

/** URL: /customers/* */
@WebServlet("/customers/*")
public class CustomerServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(CustomerServlet.class.getName());
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final PaymentDAO  paymentDAO  = new PaymentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getPathInfo();
        if (path == null) path = "/";

        try {
            switch (path) {
                case "/", "/list" -> {
                    req.setAttribute("customers", customerDAO.findAll());
                    req.getRequestDispatcher("/jsp/customer/customer-list.jsp").forward(req, resp);
                }
                case "/ledger" -> {
                    int customerId = Integer.parseInt(req.getParameter("id"));
                    req.setAttribute("customer", customerDAO.findById(customerId));
                    req.setAttribute("ledger", paymentDAO.getCustomerLedger(customerId));
                    req.getRequestDispatcher("/jsp/customer/ledger.jsp").forward(req, resp);
                }
                case "/outstanding" -> {
                    req.setAttribute("aging", paymentDAO.getCustomerOutstandingAging());
                    req.getRequestDispatcher("/jsp/customer/outstanding.jsp").forward(req, resp);
                }
                case "/search" -> {
                    String q = req.getParameter("q");
                    JsonUtil.writeJson(resp, customerDAO.search(q));
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
                    Customer c = buildCustomer(req);
                    int id = customerDAO.create(c, user.getUserId());
                    JsonUtil.writeSuccess(resp, "Customer added", java.util.Map.of("customerId", id));
                }
                case "/update" -> {
                    Customer c = buildCustomer(req);
                    c.setCustomerId(Integer.parseInt(req.getParameter("customerId")));
                    customerDAO.update(c);
                    JsonUtil.writeSuccess(resp, "Customer updated");
                }
                case "/delete" -> {
                    customerDAO.deactivate(Integer.parseInt(req.getParameter("customerId")));
                    JsonUtil.writeSuccess(resp, "Customer deactivated");
                }
                default -> resp.sendError(404);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }

    private Customer buildCustomer(HttpServletRequest req) {
        Customer c = new Customer();
        c.setShopName(req.getParameter("shopName"));
        c.setOwnerName(req.getParameter("ownerName"));
        c.setPhone(req.getParameter("phone"));
        c.setGstin(req.getParameter("gstin"));
        c.setAddress(req.getParameter("address"));
        String routeId = req.getParameter("routeId");
        c.setRouteId(routeId != null && !routeId.isBlank() ? Integer.parseInt(routeId) : 0);
        String cl = req.getParameter("creditLimit");
        c.setCreditLimit(cl != null && !cl.isBlank() ? new BigDecimal(cl) : BigDecimal.ZERO);
        return c;
    }
}
