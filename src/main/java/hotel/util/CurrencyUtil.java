package hotel.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Currency utility strictly using BigDecimal for monetary operations.
 * Never uses double or float for financial amounts.
 */
public final class CurrencyUtil {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);

    private CurrencyUtil() {}

    public static BigDecimal roundMoney(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return "$0.00";
        }
        return CURRENCY_FORMAT.format(roundMoney(amount));
    }

    public static BigDecimal calculateTax(BigDecimal subtotal, BigDecimal taxRate) {
        if (subtotal == null || taxRate == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return roundMoney(subtotal.multiply(taxRate));
    }

    public static BigDecimal calculateTotal(BigDecimal roomCharges, BigDecimal taxes, BigDecimal fees, BigDecimal discount) {
        BigDecimal total = BigDecimal.ZERO;
        if (roomCharges != null) total = total.add(roomCharges);
        if (taxes != null) total = total.add(taxes);
        if (fees != null) total = total.add(fees);
        if (discount != null) total = total.subtract(discount);
        return total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : roundMoney(total);
    }
}
