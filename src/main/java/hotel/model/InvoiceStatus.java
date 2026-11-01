package hotel.model;

public enum InvoiceStatus {
    ISSUED("Issued"),
    PARTIALLY_PAID("Partially Paid"),
    PAID("Paid in Full"),
    CANCELLED("Cancelled");

    private final String displayName;

    InvoiceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
