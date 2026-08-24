package com.smartmedical.servlet.auth;

import com.smartmedical.dao.UserDAO;
import com.smartmedical.model.User;
import com.smartmedical.util.JsonUtil;
import com.smartmedical.util.PasswordUtil;
import com.smartmedical.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * UserManagementServlet — URL: /users/*
 * Only ADMIN role can access (FR-04)
 */
@WebServlet("/users/*")
public class UserManagementServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(UserManagementServlet.class.getName());
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!SessionUtil.hasRole(req, "ADMIN")) {
            resp.sendError(403, "Access denied — Admin only");
            return;
        }

        String path = req.getPathInfo();
        if (path == null) path = "/";

        try {
            switch (path) {
                case "/", "/list" -> {
                    req.setAttribute("users", userDAO.findAll());
                    req.getRequestDispatcher("/jsp/auth/user-list.jsp").forward(req, resp);
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

        String path = req.getPathInfo();
        if (path == null) path = "/";

        try {
            switch (path) {
                case "/add" -> {
                    String username = req.getParameter("username");
                    String password = req.getParameter("password");
                    String role     = req.getParameter("role");

                    // Validate role
                    if (!role.matches("ADMIN|STAFF|SALESMAN|OWNER")) {
                        JsonUtil.writeError(resp, 400, "Invalid role");
                        return;
                    }

                    User u = new User();
                    u.setUsername(username);
                    u.setPasswordHash(PasswordUtil.hash(password));
                    u.setRole(role);

                    int id = userDAO.create(u);
                    JsonUtil.writeSuccess(resp, "User created", Map.of("userId", id));
                }
                case "/reset-password" -> {
                    int userId      = Integer.parseInt(req.getParameter("userId"));
                    String newPass  = req.getParameter("newPassword");
                    userDAO.updatePassword(userId, PasswordUtil.hash(newPass));
                    JsonUtil.writeSuccess(resp, "Password reset successfully");
                }
                case "/deactivate" -> {
                    int userId = Integer.parseInt(req.getParameter("userId"));
                    // Prevent deactivating yourself
                    User loggedIn = SessionUtil.getLoggedInUser(req);
                    if (loggedIn.getUserId() == userId) {
                        JsonUtil.writeError(resp, 400, "Cannot deactivate your own account");
                        return;
                    }
                    userDAO.deactivate(userId);
                    JsonUtil.writeSuccess(resp, "User deactivated");
                }
                default -> resp.sendError(404);
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
            JsonUtil.writeError(resp, 500, e.getMessage());
        }
    }
}
