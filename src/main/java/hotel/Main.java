package hotel;

import hotel.config.AppConfig;
import hotel.database.DatabaseInitializer;
import hotel.ui.auth.LoginFrame;
import hotel.ui.common.UITheme;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application entry point for GrandStay Hotel Management System.
 */
public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting {}...", AppConfig.getHotelName());

        // Step 1: Initialize modern Look and Feel
        UITheme.setupLookAndFeel();

        // Step 2: Initialize Database and Schema in background
        new Thread(() -> {
            try {
                boolean ready = DatabaseInitializer.initialize();
                if (ready) {
                    logger.info("Database is initialized and ready.");
                } else {
                    logger.warn("Database initialization deferred. Users may configure connection in Login screen settings.");
                }
            } catch (Exception e) {
                logger.error("Database startup check: {}", e.getMessage());
            }
        }, "DatabaseInitializerThread").start();

        // Step 3: Launch Graphical User Interface on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            logger.info("Login window presented to user.");
        });
    }
}
