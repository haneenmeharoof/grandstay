package hotel.model;

public enum RoomStatus {
    AVAILABLE("Available"),
    RESERVED("Reserved for Upcoming Booking"),
    OCCUPIED("Occupied"),
    DIRTY("Dirty / Needs Cleaning"),
    CLEANING("Cleaning In Progress"),
    INSPECTED("Clean & Inspected"),
    MAINTENANCE("Under Maintenance"),
    OUT_OF_SERVICE("Out of Service");

    private final String displayName;

    RoomStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isBookable() {
        return this == AVAILABLE || this == INSPECTED;
    }
}
