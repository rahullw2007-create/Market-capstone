package com.zenithbazaar.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;

    private DatabaseConfig() {}

    public static synchronized void initialize() {
        if (dataSource == null || dataSource.isClosed()) {
            try {
                HikariConfig config = new HikariConfig();
                config.setDriverClassName(AppConfig.get("db.driver", "org.h2.Driver"));
                config.setJdbcUrl(AppConfig.get("db.url", "jdbc:h2:file:./zenithbazaar_db;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1"));
                config.setUsername(AppConfig.get("db.user", "sa"));
                config.setPassword(AppConfig.get("db.password", ""));
                config.setMaximumPoolSize(AppConfig.getInt("db.pool.maximumPoolSize", 10));
                config.setMinimumIdle(AppConfig.getInt("db.pool.minimumIdle", 2));
                config.setIdleTimeout(AppConfig.getInt("db.pool.idleTimeout", 30000));
                config.setConnectionTimeout(AppConfig.getInt("db.pool.connectionTimeout", 10000));

                dataSource = new HikariDataSource(config);
                logger.info("HikariCP DataSource initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize HikariCP DataSource", e);
                throw new RuntimeException("Database initialization error", e);
            }
        }
    }

    public static synchronized void initializeWithDataSource(HikariDataSource ds) {
        dataSource = ds;
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            initialize();
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP DataSource closed");
        }
    }
}
