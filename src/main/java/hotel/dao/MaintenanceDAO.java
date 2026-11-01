package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.MaintenanceTicket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaintenanceDAO {

    public Optional<MaintenanceTicket> findById(Long id) throws SQLException {
        String sql = baseTicketSelect() + " WHERE mt.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTicket(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<MaintenanceTicket> findAll() throws SQLException {
        List<MaintenanceTicket> list = new ArrayList<>();
        String sql = baseTicketSelect() + " ORDER BY mt.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToTicket(rs));
            }
        }
        return list;
    }

    public List<MaintenanceTicket> findOpenTickets() throws SQLException {
        List<MaintenanceTicket> list = new ArrayList<>();
        String sql = baseTicketSelect() + " WHERE mt.status IN ('OPEN', 'IN_PROGRESS') ORDER BY mt.priority DESC, mt.created_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToTicket(rs));
            }
        }
        return list;
    }

    public Long insert(MaintenanceTicket ticket) throws SQLException {
        String sql = "INSERT INTO maintenance_tickets (ticket_number, room_id, reported_by_user_id, assigned_to_user_id, " +
                "issue_description, priority, status, room_made_unavailable) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ticket.getTicketNumber());
            ps.setLong(2, ticket.getRoomId());
            if (ticket.getReportedByUserId() != null) {
                ps.setLong(3, ticket.getReportedByUserId());
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
            }
            if (ticket.getAssignedToUserId() != null) {
                ps.setLong(4, ticket.getAssignedToUserId());
            } else {
                ps.setNull(4, java.sql.Types.BIGINT);
            }
            ps.setString(5, ticket.getIssueDescription());
            ps.setString(6, ticket.getPriority().name());
            ps.setString(7, ticket.getStatus().name());
            ps.setBoolean(8, ticket.isRoomMadeUnavailable());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    ticket.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public boolean updateStatus(Long ticketId, MaintenanceTicket.Status status, String resolutionNotes) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE maintenance_tickets SET status = ?, resolution_notes = ? ");
        if (status == MaintenanceTicket.Status.RESOLVED || status == MaintenanceTicket.Status.CLOSED) {
            sql.append(", resolved_at = CURRENT_TIMESTAMP ");
        }
        sql.append("WHERE id = ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, status.name());
            ps.setString(2, resolutionNotes);
            ps.setLong(3, ticketId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean assignStaff(Long ticketId, Long staffUserId) throws SQLException {
        String sql = "UPDATE maintenance_tickets SET assigned_to_user_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, staffUserId);
            ps.setLong(2, ticketId);
            return ps.executeUpdate() > 0;
        }
    }

    private String baseTicketSelect() {
        return "SELECT mt.*, r.room_number, u.full_name as assigned_staff_name " +
                "FROM maintenance_tickets mt " +
                "JOIN rooms r ON mt.room_id = r.id " +
                "LEFT JOIN users u ON mt.assigned_to_user_id = u.id ";
    }

    private MaintenanceTicket mapResultSetToTicket(ResultSet rs) throws SQLException {
        MaintenanceTicket t = new MaintenanceTicket();
        t.setId(rs.getLong("id"));
        t.setTicketNumber(rs.getString("ticket_number"));
        t.setRoomId(rs.getLong("room_id"));
        t.setRoomNumber(rs.getString("room_number"));
        long repId = rs.getLong("reported_by_user_id");
        if (!rs.wasNull()) t.setReportedByUserId(repId);
        long assId = rs.getLong("assigned_to_user_id");
        if (!rs.wasNull()) t.setAssignedToUserId(assId);
        t.setAssignedStaffName(rs.getString("assigned_staff_name"));
        t.setIssueDescription(rs.getString("issue_description"));
        t.setPriority(MaintenanceTicket.Priority.valueOf(rs.getString("priority")));
        t.setStatus(MaintenanceTicket.Status.valueOf(rs.getString("status")));
        t.setResolutionNotes(rs.getString("resolution_notes"));
        t.setRoomMadeUnavailable(rs.getBoolean("room_made_unavailable"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) t.setCreatedAt(created.toLocalDateTime());
        Timestamp res = rs.getTimestamp("resolved_at");
        if (res != null) t.setResolvedAt(res.toLocalDateTime());
        return t;
    }
}
