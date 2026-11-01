package hotel.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application configuration provider.
 * Loads configuration with precedence:
 * 1. Environment variables
 * 2. .env file in workspace
 * 3. db.properties file
 * 4. Built-in sensible defaults
 */
public final class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);
    private static final Properties properties = new Properties();

    // Default configuration values
    private static final String DEFAULT_DB_HOST = "localhost";
    private static final String DEFAULT_DB_PORT = "3306";
    private static final String DEFAULT_DB_NAME = "hotel_management_test";
    private static final String DEFAULT_DB_USER = "root";
    private static final String DEFAULT_DB_PASSWORD = "";

    private static final String DEFAULT_HOTEL_NAME = "GrandStay Hotel & Luxury Suites";
    private static final String DEFAULT_TAX_RATE = "0.10";
    private static final String DEFAULT_SERVICE_FEE = "15.00";

    static {
        loadConfiguration();
    }

    private AppConfig() {
        // Prevent instantiation
    }

    private static void loadConfiguration() {
        // Step 1: Load db.properties if exists
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (InputStream input = new FileInputStream(propFile)) {
                properties.load(input);
                logger.info("Loaded configuration from db.properties");
            } catch (IOException e) {
                logger.warn("Could not read db.properties: {}", e.getMessage());
            }
        }

        // Step 2: Load .env if exists
        Path envPath = Paths.get(".env");
        if (Files.exists(envPath)) {
            try {
                List<String> lines = Files.readAllLines(envPath);
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("#") && trimmed.contains("=")) {
                        int eqIdx = trimmed.indexOf('=');
                        String key = trimmed.substring(0, eqIdx).trim();
                        String value = trimmed.substring(eqIdx + 1).trim();
                        properties.setProperty(key, value);
                    }
                }
                logger.info("Loaded configuration from .env");
            } catch (IOException e) {
                logger.warn("Could not read .env: {}", e.getMessage());
            }
        }
    }

    public static String get(String key, String defaultValue) {
        // Check system env first
        String envVal = System.getenv(key);
        if (envVal != null && !envVal.trim().isEmpty()) {
            return envVal.trim();
        }
        // Then loaded properties
        String propVal = properties.getProperty(key);
        if (propVal != null && !propVal.trim().isEmpty()) {
            return propVal.trim();
        }
        return defaultValue;
    }

    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public static void saveToFile(String host, String port, String dbName, String user, String password) {
        properties.setProperty("DB_HOST", host);
        properties.setProperty("DB_PORT", port);
        properties.setProperty("DB_NAME", dbName);
        properties.setProperty("DB_USER", user);
        properties.setProperty("DB_PASSWORD", password);

        try (FileOutputStream out = new FileOutputStream("db.properties")) {
            properties.store(out, "GrandStay Hotel Management Database Configuration");
            logger.info("Saved database configuration to db.properties");
        } catch (IOException e) {
            logger.error("Failed to save db.properties: {}", e.getMessage());
        }
    }

    public static String getHotelName() {
        return get("HOTEL_NAME", DEFAULT_HOTEL_NAME);
    }

    public static BigDecimal getTaxRate() {
        try {
            return new BigDecimal(get("HOTEL_TAX_RATE", DEFAULT_TAX_RATE));
        } catch (Exception e) {
            return new BigDecimal(DEFAULT_TAX_RATE);
        }
    }

    public static BigDecimal getServiceFee() {
        try {
            return new BigDecimal(get("HOTEL_SERVICE_FEE", DEFAULT_SERVICE_FEE));
        } catch (Exception e) {
            return new BigDecimal(DEFAULT_SERVICE_FEE);
        }
    }

    public static String getDbHost() {
    return get("DB_HOST", DEFAULT_DB_HOST);
}

public static String getDbPort() {
    return get("DB_PORT", DEFAULT_DB_PORT);
}

public static String getDbName() {
    return get("DB_NAME", DEFAULT_DB_NAME);
}

public static String getDbUser() {
    return get("DB_USER", DEFAULT_DB_USER);
}

public static String getDbPassword() {
    return get("DB_PASSWORD", DEFAULT_DB_PASSWORD);
}

public static String getJdbcUrl() {
    return String.format(
        "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
        getDbHost(), getDbPort(), getDbName()
    );
}

public static String getJdbcServerUrl() {
    return String.format(
        "jdbc:mysql://%s:%s/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
        getDbHost(), getDbPort()
    );
    }
}
