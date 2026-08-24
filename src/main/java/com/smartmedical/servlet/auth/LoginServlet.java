package com.smartmedical.servlet.auth;

import com.smartmedical.dao.UserDAO;
import com.smartmedical.model.User;
import com.smartmedical.util.PasswordUtil;
import com.smartmedical.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * LoginServlet — handles GET (show form) and POST (authenticate)
 * URL: /login
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(LoginServlet.class.getName());
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // If already logged in, redirect to dashboard
        if (SessionUtil.isLoggedIn(req)) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        req.getRequestDispatcher("/jsp/auth/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            User user = userDAO.findByUsername(username);
            if (user != null && PasswordUtil.verify(password, user.getPasswordHash())) {
                // Create session
                HttpSession session = req.getSession(true);
                session.setAttribute(SessionUtil.SESSION_USER, user);
                session.setMaxInactiveInterval(30 * 60); // 30 minutes (BRD FR-03)

                // Update last login (BRD FR-05)
                userDAO.updateLastLogin(user.getUserId());

                logger.info("Login: " + username + " | IP: " + req.getRemoteAddr());
                resp.sendRedirect(req.getContextPath() + "/dashboard");
            } else {
                req.setAttribute("error", "Invalid username or password.");
                req.getRequestDispatcher("/jsp/auth/login.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            logger.severe("Login error: " + e.getMessage());
            req.setAttribute("error", "System error. Please try again.");
            req.getRequestDispatcher("/jsp/auth/login.jsp").forward(req, resp);
        }
    }
}
