package hotel.model;

public enum Role {
    CUSTOMER("Customer"),
    RECEPTIONIST("Receptionist"),
    ADMIN("Hotel Administrator"),
    HOUSEKEEPING("Housekeeping Staff");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
