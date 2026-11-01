package hotel.model;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ModelLogicTest {

    @Test
    @DisplayName("Reservation balance due and payment status logic")
    void testReservationBalanceDue() {
        Reservation res = new Reservation();
        res.setTotalAmount(new BigDecimal("500.00"));
        res.setPaidAmount(new BigDecimal("200.00"));

        assertEquals(new BigDecimal("300.00"), res.getBalanceDue());
        assertFalse(res.isFullyPaid());

        res.setPaidAmount(new BigDecimal("500.00"));
        assertEquals(new BigDecimal("0.00"), res.getBalanceDue());
        assertTrue(res.isFullyPaid());
    }

    @Test
    @DisplayName("Reservation cancellation eligibility by status")
    void testCancellationEligibility() {
        assertTrue(ReservationStatus.PENDING.isCancellable());
        assertTrue(ReservationStatus.CONFIRMED.isCancellable());

        assertFalse(ReservationStatus.CHECKED_IN.isCancellable());
        assertFalse(ReservationStatus.CHECKED_OUT.isCancellable());
        assertFalse(ReservationStatus.CANCELLED.isCancellable());
    }

    @Test
    @DisplayName("Room bookability status")
    void testRoomBookable() {
        assertTrue(RoomStatus.AVAILABLE.isBookable());
        assertTrue(RoomStatus.INSPECTED.isBookable());

        assertFalse(RoomStatus.OCCUPIED.isBookable());
        assertFalse(RoomStatus.DIRTY.isBookable());
        assertFalse(RoomStatus.CLEANING.isBookable());
        assertFalse(RoomStatus.MAINTENANCE.isBookable());
        assertFalse(RoomStatus.OUT_OF_SERVICE.isBookable());
    }
}
