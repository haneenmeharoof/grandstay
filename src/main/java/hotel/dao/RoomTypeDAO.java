package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.RoomType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomTypeDAO {

    public Optional<RoomType> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM room_types WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRoomType(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<RoomType> findAllActive() throws SQLException {
        List<RoomType> list = new ArrayList<>();
        String sql = "SELECT * FROM room_types WHERE is_active = TRUE ORDER BY base_price ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToRoomType(rs));
            }
        }
        return list;
    }

    public List<RoomType> findAll() throws SQLException {
        List<RoomType> list = new ArrayList<>();
        String sql = "SELECT * FROM room_types ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToRoomType(rs));
            }
        }
        return list;
    }

    public Long insert(RoomType rt) throws SQLException {
        String sql = "INSERT INTO room_types (code, name, description, base_price, max_capacity, bed_type, amenities, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, rt.getCode());
            ps.setString(2, rt.getName());
            ps.setString(3, rt.getDescription());
            ps.setBigDecimal(4, rt.getBasePrice());
            ps.setInt(5, rt.getMaxCapacity());
            ps.setString(6, rt.getBedType());
            ps.setString(7, rt.getAmenities());
            ps.setBoolean(8, rt.isActive());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    rt.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public boolean update(RoomType rt) throws SQLException {
        String sql = "UPDATE room_types SET code = ?, name = ?, description = ?, base_price = ?, " +
                "max_capacity = ?, bed_type = ?, amenities = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rt.getCode());
            ps.setString(2, rt.getName());
            ps.setString(3, rt.getDescription());
            ps.setBigDecimal(4, rt.getBasePrice());
            ps.setInt(5, rt.getMaxCapacity());
            ps.setString(6, rt.getBedType());
            ps.setString(7, rt.getAmenities());
            ps.setBoolean(8, rt.isActive());
            ps.setLong(9, rt.getId());
            return ps.executeUpdate() > 0;
        }
    }

    private RoomType mapResultSetToRoomType(ResultSet rs) throws SQLException {
        RoomType rt = new RoomType();
        rt.setId(rs.getLong("id"));
        rt.setCode(rs.getString("code"));
        rt.setName(rs.getString("name"));
        rt.setDescription(rs.getString("description"));
        rt.setBasePrice(rs.getBigDecimal("base_price"));
        rt.setMaxCapacity(rs.getInt("max_capacity"));
        rt.setBedType(rs.getString("bed_type"));
        rt.setAmenities(rs.getString("amenities"));
        rt.setActive(rs.getBoolean("is_active"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) rt.setCreatedAt(created.toLocalDateTime());
        return rt;
    }
}
