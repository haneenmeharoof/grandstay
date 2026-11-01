package hotel.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for secure password hashing and verification using BCrypt.
 */
public final class PasswordUtil {

    private static final int BCRYPT_LOG_ROUNDS = 12;

    private PasswordUtil() {
        // Prevent instantiation
    }

    /**
     * Hashes a raw password using BCrypt with 12 log rounds.
     *
     * @param plainPassword Raw password string
     * @return 60-character BCrypt hash string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
    }

    /**
     * Verifies whether a plaintext password matches the BCrypt hash.
     *
     * @param plainPassword Raw password candidate
     * @param hashedPassword Hashed password from database
     * @return true if candidate matches hash, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
