package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.Feedback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FeedbackDAO {

    public List<Feedback> findApproved() throws SQLException {
        List<Feedback> list = new ArrayList<>();
        String sql = baseFeedbackSelect() + " WHERE f.moderation_status = 'APPROVED' ORDER BY f.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToFeedback(rs));
            }
        }
        return list;
    }

    public List<Feedback> findAll() throws SQLException {
        List<Feedback> list = new ArrayList<>();
        String sql = baseFeedbackSelect() + " ORDER BY f.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToFeedback(rs));
            }
        }
        return list;
    }

    public Optional<Feedback> findByReservationId(Long reservationId) throws SQLException {
        String sql = baseFeedbackSelect() + " WHERE f.reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToFeedback(rs));
                }
            }
        }
        return Optional.empty();
    }

    public double getAverageApprovedRating() throws SQLException {
        String sql = "SELECT AVG(rating) FROM feedback WHERE moderation_status = 'APPROVED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 5.0;
    }

    public Long insert(Feedback feedback) throws SQLException {
        String sql = "INSERT INTO feedback (reservation_id, guest_id, user_id, rating, review_title, review_text, moderation_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, feedback.getReservationId());
            ps.setLong(2, feedback.getGuestId());
            if (feedback.getUserId() != null) {
                ps.setLong(3, feedback.getUserId());
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
            }
            ps.setInt(4, feedback.getRating());
            ps.setString(5, feedback.getReviewTitle());
            ps.setString(6, feedback.getReviewText());
            ps.setString(7, feedback.getModerationStatus().name());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    feedback.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public boolean updateModeration(Long feedbackId, Feedback.ModerationStatus status, Long adminUserId) throws SQLException {
        String sql = "UPDATE feedback SET moderation_status = ?, moderated_by_user_id = ?, moderated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, adminUserId);
            ps.setLong(3, feedbackId);
            return ps.executeUpdate() > 0;
        }
    }

    private String baseFeedbackSelect() {
        return "SELECT f.*, res.reference_number as res_ref, " +
                "CONCAT(g.first_name, ' ', g.last_name) as guest_full_name " +
                "FROM feedback f " +
                "JOIN reservations res ON f.reservation_id = res.id " +
                "JOIN guests g ON f.guest_id = g.id ";
    }

    private Feedback mapResultSetToFeedback(ResultSet rs) throws SQLException {
        Feedback f = new Feedback();
        f.setId(rs.getLong("id"));
        f.setReservationId(rs.getLong("reservation_id"));
        f.setReservationRef(rs.getString("res_ref"));
        f.setGuestId(rs.getLong("guest_id"));
        f.setGuestName(rs.getString("guest_full_name"));
        long uId = rs.getLong("user_id");
        if (!rs.wasNull()) f.setUserId(uId);
        f.setRating(rs.getInt("rating"));
        f.setReviewTitle(rs.getString("review_title"));
        f.setReviewText(rs.getString("review_text"));
        f.setModerationStatus(Feedback.ModerationStatus.valueOf(rs.getString("moderation_status")));
        long mId = rs.getLong("moderated_by_user_id");
        if (!rs.wasNull()) f.setModeratedByUserId(mId);
        Timestamp mod = rs.getTimestamp("moderated_at");
        if (mod != null) f.setModeratedAt(mod.toLocalDateTime());
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) f.setCreatedAt(created.toLocalDateTime());
        return f;
    }
}
