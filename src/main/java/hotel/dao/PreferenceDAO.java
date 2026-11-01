package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.CustomerPreference;
import hotel.model.RoomType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PreferenceDAO {

    public Optional<CustomerPreference> findByUserId(Long userId) throws SQLException {
        String sql = "SELECT * FROM customer_preferences WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CustomerPreference cp = new CustomerPreference();
                    cp.setId(rs.getLong("id"));
                    cp.setUserId(rs.getLong("user_id"));
                    cp.setPreferredFloor(rs.getString("preferred_floor"));
                    cp.setPreferredBedType(rs.getString("preferred_bed_type"));
                    cp.setQuietRoom(rs.getBoolean("quiet_room"));
                    cp.setHighFloor(rs.getBoolean("high_floor"));
                    cp.setNearElevator(rs.getBoolean("near_elevator"));
                    cp.setDietaryNeeds(rs.getString("dietary_needs"));
                    cp.setNotes(rs.getString("notes"));
                    Timestamp updated = rs.getTimestamp("updated_at");
                    if (updated != null) cp.setUpdatedAt(updated.toLocalDateTime());
                    return Optional.of(cp);
                }
            }
        }
        return Optional.empty();
    }

    public void saveOrUpdate(CustomerPreference cp) throws SQLException {
        String sql = "INSERT INTO customer_preferences (user_id, preferred_floor, preferred_bed_type, quiet_room, " +
                "high_floor, near_elevator, dietary_needs, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE preferred_floor = VALUES(preferred_floor), " +
                "preferred_bed_type = VALUES(preferred_bed_type), quiet_room = VALUES(quiet_room), " +
                "high_floor = VALUES(high_floor), near_elevator = VALUES(near_elevator), " +
                "dietary_needs = VALUES(dietary_needs), notes = VALUES(notes)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cp.getUserId());
            ps.setString(2, cp.getPreferredFloor());
            ps.setString(3, cp.getPreferredBedType());
            ps.setBoolean(4, cp.isQuietRoom());
            ps.setBoolean(5, cp.isHighFloor());
            ps.setBoolean(6, cp.isNearElevator());
            ps.setString(7, cp.getDietaryNeeds());
            ps.setString(8, cp.getNotes());
            ps.executeUpdate();
        }
    }

    public List<Long> getFavoriteRoomTypeIds(Long userId) throws SQLException {
        List<Long> ids = new ArrayList<>();
        String sql = "SELECT room_type_id FROM customer_favorites WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getLong("room_type_id"));
                }
            }
        }
        return ids;
    }

    public void addFavorite(Long userId, Long roomTypeId) throws SQLException {
        String sql = "INSERT IGNORE INTO customer_favorites (user_id, room_type_id) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, roomTypeId);
            ps.executeUpdate();
        }
    }

    public void removeFavorite(Long userId, Long roomTypeId) throws SQLException {
        String sql = "DELETE FROM customer_favorites WHERE user_id = ? AND room_type_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, roomTypeId);
            ps.executeUpdate();
        }
    }
}
