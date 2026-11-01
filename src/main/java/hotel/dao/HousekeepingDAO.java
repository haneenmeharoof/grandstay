package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.HousekeepingTask;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HousekeepingDAO {

    public Optional<HousekeepingTask> findById(Long id) throws SQLException {
        String sql = baseTaskSelect() + " WHERE ht.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTask(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<HousekeepingTask> findAll() throws SQLException {
        List<HousekeepingTask> list = new ArrayList<>();
        String sql = baseTaskSelect() + " ORDER BY ht.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToTask(rs));
            }
        }
        return list;
    }

    public List<HousekeepingTask> findByStaffUserId(Long userId) throws SQLException {
        List<HousekeepingTask> list = new ArrayList<>();
        String sql = baseTaskSelect() + " WHERE ht.assigned_to_user_id = ? ORDER BY ht.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTask(rs));
                }
            }
        }
        return list;
    }

    public Long insert(HousekeepingTask task) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return insertWithConnection(conn, task);
        }
    }

    public Long insertWithConnection(Connection conn, HousekeepingTask task) throws SQLException {
        String sql = "INSERT INTO housekeeping_tasks (room_id, assigned_to_user_id, task_type, priority, status, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, task.getRoomId());
            if (task.getAssignedToUserId() != null) ps.setLong(2, task.getAssignedToUserId());
            else ps.setNull(2, java.sql.Types.BIGINT);
            ps.setString(3, task.getTaskType());
            ps.setString(4, task.getPriority().name());
            ps.setString(5, task.getStatus().name());
            ps.setString(6, task.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    task.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public boolean updateStatus(Long taskId, HousekeepingTask.Status status, String notes) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE housekeeping_tasks SET status = ? ");
        if (status == HousekeepingTask.Status.COMPLETED) {
            sql.append(", completed_at = CURRENT_TIMESTAMP ");
        } else if (status == HousekeepingTask.Status.INSPECTED) {
            sql.append(", inspected_at = CURRENT_TIMESTAMP ");
        }
        if (notes != null) {
            sql.append(", notes = ? ");
        }
        sql.append("WHERE id = ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, status.name());
            int idx = 2;
            if (notes != null) {
                ps.setString(idx++, notes);
            }
            ps.setLong(idx, taskId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean assignStaff(Long taskId, Long staffUserId) throws SQLException {
        String sql = "UPDATE housekeeping_tasks SET assigned_to_user_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, staffUserId);
            ps.setLong(2, taskId);
            return ps.executeUpdate() > 0;
        }
    }

    private String baseTaskSelect() {
        return "SELECT ht.*, r.room_number, u.full_name as assigned_staff_name " +
                "FROM housekeeping_tasks ht " +
                "JOIN rooms r ON ht.room_id = r.id " +
                "LEFT JOIN users u ON ht.assigned_to_user_id = u.id ";
    }

    private HousekeepingTask mapResultSetToTask(ResultSet rs) throws SQLException {
        HousekeepingTask t = new HousekeepingTask();
        t.setId(rs.getLong("id"));
        t.setRoomId(rs.getLong("room_id"));
        t.setRoomNumber(rs.getString("room_number"));
        long staffId = rs.getLong("assigned_to_user_id");
        if (!rs.wasNull()) t.setAssignedToUserId(staffId);
        t.setAssignedStaffName(rs.getString("assigned_staff_name"));
        t.setTaskType(rs.getString("task_type"));
        t.setPriority(HousekeepingTask.Priority.valueOf(rs.getString("priority")));
        t.setStatus(HousekeepingTask.Status.valueOf(rs.getString("status")));
        t.setNotes(rs.getString("notes"));
        t.setInspectionNotes(rs.getString("inspection_notes"));
        long inspId = rs.getLong("inspected_by_user_id");
        if (!rs.wasNull()) t.setInspectedByUserId(inspId);
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) t.setCreatedAt(created.toLocalDateTime());
        Timestamp comp = rs.getTimestamp("completed_at");
        if (comp != null) t.setCompletedAt(comp.toLocalDateTime());
        Timestamp insp = rs.getTimestamp("inspected_at");
        if (insp != null) t.setInspectedAt(insp.toLocalDateTime());
        return t;
    }
}
