package hotel.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ReservationCalculationTest {

    private final ReservationService reservationService = new ReservationService();

    @Test
    @DisplayName("Should accurately calculate breakdown of nightly rates, taxes, fees, and total")
    void testCalculateCostBreakdown() {
        LocalDate in = LocalDate.of(2026, 12, 1);
        LocalDate out = LocalDate.of(2026, 12, 4); // 3 nights
        BigDecimal rate = new BigDecimal("120.00");
        BigDecimal discount = new BigDecimal("10.00");

        ReservationService.CostBreakdown cost = reservationService.calculateCost(rate, in, out, discount);

        assertEquals(3, cost.nights());
        assertEquals(new BigDecimal("120.00"), cost.roomRate());
        assertEquals(new BigDecimal("360.00"), cost.totalRoomCharge()); // 3 * 120.00
        assertEquals(new BigDecimal("36.00"), cost.taxAmount());        // 10% tax on 360 = 36.00
        assertEquals(new BigDecimal("15.00"), cost.serviceFee());       // $15 fee
        assertEquals(new BigDecimal("10.00"), cost.discountAmount());   // $10 discount
        // 360 + 36 + 15 - 10 = 401.00
        assertEquals(new BigDecimal("401.00"), cost.totalAmount());
    }
}
