package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.WaitlistEntry;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class WaitlistDAO {

    public List<WaitlistEntry> findActiveByRoomType(Long roomTypeId) throws SQLException {
        List<WaitlistEntry> list = new ArrayList<>();
        String sql = baseWaitlistSelect() + " WHERE w.room_type_id = ? AND w.status = 'ACTIVE' ORDER BY w.created_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, roomTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntry(rs));
                }
            }
        }
        return list;
    }

    public List<WaitlistEntry> findByUserId(Long userId) throws SQLException {
        List<WaitlistEntry> list = new ArrayList<>();
        String sql = baseWaitlistSelect() + " WHERE w.user_id = ? ORDER BY w.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntry(rs));
                }
            }
        }
        return list;
    }

    public List<WaitlistEntry> findAll() throws SQLException {
        List<WaitlistEntry> list = new ArrayList<>();
        String sql = baseWaitlistSelect() + " ORDER BY w.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToEntry(rs));
            }
        }
        return list;
    }

    public Long insert(WaitlistEntry entry) throws SQLException {
        String sql = "INSERT INTO waitlist (user_id, guest_id, room_type_id, desired_check_in, desired_check_out, " +
                "number_of_guests, status, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (entry.getUserId() != null) {
                ps.setLong(1, entry.getUserId());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }
            ps.setLong(2, entry.getGuestId());
            ps.setLong(3, entry.getRoomTypeId());
            ps.setDate(4, Date.valueOf(entry.getDesiredCheckIn()));
            ps.setDate(5, Date.valueOf(entry.getDesiredCheckOut()));
            ps.setInt(6, entry.getNumberOfGuests());
            ps.setString(7, entry.getStatus().name());
            ps.setString(8, entry.getNotes());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    entry.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public boolean updateStatus(Long entryId, WaitlistEntry.Status status) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE waitlist SET status = ? ");
        if (status == WaitlistEntry.Status.NOTIFIED) {
            sql.append(", notified_at = CURRENT_TIMESTAMP ");
        }
        sql.append("WHERE id = ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, status.name());
            ps.setLong(2, entryId);
            return ps.executeUpdate() > 0;
        }
    }

    private String baseWaitlistSelect() {
        return "SELECT w.*, rt.name as room_type_name, " +
                "CONCAT(g.first_name, ' ', g.last_name) as guest_full_name " +
                "FROM waitlist w " +
                "JOIN room_types rt ON w.room_type_id = rt.id " +
                "JOIN guests g ON w.guest_id = g.id ";
    }

    private WaitlistEntry mapResultSetToEntry(ResultSet rs) throws SQLException {
        WaitlistEntry e = new WaitlistEntry();
        e.setId(rs.getLong("id"));
        long uId = rs.getLong("user_id");
        if (!rs.wasNull()) e.setUserId(uId);
        e.setGuestId(rs.getLong("guest_id"));
        e.setGuestName(rs.getString("guest_full_name"));
        e.setRoomTypeId(rs.getLong("room_type_id"));
        e.setRoomTypeName(rs.getString("room_type_name"));
        e.setDesiredCheckIn(rs.getDate("desired_check_in").toLocalDate());
        e.setDesiredCheckOut(rs.getDate("desired_check_out").toLocalDate());
        e.setNumberOfGuests(rs.getInt("number_of_guests"));
        e.setStatus(WaitlistEntry.Status.valueOf(rs.getString("status")));
        e.setNotes(rs.getString("notes"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) e.setCreatedAt(created.toLocalDateTime());
        Timestamp notified = rs.getTimestamp("notified_at");
        if (notified != null) e.setNotifiedAt(notified.toLocalDateTime());
        return e;
    }
}
