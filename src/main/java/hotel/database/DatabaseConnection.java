package hotel.database;

import hotel.config.AppConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages JDBC connections to the MySQL database.
 * Supports connection testing, validation, and transaction isolation.
 */
public final class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("MySQL JDBC Driver not found on classpath: {}", e.getMessage());
        }
    }

    private DatabaseConnection() {
        // Prevent instantiation
    }

    /**
     * Gets a connection to the hotel_management database.
     *
     * @return active java.sql.Connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        String url = AppConfig.getJdbcUrl();
        String user = AppConfig.getDbUser();
        String password = AppConfig.getDbPassword();
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Gets a connection to the MySQL server (without specifying database name).
     * Useful for creating the database if it doesn't exist yet.
     *
     * @return active java.sql.Connection to MySQL server
     * @throws SQLException if connection fails
     */
    public static Connection getServerConnection() throws SQLException {
        String url = AppConfig.getJdbcServerUrl();
        String user = AppConfig.getDbUser();
        String password = AppConfig.getDbPassword();
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Tests connectivity to the database.
     *
     * @return true if database is reachable, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.debug("Database connection check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Tests connectivity to the MySQL server.
     *
     * @return true if MySQL server is reachable, false otherwise
     */
    public static boolean testServerConnection() {
        try (Connection conn = getServerConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.debug("MySQL Server connection check failed: {}", e.getMessage());
            return false;
        }
    }
}
