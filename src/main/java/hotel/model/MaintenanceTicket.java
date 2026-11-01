package hotel.model;

import java.time.LocalDateTime;

public class MaintenanceTicket {
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Status {
        OPEN("Open"),
        IN_PROGRESS("In Progress"),
        RESOLVED("Resolved"),
        CLOSED("Closed");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private Long id;
    private String ticketNumber;
    private Long roomId;
    private String roomNumber;
    private Long reportedByUserId;
    private Long assignedToUserId;
    private String assignedStaffName;
    private String issueDescription;
    private Priority priority = Priority.MEDIUM;
    private Status status = Status.OPEN;
    private String resolutionNotes;
    private boolean roomMadeUnavailable;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public MaintenanceTicket() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Long getReportedByUserId() { return reportedByUserId; }
    public void setReportedByUserId(Long reportedByUserId) { this.reportedByUserId = reportedByUserId; }

    public Long getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(Long assignedToUserId) { this.assignedToUserId = assignedToUserId; }

    public String getAssignedStaffName() { return assignedStaffName; }
    public void setAssignedStaffName(String assignedStaffName) { this.assignedStaffName = assignedStaffName; }

    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public boolean isRoomMadeUnavailable() { return roomMadeUnavailable; }
    public void setRoomMadeUnavailable(boolean roomMadeUnavailable) { this.roomMadeUnavailable = roomMadeUnavailable; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
