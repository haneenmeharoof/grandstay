package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.PreCheckin;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PreCheckinDAO {

    public Optional<PreCheckin> findByReservationId(Long reservationId) throws SQLException {
        String sql = basePreCheckinSelect() + " WHERE pc.reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPreCheckin(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<PreCheckin> findById(Long preCheckinId) throws SQLException {
        String sql = basePreCheckinSelect() + " WHERE pc.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, preCheckinId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToPreCheckin(rs));
            }
        }
        return Optional.empty();
    }

    public List<PreCheckin> findPendingReview() throws SQLException {
        List<PreCheckin> list = new ArrayList<>();
        String sql = basePreCheckinSelect() + " WHERE pc.status = 'SUBMITTED' ORDER BY pc.created_at ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToPreCheckin(rs));
            }
        }
        return list;
    }

    public List<PreCheckin> findAll() throws SQLException {
        List<PreCheckin> list = new ArrayList<>();
        String sql = basePreCheckinSelect() + " ORDER BY pc.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToPreCheckin(rs));
            }
        }
        return list;
    }

    public Long insert(PreCheckin pc) throws SQLException {
        String sql = "INSERT INTO pre_checkins (reservation_id, user_id, expected_arrival_time, id_document_type, " +
                "id_document_number, special_requests, emergency_contact_name, emergency_contact_phone, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, pc.getReservationId());
            ps.setLong(2, pc.getUserId());
            ps.setString(3, pc.getExpectedArrivalTime());
            ps.setString(4, pc.getIdDocumentType());
            ps.setString(5, pc.getIdDocumentNumber());
            ps.setString(6, pc.getSpecialRequests());
            ps.setString(7, pc.getEmergencyContactName());
            ps.setString(8, pc.getEmergencyContactPhone());
            ps.setString(9, pc.getStatus().name());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    pc.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public boolean updateStatus(Long preCheckinId, PreCheckin.Status status, String staffNotes, Long reviewedByUserId) throws SQLException {
        String sql = "UPDATE pre_checkins SET status = ?, staff_notes = ?, reviewed_by_user_id = ?, reviewed_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, staffNotes);
            ps.setLong(3, reviewedByUserId);
            ps.setLong(4, preCheckinId);
            return ps.executeUpdate() > 0;
        }
    }

    private String basePreCheckinSelect() {
        return "SELECT pc.*, res.reference_number as res_ref " +
                "FROM pre_checkins pc " +
                "JOIN reservations res ON pc.reservation_id = res.id ";
    }

    private PreCheckin mapResultSetToPreCheckin(ResultSet rs) throws SQLException {
        PreCheckin pc = new PreCheckin();
        pc.setId(rs.getLong("id"));
        pc.setReservationId(rs.getLong("reservation_id"));
        pc.setReservationRef(rs.getString("res_ref"));
        pc.setUserId(rs.getLong("user_id"));
        pc.setExpectedArrivalTime(rs.getString("expected_arrival_time"));
        pc.setIdDocumentType(rs.getString("id_document_type"));
        pc.setIdDocumentNumber(rs.getString("id_document_number"));
        pc.setSpecialRequests(rs.getString("special_requests"));
        pc.setEmergencyContactName(rs.getString("emergency_contact_name"));
        pc.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
        pc.setStatus(PreCheckin.Status.valueOf(rs.getString("status")));
        pc.setStaffNotes(rs.getString("staff_notes"));
        long rUid = rs.getLong("reviewed_by_user_id");
        if (!rs.wasNull()) pc.setReviewedByUserId(rUid);
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) pc.setCreatedAt(created.toLocalDateTime());
        Timestamp rev = rs.getTimestamp("reviewed_at");
        if (rev != null) pc.setReviewedAt(rev.toLocalDateTime());
        return pc;
    }
}
