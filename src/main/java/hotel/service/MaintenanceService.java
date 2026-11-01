package hotel.service;

import hotel.dao.AuditLogDAO;
import hotel.dao.MaintenanceDAO;
import hotel.dao.RoomDAO;
import hotel.model.AuditLog;
import hotel.model.MaintenanceTicket;
import hotel.model.Room;
import hotel.model.RoomStatus;
import hotel.model.User;
import hotel.util.SessionContext;
import hotel.validation.ValidationResult;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MaintenanceService {

    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    public List<MaintenanceTicket> getAllTickets() throws SQLException {
        return maintenanceDAO.findAll();
    }

    public List<MaintenanceTicket> getOpenTickets() throws SQLException {
        return maintenanceDAO.findOpenTickets();
    }

    public ValidationResult createTicket(Long roomId, String description, MaintenanceTicket.Priority priority,
                                         boolean makeUnavailable, Long assignedStaffId) {
        if (roomId == null) {
            return ValidationResult.error("Please select a room.");
        }
        if (description == null || description.trim().isEmpty()) {
            return ValidationResult.error("Please enter issue description.");
        }

        try {
            Optional<Room> roomOpt = roomDAO.findById(roomId);
            if (roomOpt.isEmpty()) {
                return ValidationResult.error("Room not found.");
            }
            Room room = roomOpt.get();

            String ticketNum = "MNT-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            User actor = SessionContext.getCurrentUser();
            Long actorId = actor != null ? actor.getId() : null;

            MaintenanceTicket t = new MaintenanceTicket();
            t.setTicketNumber(ticketNum);
            t.setRoomId(roomId);
            t.setReportedByUserId(actorId);
            t.setAssignedToUserId(assignedStaffId);
            t.setIssueDescription(description.trim());
            t.setPriority(priority != null ? priority : MaintenanceTicket.Priority.MEDIUM);
            t.setStatus(MaintenanceTicket.Status.OPEN);
            t.setRoomMadeUnavailable(makeUnavailable);

            maintenanceDAO.insert(t);

            if (makeUnavailable) {
                roomDAO.updateStatus(roomId, RoomStatus.MAINTENANCE);
            }

            auditLogDAO.log(new AuditLog(actorId, actor != null ? actor.getUsername() : "Staff",
                    actor != null ? actor.getRole().name() : "STAFF", "MAINTENANCE_TICKET_CREATED",
                    "ROOM", String.valueOf(roomId), "Created maintenance ticket " + ticketNum + " for Room " + room.getRoomNumber()));

            return ValidationResult.success();
        } catch (SQLException e) {
            return ValidationResult.error("Failed to create maintenance ticket: " + e.getMessage());
        }
    }

    public ValidationResult updateTicketStatus(Long ticketId, MaintenanceTicket.Status status, String resolutionNotes) {
        try {
            Optional<MaintenanceTicket> ticketOpt = maintenanceDAO.findById(ticketId);
            if (ticketOpt.isEmpty()) {
                return ValidationResult.error("Maintenance ticket not found.");
            }
            MaintenanceTicket ticket = ticketOpt.get();

            maintenanceDAO.updateStatus(ticketId, status, resolutionNotes);

            // If resolved or closed and room was made unavailable, restore room availability
            if ((status == MaintenanceTicket.Status.RESOLVED || status == MaintenanceTicket.Status.CLOSED) && ticket.isRoomMadeUnavailable()) {
                roomDAO.updateStatus(ticket.getRoomId(), RoomStatus.AVAILABLE);
            }

            User actor = SessionContext.getCurrentUser();
            auditLogDAO.log(new AuditLog(actor != null ? actor.getId() : null,
                    actor != null ? actor.getUsername() : "Staff",
                    actor != null ? actor.getRole().name() : "STAFF",
                    "MAINTENANCE_STATUS_UPDATE", "TICKET", String.valueOf(ticketId),
                    "Updated ticket " + ticket.getTicketNumber() + " to " + status.getDisplayName()));

            return ValidationResult.success();
        } catch (SQLException e) {
            return ValidationResult.error("Failed to update maintenance ticket: " + e.getMessage());
        }
    }
}
