package com.zenithbazaar.controller;

import com.zenithbazaar.config.DatabaseConfig;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/system/health")
public class SystemHealthServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Map<String, String> healthStatus = new HashMap<>();
        healthStatus.put("status", "UP");

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            boolean dbOk = stmt.execute("SELECT 1");
            if (dbOk) {
                healthStatus.put("database", "UP");
                sendSuccess(resp, healthStatus);
            } else {
                healthStatus.put("database", "DOWN");
                sendError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Database connection failed");
            }
        } catch (Exception e) {
            logger.error("Health check database failure", e);
            healthStatus.put("database", "DOWN");
            healthStatus.put("error", e.getMessage());
            sendJson(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE, healthStatus);
        }
    }
}
