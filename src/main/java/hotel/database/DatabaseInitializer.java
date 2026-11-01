package hotel.database;

import hotel.config.AppConfig;
import hotel.util.PasswordUtil;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automatically initializes and verifies database structure and seed data.
 */
public final class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private DatabaseInitializer() {
    }

    /**
     * Initializes database if not yet initialized.
     * Ensures database, schema, and base admin/staff/customer accounts exist.
     *
     * @return true if database is ready to use, false otherwise
     */
    public static boolean initialize() {
        try {
            // Check if server is reachable
            if (!DatabaseConnection.testServerConnection()) {
                logger.warn("MySQL Server is currently unreachable at {}:{}",
                        AppConfig.getDbHost(), AppConfig.getDbPort());
                return false;
            }

            // Ensure database exists
            createDatabaseIfNotExists();

            // Check if tables exist
            if (!tablesExist()) {
                logger.info("Database tables not found. Executing schema.sql...");
                executeSqlScript(findScriptPath("database/schema.sql"));
                logger.info("Executing seed.sql...");
                executeSqlScript(findScriptPath("database/seed.sql"));
            }

            // Ensure default accounts exist with valid BCrypt hashes
            ensureDefaultAccounts();

            logger.info("Database initialization completed successfully.");
            return true;
        } catch (Exception e) {
            logger.error("Database initialization failed: {}", e.getMessage(), e);
            return false;
        }
    }

    private static void createDatabaseIfNotExists() throws SQLException {
        try (Connection conn = DatabaseConnection.getServerConnection();
             Statement stmt = conn.createStatement()) {
            String dbName = AppConfig.getDbName();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName + "` " +
                    "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            logger.info("Database '{}' verified/created.", dbName);
        }
    }

    private static boolean tablesExist() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(AppConfig.getDbName(), null, "users", new String[]{"TABLE"})) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private static Path findScriptPath(String relativePath) {
        Path p = Paths.get(relativePath);
        if (Files.exists(p)) {
            return p;
        }
        Path parentP = Paths.get("..", relativePath);
        if (Files.exists(parentP)) {
            return parentP;
        }
        return p;
    }

    public static void executeSqlScript(Path scriptPath) throws Exception {
        if (!Files.exists(scriptPath)) {
            logger.warn("SQL script not found at {}", scriptPath.toAbsolutePath());
            return;
        }

        List<String> statements = parseSqlStatements(Files.readAllLines(scriptPath, StandardCharsets.UTF_8));
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        stmt.execute(trimmed);
                    } catch (SQLException e) {
                        logger.debug("SQL statement note: {} -> {}", e.getMessage(), trimmed);
                    }
                }
            }
        }
    }

    private static List<String> parseSqlStatements(List<String> lines) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--") || trimmed.startsWith("/*") || trimmed.isEmpty()) {
                continue;
            }
            current.append(line).append("\n");
            if (trimmed.endsWith(";")) {
                statements.add(current.toString().substring(0, current.toString().lastIndexOf(';')));
                current.setLength(0);
            }
        }
        if (current.length() > 0 && !current.toString().trim().isEmpty()) {
            statements.add(current.toString());
        }
        return statements;
    }

    /**
     * Ensures default users exist with fresh, guaranteed BCrypt hashes.
     */
    private static void ensureDefaultAccounts() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            createOrUpdateUser(conn, "admin", "admin@grandstay.com", "Admin@123", "System Administrator", "+1-555-0100", "ADMIN");
            createOrUpdateUser(conn, "receptionist", "reception@grandstay.com", "Reception@123", "Emily Davis (Front Desk)", "+1-555-0101", "RECEPTIONIST");
            createOrUpdateUser(conn, "housekeeper", "housekeeper@grandstay.com", "Housekeeping@123", "Carlos Mendez (Housekeeping)", "+1-555-0102", "HOUSEKEEPING");
            createOrUpdateUser(conn, "johndoe", "john.doe@example.com", "Customer@123", "John Doe", "+1-555-0199", "CUSTOMER");
        } catch (SQLException e) {
            logger.error("Failed to verify default accounts: {}", e.getMessage());
        }
    }

    private static void createOrUpdateUser(Connection conn, String username, String email, String password,
                                           String fullName, String phone, String role) throws SQLException {
        String selectSql = "SELECT id FROM users WHERE username = ? OR email = ?";
        try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    // Insert new user
                    String insertSql = "INSERT INTO users (username, email, password_hash, full_name, phone, role, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE')";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setString(1, username);
                        insertPs.setString(2, email);
                        insertPs.setString(3, PasswordUtil.hashPassword(password));
                        insertPs.setString(4, fullName);
                        insertPs.setString(5, phone);
                        insertPs.setString(6, role);
                        insertPs.executeUpdate();
                        logger.info("Created default user '{}' with role {}", username, role);
                    }
                }
            }
        }
    }
}
