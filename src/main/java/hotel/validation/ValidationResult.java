package hotel.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates validation success status and error messages.
 */
public class ValidationResult {
    private final List<String> errors = new ArrayList<>();

    public static ValidationResult success() {
        return new ValidationResult();
    }

    public static ValidationResult error(String message) {
        ValidationResult result = new ValidationResult();
        result.addError(message);
        return result;
    }

    public void addError(String message) {
        if (message != null && !message.trim().isEmpty()) {
            errors.add(message.trim());
        }
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public String getFormattedErrors() {
        return String.join("\n• ", errors);
    }
}
