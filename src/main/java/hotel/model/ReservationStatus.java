package hotel.model;

public enum ReservationStatus {
    PENDING("Pending Confirmation"),
    CONFIRMED("Confirmed"),
    CHECKED_IN("Checked In"),
    CHECKED_OUT("Checked Out"),
    CANCELLED("Cancelled"),
    NO_SHOW("No Show");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isCancellable() {
        return this == PENDING || this == CONFIRMED;
    }

    public boolean isActive() {
        return this == CONFIRMED || this == CHECKED_IN;
    }
}
