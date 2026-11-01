package hotel.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Common date and time utilities for bookings, reporting, and formatting.
 */
public final class DateUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    private DateUtil() {}

    /**
     * Calculates number of nights between checkIn and checkOut.
     * Minimum 1 night.
     */
    public static int calculateNights(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            return 0;
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        return (int) Math.max(1, nights);
    }

    /**
     * Checks if two date ranges [start1, end1) and [start2, end2) overlap.
     * In hotel reservations, check-out date of reservation A can be check-in date of reservation B
     * without conflict. Hence strictly: start1 < end2 AND end1 > start2.
     */
    public static boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DISPLAY_DATE_FORMATTER) : "N/A";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_DATETIME_FORMATTER) : "N/A";
    }

    public static String toSqlDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
}
