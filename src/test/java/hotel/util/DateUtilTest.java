package hotel.util;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DateUtilTest {

    @Test
    @DisplayName("Should correctly calculate number of nights between check-in and check-out")
    void testCalculateNights() {
        LocalDate in = LocalDate.of(2026, 11, 1);
        LocalDate out = LocalDate.of(2026, 11, 5);
        assertEquals(4, DateUtil.calculateNights(in, out));

        // Same-day or 0 days defaults to 1 minimum night
        assertEquals(1, DateUtil.calculateNights(in, in));
    }

    @Test
    @DisplayName("Should correctly identify overlapping date ranges")
    void testDatesOverlap() {
        LocalDate startA = LocalDate.of(2026, 11, 10);
        LocalDate endA = LocalDate.of(2026, 11, 15);

        // Case 1: Overlapping in the middle
        LocalDate startB = LocalDate.of(2026, 11, 12);
        LocalDate endB = LocalDate.of(2026, 11, 18);
        assertTrue(DateUtil.datesOverlap(startA, endA, startB, endB));

        // Case 2: Adjacent dates (checkout of A is check-in of B) -> NO overlap
        LocalDate startC = LocalDate.of(2026, 11, 15);
        LocalDate endC = LocalDate.of(2026, 11, 20);
        assertFalse(DateUtil.datesOverlap(startA, endA, startC, endC));

        // Case 3: Completely before
        LocalDate startD = LocalDate.of(2026, 11, 1);
        LocalDate endD = LocalDate.of(2026, 11, 5);
        assertFalse(DateUtil.datesOverlap(startA, endA, startD, endD));
    }
}
