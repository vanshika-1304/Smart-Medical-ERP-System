package com.smartmedical.util;

import com.smartmedical.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Session management helper.
 * Sessions expire after 30 minutes (set in web.xml).
 */
public class SessionUtil {

    public static final String SESSION_USER = "loggedInUser";

    private SessionUtil() {}

    public static User getLoggedInUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        return (User) session.getAttribute(SESSION_USER);
    }

    public static boolean isLoggedIn(HttpServletRequest request) {
        return getLoggedInUser(request) != null;
    }

    public static boolean hasRole(HttpServletRequest request, String... roles) {
        User user = getLoggedInUser(request);
        if (user == null) return false;
        for (String role : roles) {
            if (user.getRole().equalsIgnoreCase(role)) return true;
        }
        return false;
    }

    public static void invalidate(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
    }
}
