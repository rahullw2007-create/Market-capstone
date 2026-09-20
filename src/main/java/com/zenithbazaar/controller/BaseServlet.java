package com.zenithbazaar.controller;

import com.zenithbazaar.dto.ApiResponse;
import com.zenithbazaar.exception.AppException;
import com.zenithbazaar.utility.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {
    protected static final Logger logger = LoggerFactory.getLogger(BaseServlet.class);

    protected void sendSuccess(HttpServletResponse resp, Object data) throws IOException {
        sendJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(data));
    }

    protected void sendSuccess(HttpServletResponse resp, Object data, String message) throws IOException {
        sendJson(resp, HttpServletResponse.SC_OK, ApiResponse.ok(data, message));
    }

    protected void sendCreated(HttpServletResponse resp, Object data, String message) throws IOException {
        sendJson(resp, HttpServletResponse.SC_CREATED, ApiResponse.ok(data, message));
    }

    protected void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        sendJson(resp, statusCode, ApiResponse.error(message));
    }

    protected void sendJson(HttpServletResponse resp, int statusCode, Object body) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.getWriter().write(JsonUtil.toJson(body));
    }

    protected void handleError(HttpServletResponse resp, Exception e) throws IOException {
        if (e instanceof AppException appEx) {
            logger.warn("AppException occurred: {}", appEx.getMessage());
            sendError(resp, appEx.getStatusCode(), appEx.getMessage());
        } else {
            logger.error("Unhandled server exception", e);
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal server error occurred. Please try again later.");
        }
    }

    protected Long parseIdFromPath(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            return null;
        }
        String[] parts = pathInfo.split("/");
        if (parts.length >= 3) {
            try {
                return Long.parseLong(parts[2]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
