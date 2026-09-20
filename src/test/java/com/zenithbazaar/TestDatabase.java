package com.zenithbazaar;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zenithbazaar.config.DatabaseConfig;
import com.zenithbazaar.config.DatabaseInitializer;

import java.sql.Connection;
import java.sql.Statement;

public class TestDatabase {
    private static boolean initialized = false;

    public static synchronized void setupInMemoryDatabase() {
        if (!initialized) {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.h2.Driver");
            config.setJdbcUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=REGULAR");
            config.setUsername("sa");
            config.setPassword("");
            config.setMaximumPoolSize(5);

            HikariDataSource ds = new HikariDataSource(config);
            DatabaseConfig.initializeWithDataSource(ds);

            try (Connection conn = DatabaseConfig.getConnection();
                 Statement stmt = conn.createStatement()) {
                // Initialize database schema and seed data
                DatabaseInitializer.initializeDatabase();
            } catch (Exception e) {
                throw new RuntimeException("Failed to set up in-memory test database", e);
            }
            initialized = true;
        }
    }
}
