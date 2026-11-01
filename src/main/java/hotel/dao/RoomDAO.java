package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.Room;
import hotel.model.RoomStatus;
import hotel.model.RoomType;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDAO {

    public Optional<Room> findById(Long id) throws SQLException {
        String sql = "SELECT r.*, rt.code as rt_code, rt.name as rt_name, rt.base_price, rt.max_capacity, rt.bed_type, rt.amenities " +
                "FROM rooms r " +
                "JOIN room_types rt ON r.room_type_id = rt.id " +
                "WHERE r.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRoom(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Room> findByIdForUpdate(Connection conn, Long id) throws SQLException {
    String sql = "SELECT r.*, rt.code as rt_code, rt.name as rt_name, " +
            "rt.base_price, rt.max_capacity, rt.bed_type, rt.amenities " +
            "FROM rooms r " +
            "JOIN room_types rt ON r.room_type_id = rt.id " +
            "WHERE r.id = ? FOR UPDATE";

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setLong(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapResultSetToRoom(rs));
            }
        }
    }

    return Optional.empty();
}

    public Optional<Room> findByRoomNumber(String roomNumber) throws SQLException {
        String sql = "SELECT r.*, rt.code as rt_code, rt.name as rt_name, rt.base_price, rt.max_capacity, rt.bed_type, rt.amenities " +
                "FROM rooms r " +
                "JOIN room_types rt ON r.room_type_id = rt.id " +
                "WHERE r.room_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRoom(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Room> findAll() throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.*, rt.code as rt_code, rt.name as rt_name, rt.base_price, rt.max_capacity, rt.bed_type, rt.amenities " +
                "FROM rooms r " +
                "JOIN room_types rt ON r.room_type_id = rt.id " +
                "ORDER BY r.floor_number ASC, r.room_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToRoom(rs));
            }
        }
        return list;
    }

    public List<Room> findByStatus(RoomStatus status) throws SQLException {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.*, rt.code as rt_code, rt.name as rt_name, rt.base_price, rt.max_capacity, rt.bed_type, rt.amenities " +
                "FROM rooms r " +
                "JOIN room_types rt ON r.room_type_id = rt.id " +
                "WHERE r.status = ? ORDER BY r.room_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRoom(rs));
                }
            }
        }
        return list;
    }

    /**
     * Finds rooms that are bookable and NOT reserved during [checkIn, checkOut).
     * Strictly verifies against active/confirmed reservations.
     */
    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, Long roomTypeId, Integer minCapacity) throws SQLException {
        List<Room> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.*, rt.code as rt_code, rt.name as rt_name, rt.base_price, rt.max_capacity, rt.bed_type, rt.amenities ");
        sql.append("FROM rooms r ");
        sql.append("JOIN room_types rt ON r.room_type_id = rt.id ");
        sql.append("WHERE (r.status IN ('AVAILABLE', 'INSPECTED') OR (r.status = 'OCCUPIED' AND ? > CURDATE())) ");
        sql.append("AND rt.is_active = TRUE ");
        // Exclude rooms blocked by active maintenance tickets
sql.append("AND r.id NOT IN (");
sql.append("   SELECT mt.room_id FROM maintenance_tickets mt ");
sql.append("   WHERE mt.status IN ('OPEN', 'IN_PROGRESS') ");
sql.append("   AND mt.room_made_unavailable = TRUE ");
sql.append(") ");

        // Crucial: check for overlapping reservations
        sql.append("AND r.id NOT IN (");
        sql.append("   SELECT res.room_id FROM reservations res ");
        sql.append("   WHERE res.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN') ");
        sql.append("   AND res.check_in_date < ? ");
        sql.append("   AND res.check_out_date > ? ");
        sql.append(") ");

        if (roomTypeId != null) {
            sql.append("AND r.room_type_id = ? ");
        }
        if (minCapacity != null && minCapacity > 0) {
            sql.append("AND rt.max_capacity >= ? ");
        }
        sql.append("ORDER BY rt.base_price ASC, r.room_number ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setDate(1, Date.valueOf(checkIn));
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));

            int paramIndex = 4;
            if (roomTypeId != null) {
                ps.setLong(paramIndex++, roomTypeId);
            }
            if (minCapacity != null && minCapacity > 0) {
                ps.setInt(paramIndex++, minCapacity);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRoom(rs));
                }
            }
        }
        return list;
    }

    public Long insert(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, room_type_id, floor_number, status, notes) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getRoomNumber());
            ps.setLong(2, room.getRoomTypeId());
            ps.setInt(3, room.getFloorNumber());
            ps.setString(4, room.getStatus().name());
            ps.setString(5, room.getNotes());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    room.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public boolean update(Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_number = ?, room_type_id = ?, floor_number = ?, status = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setLong(2, room.getRoomTypeId());
            ps.setInt(3, room.getFloorNumber());
            ps.setString(4, room.getStatus().name());
            ps.setString(5, room.getNotes());
            ps.setLong(6, room.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(Long roomId, RoomStatus status) throws SQLException {
        String sql = "UPDATE rooms SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatusWithConnection(Connection conn, Long roomId, RoomStatus status) throws SQLException {
        String sql = "UPDATE rooms SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, roomId);
            return ps.executeUpdate() > 0;
        }
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setId(rs.getLong("id"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setRoomTypeId(rs.getLong("room_type_id"));
        r.setFloorNumber(rs.getInt("floor_number"));
        r.setStatus(RoomStatus.valueOf(rs.getString("status")));
        r.setNotes(rs.getString("notes"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) r.setCreatedAt(created.toLocalDateTime());

        // Joined room type
        RoomType rt = new RoomType();
        rt.setId(r.getRoomTypeId());
        rt.setCode(rs.getString("rt_code"));
        rt.setName(rs.getString("rt_name"));
        rt.setBasePrice(rs.getBigDecimal("base_price"));
        rt.setMaxCapacity(rs.getInt("max_capacity"));
        rt.setBedType(rs.getString("bed_type"));
        rt.setAmenities(rs.getString("amenities"));
        r.setRoomType(rt);

        return r;
    }
}
