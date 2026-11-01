package hotel.model;

public enum PaymentMethod {
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    BANK_TRANSFER("Bank Transfer"),
    ONLINE("Online / Card Gateway");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
