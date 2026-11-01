package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.AuditLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    public List<AuditLog> findRecent(int limit) throws SQLException {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 100);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLog(rs));
                }
            }
        }
        return list;
    }

    public List<AuditLog> filter(String actionQuery, String entityTypeQuery) throws SQLException {
        List<AuditLog> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_logs WHERE 1=1 ");
        if (actionQuery != null && !actionQuery.trim().isEmpty()) {
            sql.append("AND action LIKE ? ");
        }
        if (entityTypeQuery != null && !entityTypeQuery.trim().isEmpty()) {
            sql.append("AND entity_type = ? ");
        }
        sql.append("ORDER BY timestamp DESC LIMIT 200");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (actionQuery != null && !actionQuery.trim().isEmpty()) {
                ps.setString(idx++, "%" + actionQuery.trim() + "%");
            }
            if (entityTypeQuery != null && !entityTypeQuery.trim().isEmpty()) {
                ps.setString(idx++, entityTypeQuery.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLog(rs));
                }
            }
        }
        return list;
    }

    public void log(AuditLog log) {
        String sql = "INSERT INTO audit_logs (user_id, username, role, action, entity_type, entity_id, details, ip_address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (log.getUserId() != null) {
                ps.setLong(1, log.getUserId());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }
            ps.setString(2, log.getUsername());
            ps.setString(3, log.getRole());
            ps.setString(4, log.getAction());
            ps.setString(5, log.getEntityType());
            ps.setString(6, log.getEntityId());
            ps.setString(7, log.getDetails());
            ps.setString(8, log.getIpAddress());
            ps.executeUpdate();
        } catch (SQLException e) {
            // Audit log write failure should not break primary transaction
            System.err.println("Audit log warning: " + e.getMessage());
        }
    }

    private AuditLog mapResultSetToLog(ResultSet rs) throws SQLException {
        AuditLog al = new AuditLog();
        al.setId(rs.getLong("id"));
        long uId = rs.getLong("user_id");
        if (!rs.wasNull()) al.setUserId(uId);
        al.setUsername(rs.getString("username"));
        al.setRole(rs.getString("role"));
        al.setAction(rs.getString("action"));
        al.setEntityType(rs.getString("entity_type"));
        al.setEntityId(rs.getString("entity_id"));
        al.setDetails(rs.getString("details"));
        al.setIpAddress(rs.getString("ip_address"));
        Timestamp ts = rs.getTimestamp("timestamp");
        if (ts != null) al.setTimestamp(ts.toLocalDateTime());
        return al;
    }
}
