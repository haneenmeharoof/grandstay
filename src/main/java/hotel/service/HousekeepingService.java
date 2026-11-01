package hotel.service;

import hotel.dao.AuditLogDAO;
import hotel.dao.HousekeepingDAO;
import hotel.dao.RoomDAO;
import hotel.model.AuditLog;
import hotel.model.HousekeepingTask;
import hotel.model.Room;
import hotel.model.RoomStatus;
import hotel.model.User;
import hotel.util.SessionContext;
import hotel.validation.ValidationResult;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class HousekeepingService {

    private final HousekeepingDAO housekeepingDAO = new HousekeepingDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    public List<HousekeepingTask> getAllTasks() throws SQLException {
        return housekeepingDAO.findAll();
    }

    public List<HousekeepingTask> getMyTasks(Long staffUserId) throws SQLException {
        return housekeepingDAO.findByStaffUserId(staffUserId);
    }

    public ValidationResult createTask(Long roomId, Long assignedStaffId, String taskType,
                                       HousekeepingTask.Priority priority, String notes) {
        if (roomId == null) {
            return ValidationResult.error("Please select a room.");
        }

        try {
            Optional<Room> roomOpt = roomDAO.findById(roomId);
            if (roomOpt.isEmpty()) {
                return ValidationResult.error("Room not found.");
            }

            HousekeepingTask task = new HousekeepingTask();
            task.setRoomId(roomId);
            task.setAssignedToUserId(assignedStaffId);
            task.setTaskType(taskType != null ? taskType : "DAILY_CLEANING");
            task.setPriority(priority != null ? priority : HousekeepingTask.Priority.MEDIUM);
            task.setStatus(HousekeepingTask.Status.PENDING);
            task.setNotes(notes);

            housekeepingDAO.insert(task);

            User actor = SessionContext.getCurrentUser();
            auditLogDAO.log(new AuditLog(actor != null ? actor.getId() : null,
                    actor != null ? actor.getUsername() : "Staff",
                    actor != null ? actor.getRole().name() : "STAFF",
                    "HOUSEKEEPING_TASK_CREATED", "ROOM", String.valueOf(roomId),
                    "Created housekeeping task for Room " + roomOpt.get().getRoomNumber()));

            return ValidationResult.success();
        } catch (SQLException e) {
            return ValidationResult.error("Failed to create task: " + e.getMessage());
        }
    }

    public ValidationResult updateTaskStatus(Long taskId, HousekeepingTask.Status status, String notes) {
        try {
            Optional<HousekeepingTask> taskOpt = housekeepingDAO.findById(taskId);
            if (taskOpt.isEmpty()) {
                return ValidationResult.error("Housekeeping task not found.");
            }
            HousekeepingTask task = taskOpt.get();

            housekeepingDAO.updateStatus(taskId, status, notes);

            // Update room operational status accordingly
            if (status == HousekeepingTask.Status.IN_PROGRESS) {
                roomDAO.updateStatus(task.getRoomId(), RoomStatus.CLEANING);
            } else if (status == HousekeepingTask.Status.COMPLETED) {
                roomDAO.updateStatus(task.getRoomId(), RoomStatus.CLEANING);
            } else if (status == HousekeepingTask.Status.INSPECTED) {
                // Room is clean, inspected, and ready for guests
                roomDAO.updateStatus(task.getRoomId(), RoomStatus.AVAILABLE);
            }

            User actor = SessionContext.getCurrentUser();
            auditLogDAO.log(new AuditLog(actor != null ? actor.getId() : null,
                    actor != null ? actor.getUsername() : "Housekeeper",
                    actor != null ? actor.getRole().name() : "HOUSEKEEPING",
                    "HOUSEKEEPING_STATUS_UPDATE", "TASK", String.valueOf(taskId),
                    "Updated task status to " + status.getDisplayName() + " for Room " + task.getRoomNumber()));

            return ValidationResult.success();
        } catch (SQLException e) {
            return ValidationResult.error("Failed to update task: " + e.getMessage());
        }
    }
}
