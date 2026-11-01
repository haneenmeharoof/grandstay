package hotel.validation;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InputValidatorTest {

    @Test
    @DisplayName("Should reject check-in dates in the past")
    void testBookingDatesInPast() {
        LocalDate past = LocalDate.now().minusDays(2);
        LocalDate future = LocalDate.now().plusDays(2);

        ValidationResult res = InputValidator.validateBookingDates(past, future);
        assertFalse(res.isValid());
        assertTrue(res.getFormattedErrors().contains("past"));
    }

    @Test
    @DisplayName("Should reject check-out dates on or before check-in date")
    void testBookingDatesOrder() {
        LocalDate in = LocalDate.now().plusDays(5);
        LocalDate out = in; // same day -> 0 nights

        ValidationResult res = InputValidator.validateBookingDates(in, out);
        assertFalse(res.isValid());
        assertTrue(res.getFormattedErrors().contains("strictly after"));
    }

    @Test
    @DisplayName("Should accept valid forward booking dates")
    void testValidBookingDates() {
        LocalDate in = LocalDate.now().plusDays(1);
        LocalDate out = LocalDate.now().plusDays(4);

        ValidationResult res = InputValidator.validateBookingDates(in, out);
        assertTrue(res.isValid());
    }

    @Test
    @DisplayName("Should reject guest capacity exceeding room limit")
    void testCapacityValidation() {
        ValidationResult over = InputValidator.validateCapacity(5, 2);
        assertFalse(over.isValid());

        ValidationResult zero = InputValidator.validateCapacity(0, 2);
        assertFalse(zero.isValid());

        ValidationResult valid = InputValidator.validateCapacity(2, 2);
        assertTrue(valid.isValid());
    }

    @Test
    @DisplayName("Should reject inventory deductions that would cause negative stock")
    void testInventoryValidation() {
        int currentStock = 10;

        // Deduction of 15 when only 10 available -> must fail
        ValidationResult fail = InputValidator.validateInventoryQuantity(currentStock, 15, true);
        assertFalse(fail.isValid());
        assertTrue(fail.getFormattedErrors().contains("Insufficient inventory"));

        // Deduction of 8 -> valid
        ValidationResult pass = InputValidator.validateInventoryQuantity(currentStock, 8, true);
        assertTrue(pass.isValid());
    }
}
