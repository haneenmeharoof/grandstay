package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.Guest;
import hotel.model.Reservation;
import hotel.model.ReservationStatus;
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

public class ReservationDAO {

    public Optional<Reservation> findById(Long id) throws SQLException {
        String sql = baseReservationSelect() + " WHERE res.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReservation(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Reservation> findByReferenceNumber(String refNumber) throws SQLException {
        String sql = baseReservationSelect() + " WHERE res.reference_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, refNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReservation(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Reservation> findByUserId(Long userId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = baseReservationSelect() + " WHERE res.user_id = ? ORDER BY res.check_in_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReservation(rs));
                }
            }
        }
        return list;
    }

    public List<Reservation> findByGuestId(Long guestId) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = baseReservationSelect() + " WHERE res.guest_id = ? ORDER BY res.check_in_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReservation(rs));
                }
            }
        }
        return list;
    }

    public List<Reservation> search(String query) throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = baseReservationSelect() +
                " WHERE res.reference_number LIKE ? " +
                "    OR g.first_name LIKE ? " +
                "    OR g.last_name LIKE ? " +
                "    OR g.email LIKE ? " +
                "    OR r.room_number LIKE ? " +
                " ORDER BY res.check_in_date DESC LIMIT 150";
        String pattern = "%" + (query != null ? query.trim() : "") + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReservation(rs));
                }
            }
        }
        return list;
    }

    public List<Reservation> findTodayArrivals() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = baseReservationSelect() +
                " WHERE res.check_in_date = CURDATE() " +
                "AND res.status NOT IN ('CANCELLED', 'NO_SHOW') " +
                "AND NOT EXISTS (SELECT 1 FROM pre_checkins pc " +
                "WHERE pc.reservation_id = res.id AND pc.status = 'SUBMITTED') " +
                " ORDER BY g.last_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToReservation(rs));
            }
        }
        return list;
    }

    public List<Reservation> findTodayDepartures() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = baseReservationSelect() +
                " WHERE res.check_out_date = CURDATE() AND res.status NOT IN ('CANCELLED', 'NO_SHOW') " +
                " ORDER BY r.room_number ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToReservation(rs));
            }
        }
        return list;
    }

    /** True when a future confirmed/pending stay protects this room from walk-in allocation. */
    public boolean hasUpcomingReservationForRoom(Long roomId) throws SQLException {
    String sql = "SELECT COUNT(*) FROM reservations " +
            "WHERE room_id = ? " +
            "AND status IN ('PENDING', 'CONFIRMED') " +
            "AND check_out_date > CURDATE()";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setLong(1, roomId);

        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}

    public boolean hasCheckedInReservationForRoom(Long roomId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE room_id = ? AND status = 'CHECKED_IN'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<Reservation> findAllActive() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = baseReservationSelect() +
                " WHERE res.status IN ('CONFIRMED', 'CHECKED_IN', 'PENDING') " +
                " ORDER BY res.check_in_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToReservation(rs));
            }
        }
        return list;
    }

    /**
     * Checks if room has any active reservation overlapping [checkIn, checkOut).
     * Excludes a specific reservationId if modifying an existing one.
     */
    public boolean hasOverlappingReservation(Long roomId, LocalDate checkIn, LocalDate checkOut, Long excludeReservationId) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM reservations ");
        sql.append("WHERE room_id = ? ");
        sql.append("AND status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN') ");
        sql.append("AND check_in_date < ? ");
        sql.append("AND check_out_date > ? ");

        if (excludeReservationId != null) {
            sql.append("AND id != ? ");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setLong(1, roomId);
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));
            if (excludeReservationId != null) {
                ps.setLong(4, excludeReservationId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean hasOverlappingReservation(
        Connection conn,
        Long roomId,
        LocalDate checkIn,
        LocalDate checkOut,
        Long excludeReservationId
) throws SQLException {

    StringBuilder sql = new StringBuilder();
    sql.append("SELECT COUNT(*) FROM reservations ");
    sql.append("WHERE room_id = ? ");
    sql.append("AND status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN') ");
    sql.append("AND check_in_date < ? ");
    sql.append("AND check_out_date > ? ");

    if (excludeReservationId != null) {
        sql.append("AND id != ? ");
    }

    try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
        ps.setLong(1, roomId);
        ps.setDate(2, java.sql.Date.valueOf(checkOut));
        ps.setDate(3, java.sql.Date.valueOf(checkIn));

        if (excludeReservationId != null) {
            ps.setLong(4, excludeReservationId);
        }

        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }
}



    public Long insertWithConnection(Connection conn, Reservation res) throws SQLException {
        String sql = "INSERT INTO reservations (reference_number, guest_id, user_id, room_id, check_in_date, check_out_date, " +
                "number_of_guests, nights, room_rate, total_room_charge, tax_amount, service_fee, discount_amount, " +
                "total_amount, paid_amount, status, special_requests) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, res.getReferenceNumber());
            ps.setLong(2, res.getGuestId());
            if (res.getUserId() != null) {
                ps.setLong(3, res.getUserId());
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
            }
            ps.setLong(4, res.getRoomId());
            ps.setDate(5, Date.valueOf(res.getCheckInDate()));
            ps.setDate(6, Date.valueOf(res.getCheckOutDate()));
            ps.setInt(7, res.getNumberOfGuests());
            ps.setInt(8, res.getNights());
            ps.setBigDecimal(9, res.getRoomRate());
            ps.setBigDecimal(10, res.getTotalRoomCharge());
            ps.setBigDecimal(11, res.getTaxAmount());
            ps.setBigDecimal(12, res.getServiceFee());
            ps.setBigDecimal(13, res.getDiscountAmount());
            ps.setBigDecimal(14, res.getTotalAmount());
            ps.setBigDecimal(15, res.getPaidAmount());
            ps.setString(16, res.getStatus().name());
            ps.setString(17, res.getSpecialRequests());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    res.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public boolean updateStatus(Long reservationId, ReservationStatus status, String cancellationReason) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return updateStatusWithConnection(conn, reservationId, status, cancellationReason);
        }
    }

    public boolean updateStatusWithConnection(Connection conn, Long reservationId,
                                               ReservationStatus status, String cancellationReason) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE reservations SET status = ? ");
        if (status == ReservationStatus.CANCELLED) {
            sql.append(", cancellation_reason = ?, cancelled_at = CURRENT_TIMESTAMP ");
        }
        sql.append("WHERE id = ?");
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, status.name());
            int idx = 2;
            if (status == ReservationStatus.CANCELLED) ps.setString(idx++, cancellationReason);
            ps.setLong(idx, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePaidAmountWithConnection(Connection conn, Long reservationId, java.math.BigDecimal newPaidAmount) throws SQLException {
        String sql = "UPDATE reservations SET paid_amount = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newPaidAmount);
            ps.setLong(2, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateRoomAssignment(Long reservationId, Long newRoomId) throws SQLException {
        String sql = "UPDATE reservations SET room_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, newRoomId);
            ps.setLong(2, reservationId);
            return ps.executeUpdate() > 0;
        }
    }

    private String baseReservationSelect() {
        return "SELECT res.*, " +
                "g.first_name, g.last_name, g.email as guest_email, g.phone as guest_phone, g.address as guest_address, " +
                "r.room_number, r.floor_number, r.status as room_status, " +
                "rt.id as rt_id, rt.code as rt_code, rt.name as rt_name, rt.base_price, rt.max_capacity, rt.bed_type, rt.amenities " +
                "FROM reservations res " +
                "JOIN guests g ON res.guest_id = g.id " +
                "JOIN rooms r ON res.room_id = r.id " +
                "JOIN room_types rt ON r.room_type_id = rt.id ";
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation res = new Reservation();
        res.setId(rs.getLong("id"));
        res.setReferenceNumber(rs.getString("reference_number"));
        res.setGuestId(rs.getLong("guest_id"));
        long uid = rs.getLong("user_id");
        if (!rs.wasNull()) res.setUserId(uid);
        res.setRoomId(rs.getLong("room_id"));
        res.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        res.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        res.setNumberOfGuests(rs.getInt("number_of_guests"));
        res.setNights(rs.getInt("nights"));
        res.setRoomRate(rs.getBigDecimal("room_rate"));
        res.setTotalRoomCharge(rs.getBigDecimal("total_room_charge"));
        res.setTaxAmount(rs.getBigDecimal("tax_amount"));
        res.setServiceFee(rs.getBigDecimal("service_fee"));
        res.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        res.setTotalAmount(rs.getBigDecimal("total_amount"));
        res.setPaidAmount(rs.getBigDecimal("paid_amount"));
        res.setStatus(ReservationStatus.valueOf(rs.getString("status")));
        res.setCancellationReason(rs.getString("cancellation_reason"));
        Timestamp cancelledAt = rs.getTimestamp("cancelled_at");
        if (cancelledAt != null) res.setCancelledAt(cancelledAt.toLocalDateTime());
        res.setSpecialRequests(rs.getString("special_requests"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) res.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) res.setUpdatedAt(updated.toLocalDateTime());

        // Guest details
        Guest g = new Guest();
        g.setId(res.getGuestId());
        g.setFirstName(rs.getString("first_name"));
        g.setLastName(rs.getString("last_name"));
        g.setEmail(rs.getString("guest_email"));
        g.setPhone(rs.getString("guest_phone"));
        g.setAddress(rs.getString("guest_address"));
        res.setGuest(g);

        // Room & RoomType details
        Room r = new Room();
        r.setId(res.getRoomId());
        r.setRoomNumber(rs.getString("room_number"));
        r.setFloorNumber(rs.getInt("floor_number"));
        r.setStatus(RoomStatus.valueOf(rs.getString("room_status")));

        RoomType rt = new RoomType();
        rt.setId(rs.getLong("rt_id"));
        rt.setCode(rs.getString("rt_code"));
        rt.setName(rs.getString("rt_name"));
        rt.setBasePrice(rs.getBigDecimal("base_price"));
        rt.setMaxCapacity(rs.getInt("max_capacity"));
        rt.setBedType(rs.getString("bed_type"));
        rt.setAmenities(rs.getString("amenities"));
        r.setRoomType(rt);
        res.setRoom(r);

        return res;
    }
}
