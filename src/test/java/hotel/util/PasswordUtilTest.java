package hotel.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    @DisplayName("Should hash password with BCrypt and verify successfully")
    void testPasswordHashingAndVerification() {
        String plain = "Admin@Secret123";
        String hash = PasswordUtil.hashPassword(plain);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
        assertEquals(60, hash.length());

        assertTrue(PasswordUtil.checkPassword(plain, hash));
        assertFalse(PasswordUtil.checkPassword("WrongPassword", hash));
    }

    @Test
    @DisplayName("Should reject null or empty password inputs")
    void testInvalidPasswordInputs() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(""));

        assertFalse(PasswordUtil.checkPassword(null, "somehash"));
        assertFalse(PasswordUtil.checkPassword("pass", null));
        assertFalse(PasswordUtil.checkPassword("pass", ""));
    }
}
