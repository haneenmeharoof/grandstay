package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.Notification;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public List<Notification> findByUserId(Long userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        }
        return list;
    }

    public int countUnread(Long userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public Long insert(Notification n) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, title, message, notification_type, is_read, reference_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, n.getUserId());
            ps.setString(2, n.getTitle());
            ps.setString(3, n.getMessage());
            ps.setString(4, n.getNotificationType());
            ps.setBoolean(5, n.isRead());
            ps.setString(6, n.getReferenceId());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    n.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public boolean markAsRead(Long notificationId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, notificationId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean markAllAsRead(Long userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setUserId(rs.getLong("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setNotificationType(rs.getString("notification_type"));
        n.setRead(rs.getBoolean("is_read"));
        n.setReferenceId(rs.getString("reference_id"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) n.setCreatedAt(created.toLocalDateTime());
        return n;
    }
}
