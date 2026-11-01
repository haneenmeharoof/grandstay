package hotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WaitlistEntry {
    public enum Status {
    ACTIVE("Pending Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    NOTIFIED("Matching Room Available"),
    CONVERTED("Converted to Booking"),
    CANCELLED("Cancelled"),
    EXPIRED("Expired");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private Long id;
    private Long userId;
    private Long guestId;
    private String guestName;
    private Long roomTypeId;
    private String roomTypeName;
    private LocalDate desiredCheckIn;
    private LocalDate desiredCheckOut;
    private int numberOfGuests = 1;
    private Status status = Status.ACTIVE;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime notifiedAt;

    public WaitlistEntry() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public Long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(Long roomTypeId) { this.roomTypeId = roomTypeId; }

    public String getRoomTypeName() { return roomTypeName; }
    public void setRoomTypeName(String roomTypeName) { this.roomTypeName = roomTypeName; }

    public LocalDate getDesiredCheckIn() { return desiredCheckIn; }
    public void setDesiredCheckIn(LocalDate desiredCheckIn) { this.desiredCheckIn = desiredCheckIn; }

    public LocalDate getDesiredCheckOut() { return desiredCheckOut; }
    public void setDesiredCheckOut(LocalDate desiredCheckOut) { this.desiredCheckOut = desiredCheckOut; }

    public int getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(int numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getNotifiedAt() { return notifiedAt; }
    public void setNotifiedAt(LocalDateTime notifiedAt) { this.notifiedAt = notifiedAt; }
}
