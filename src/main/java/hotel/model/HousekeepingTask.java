package hotel.model;

import java.time.LocalDateTime;

public class HousekeepingTask {
    public enum Priority {
        LOW, MEDIUM, HIGH, URGENT
    }

    public enum Status {
        PENDING("Pending"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        INSPECTED("Cleaned & Inspected");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private Long id;
    private Long roomId;
    private String roomNumber;
    private Long assignedToUserId;
    private String assignedStaffName;
    private String taskType = "DAILY_CLEANING";
    private Priority priority = Priority.MEDIUM;
    private Status status = Status.PENDING;
    private String notes;
    private String inspectionNotes;
    private Long inspectedByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime inspectedAt;

    public HousekeepingTask() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Long getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(Long assignedToUserId) { this.assignedToUserId = assignedToUserId; }

    public String getAssignedStaffName() { return assignedStaffName; }
    public void setAssignedStaffName(String assignedStaffName) { this.assignedStaffName = assignedStaffName; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getInspectionNotes() { return inspectionNotes; }
    public void setInspectionNotes(String inspectionNotes) { this.inspectionNotes = inspectionNotes; }

    public Long getInspectedByUserId() { return inspectedByUserId; }
    public void setInspectedByUserId(Long inspectedByUserId) { this.inspectedByUserId = inspectedByUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getInspectedAt() { return inspectedAt; }
    public void setInspectedAt(LocalDateTime inspectedAt) { this.inspectedAt = inspectedAt; }
}
