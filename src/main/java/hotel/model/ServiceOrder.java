package hotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServiceOrder {
    public enum Category {
        EXTRA_TOWELS("Extra Towels"),
        ROOM_CLEANING("Room Cleaning"),
        ROOM_SERVICE("Room Service / Dining"),
        MAINTENANCE_ASSISTANCE("Maintenance Assistance"),
        EXTRA_AMENITIES("Extra Amenities"),
        GENERAL_ASSISTANCE("General Front Desk Assistance");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Status {
        PENDING("Pending Review"),
        ASSIGNED("Assigned to Staff"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private Long id;
    private String orderNumber;
    private Long reservationId;
    private String reservationRef;
    private Long guestId;
    private String guestName;
    private Category category = Category.EXTRA_TOWELS;
    private String description;
    private String specialInstructions;
    private Status status = Status.PENDING;
    private Long assignedToUserId;
    private String assignedStaffName;
    private BigDecimal chargeAmount = BigDecimal.ZERO;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public ServiceOrder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public String getReservationRef() { return reservationRef; }
    public void setReservationRef(String reservationRef) { this.reservationRef = reservationRef; }

    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Long getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(Long assignedToUserId) { this.assignedToUserId = assignedToUserId; }

    public String getAssignedStaffName() { return assignedStaffName; }
    public void setAssignedStaffName(String assignedStaffName) { this.assignedStaffName = assignedStaffName; }

    public BigDecimal getChargeAmount() { return chargeAmount; }
    public void setChargeAmount(BigDecimal chargeAmount) { this.chargeAmount = chargeAmount != null ? chargeAmount : BigDecimal.ZERO; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
