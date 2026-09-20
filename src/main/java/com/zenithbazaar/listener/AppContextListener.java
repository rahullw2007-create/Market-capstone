package com.zenithbazaar.listener;

import com.zenithbazaar.config.DatabaseConfig;
import com.zenithbazaar.config.DatabaseInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing ZenithBazaar Application Context...");
        try {
            DatabaseConfig.initialize();
            DatabaseInitializer.initializeDatabase();
            logger.info("ZenithBazaar Application Context initialized successfully.");
        } catch (Exception e) {
            logger.error("Error initializing ZenithBazaar Application Context", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Destroying ZenithBazaar Application Context...");
        DatabaseConfig.shutdown();
        logger.info("ZenithBazaar Application Context destroyed.");
    }
}
