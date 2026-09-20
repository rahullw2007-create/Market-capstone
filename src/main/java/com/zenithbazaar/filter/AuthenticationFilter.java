package com.zenithbazaar.filter;

import com.zenithbazaar.dto.ApiResponse;
import com.zenithbazaar.dto.UserDto;
import com.zenithbazaar.model.Role;
import com.zenithbazaar.security.SessionUtil;
import com.zenithbazaar.utility.JsonUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/api/*")
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());

        // Public endpoints
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        UserDto user = SessionUtil.getCurrentUser(req);
        if (user == null) {
            sendJsonError(res, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required. Please log in.");
            return;
        }

        // Role Authorization check
        if (path.startsWith("/api/vendor") && user.getRole() != Role.VENDOR) {
            sendJsonError(res, HttpServletResponse.SC_FORBIDDEN, "Access denied: Vendor role required.");
            return;
        }

        if (path.startsWith("/api/administrator") && user.getRole() != Role.ADMINISTRATOR) {
            sendJsonError(res, HttpServletResponse.SC_FORBIDDEN, "Access denied: Administrator role required.");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        if (path.startsWith("/api/account/login") ||
            path.startsWith("/api/account/register") ||
            path.startsWith("/api/account/logout") ||
            path.startsWith("/api/catalog") ||
            path.startsWith("/api/system/health") ||
            path.startsWith("/api/chat")) {
            return true;
        }
        // Public review reads (GET)
        if (path.startsWith("/api/reviews/product")) {
            return true;
        }
        return false;
    }

    private void sendJsonError(HttpServletResponse res, int statusCode, String message) throws IOException {
        res.setStatus(statusCode);
        res.setContentType("application/json");
        res.getWriter().write(JsonUtil.toJson(ApiResponse.error(message)));
    }

    @Override
    public void destroy() {}
}
