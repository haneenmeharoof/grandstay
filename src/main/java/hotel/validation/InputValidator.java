package hotel.validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Domain-level validation rules for user input, bookings, inventory, and finances.
 */
public final class InputValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[+]?[0-9\\s\\-().]{7,25}$");

    private InputValidator() {}

    public static ValidationResult validateUserRegistration(String username, String email, String password, String fullName) {
        ValidationResult result = new ValidationResult();

        if (username == null || username.trim().length() < 3) {
            result.addError("Username must be at least 3 characters long.");
        } else if (!username.matches("^[a-zA-Z0-9_.-]+$")) {
            result.addError("Username may only contain letters, numbers, dots, hyphens, and underscores.");
        }

        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            result.addError("Please enter a valid email address (e.g., name@example.com).");
        }

        if (password == null || password.length() < 6) {
            result.addError("Password must be at least 6 characters long.");
        }

        if (fullName == null || fullName.trim().length() < 2) {
            result.addError("Full name must be at least 2 characters long.");
        }

        return result;
    }

    public static ValidationResult validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        ValidationResult result = new ValidationResult();

        if (checkIn == null) {
            result.addError("Check-in date is required.");
        }
        if (checkOut == null) {
            result.addError("Check-out date is required.");
        }

        if (checkIn != null && checkOut != null) {
            LocalDate today = LocalDate.now();
            if (checkIn.isBefore(today)) {
                result.addError("Check-in date cannot be in the past.");
            }
            if (!checkOut.isAfter(checkIn)) {
                result.addError("Check-out date must be strictly after the check-in date (minimum 1 night).");
            }
        }

        return result;
    }

    public static ValidationResult validateCapacity(int numberOfGuests, int maxCapacity) {
        ValidationResult result = new ValidationResult();
        if (numberOfGuests <= 0) {
            result.addError("Number of guests must be at least 1.");
        }
        if (numberOfGuests > maxCapacity) {
            result.addError(String.format("Room capacity exceeded! This room type holds a maximum of %d guests (requested: %d).",
                    maxCapacity, numberOfGuests));
        }
        return result;
    }

    public static ValidationResult validatePositiveAmount(BigDecimal amount, String fieldName) {
        ValidationResult result = new ValidationResult();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            result.addError(fieldName + " must be greater than zero.");
        }
        return result;
    }

    public static ValidationResult validateInventoryQuantity(int currentStock, int changeQuantity, boolean isDeduction) {
        ValidationResult result = new ValidationResult();
        if (changeQuantity <= 0) {
            result.addError("Transaction quantity must be greater than zero.");
            return result;
        }
        if (isDeduction && (currentStock - changeQuantity < 0)) {
            result.addError(String.format("Insufficient inventory! Available stock: %d, requested deduction: %d.",
                    currentStock, changeQuantity));
        }
        return result;
    }

    public static ValidationResult validateEmail(String email) {
        ValidationResult result = new ValidationResult();
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            result.addError("Invalid email format.");
        }
        return result;
    }

    public static ValidationResult validatePhone(String phone) {
        ValidationResult result = new ValidationResult();
        if (phone == null || !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            result.addError("Invalid phone number format.");
        }
        return result;
    }
}
