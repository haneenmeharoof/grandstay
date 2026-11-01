package hotel.util;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CurrencyUtilTest {

    @Test
    @DisplayName("Should strictly round monetary values to 2 decimal places using HALF_UP")
    void testRoundMoney() {
        BigDecimal val = new BigDecimal("123.456");
        BigDecimal rounded = CurrencyUtil.roundMoney(val);
        assertEquals(new BigDecimal("123.46"), rounded);

        assertEquals(new BigDecimal("0.00"), CurrencyUtil.roundMoney(null));
    }

    @Test
    @DisplayName("Should calculate exact taxes and totals with discounts using BigDecimal")
    void testTaxAndTotalCalculations() {
        BigDecimal subtotal = new BigDecimal("360.00");
        BigDecimal taxRate = new BigDecimal("0.10"); // 10%
        BigDecimal tax = CurrencyUtil.calculateTax(subtotal, taxRate);
        assertEquals(new BigDecimal("36.00"), tax);

        BigDecimal fees = new BigDecimal("15.00");
        BigDecimal discount = new BigDecimal("20.00");

        BigDecimal total = CurrencyUtil.calculateTotal(subtotal, tax, fees, discount);
        // 360 + 36 + 15 - 20 = 391.00
        assertEquals(new BigDecimal("391.00"), total);
    }
}
