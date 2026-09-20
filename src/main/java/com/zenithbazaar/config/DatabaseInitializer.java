package com.zenithbazaar.config;

import com.zenithbazaar.security.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    public static synchronized void initializeDatabase() {
        logger.info("Initializing database schema and seed data...");
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            boolean tablesExist = false;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) = 'USERS' AND UPPER(TABLE_SCHEMA) = 'PUBLIC'")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    tablesExist = true;
                }
            } catch (Exception e) {
                logger.warn("Could not query INFORMATION_SCHEMA.TABLES: {}", e.getMessage());
            }

            if (!tablesExist) {
                logger.info("Executing schema.sql...");
                executeSqlScript(conn, "database/schema.sql");

                logger.info("Executing seed.sql...");
                executeSqlScript(conn, "database/seed.sql");

                // Update seed users password hash to guarantee BCrypt matches "Demo1234!"
                String demoHash = PasswordUtil.hashPassword("Demo1234!");
                try (Statement updateStmt = conn.createStatement()) {
                    updateStmt.executeUpdate("UPDATE users SET password_hash = '" + demoHash + "'");
                }
                logger.info("Database schema and seed data initialized successfully.");
            } else {
                logger.info("Database tables already exist in PUBLIC schema. Skipping schema initialization.");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize database schema", e);
            throw new RuntimeException("Database initialization failure", e);
        }
    }

    private static void executeSqlScript(Connection conn, String resourcePath) throws Exception {
        InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            is = DatabaseInitializer.class.getClassLoader().getResourceAsStream("/" + resourcePath);
        }
        if (is == null) {
            java.io.File file = new java.io.File(resourcePath);
            if (file.exists()) {
                is = new java.io.FileInputStream(file);
            } else {
                java.io.File rootFile = new java.io.File("src/main/resources/" + resourcePath);
                if (rootFile.exists()) {
                    is = new java.io.FileInputStream(rootFile);
                } else {
                    logger.error("Script resource not found: {}", resourcePath);
                    throw new IllegalArgumentException("Script resource not found: " + resourcePath);
                }
            }
        }

        String rawSql;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            rawSql = reader.lines()
                    .map(line -> {
                        int commentIdx = line.indexOf("--");
                        if (commentIdx >= 0) {
                            return line.substring(0, commentIdx);
                        }
                        return line;
                    })
                    .collect(Collectors.joining("\n"));
        }

        String[] statements = rawSql.split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }
}
