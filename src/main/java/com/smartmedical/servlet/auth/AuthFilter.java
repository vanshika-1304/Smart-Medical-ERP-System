package com.smartmedical.servlet.auth;

import com.smartmedical.util.SessionUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

/**
 * AuthFilter — intercepts every request.
 * Allows login page and static resources; redirects all others to login if not authenticated.
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

	private static final Set<String> PUBLIC_PATHS = Set.of(
		    "/login", "/login.jsp", "/assets/", "/css/", "/js/", "/images/"
		);

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getServletPath();

        boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        if (isPublic) {
            chain.doFilter(req, res);
            return;
        }

        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        chain.doFilter(req, res);
    }
}
