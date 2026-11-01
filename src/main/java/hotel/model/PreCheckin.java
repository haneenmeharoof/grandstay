package hotel.model;

import java.time.LocalDateTime;

public class PreCheckin {
    public enum Status {
        SUBMITTED("Submitted / Under Review"),
        APPROVED("Approved by Staff"),
        REJECTED("Rejected"),
        NEEDS_UPDATE("Needs Update / Correction");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private Long id;
    private Long reservationId;
    private String reservationRef;
    private Long userId;
    private String expectedArrivalTime;
    private String idDocumentType;
    private String idDocumentNumber;
    private String specialRequests;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Status status = Status.SUBMITTED;
    private String staffNotes;
    private Long reviewedByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public PreCheckin() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public String getReservationRef() { return reservationRef; }
    public void setReservationRef(String reservationRef) { this.reservationRef = reservationRef; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getExpectedArrivalTime() { return expectedArrivalTime; }
    public void setExpectedArrivalTime(String expectedArrivalTime) { this.expectedArrivalTime = expectedArrivalTime; }

    public String getIdDocumentType() { return idDocumentType; }
    public void setIdDocumentType(String idDocumentType) { this.idDocumentType = idDocumentType; }

    public String getIdDocumentNumber() { return idDocumentNumber; }
    public void setIdDocumentNumber(String idDocumentNumber) { this.idDocumentNumber = idDocumentNumber; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getStaffNotes() { return staffNotes; }
    public void setStaffNotes(String staffNotes) { this.staffNotes = staffNotes; }

    public Long getReviewedByUserId() { return reviewedByUserId; }
    public void setReviewedByUserId(Long reviewedByUserId) { this.reviewedByUserId = reviewedByUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
}
