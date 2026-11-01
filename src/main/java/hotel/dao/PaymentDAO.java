package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.Payment;
import hotel.model.PaymentMethod;
import hotel.model.PaymentStatus;
import hotel.model.Refund;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentDAO {

    public Optional<Payment> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM payments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Payment> findByReservationId(Long reservationId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE reservation_id = ? ORDER BY payment_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPayment(rs));
                }
            }
        }
        return list;
    }

    public Long insertWithConnection(Connection conn, Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (payment_reference, reservation_id, invoice_id, amount, payment_method, " +
                "payment_status, transaction_notes, processed_by_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, payment.getPaymentReference());
            ps.setLong(2, payment.getReservationId());
            if (payment.getInvoiceId() != null) {
                ps.setLong(3, payment.getInvoiceId());
            } else {
                ps.setNull(3, java.sql.Types.BIGINT);
            }
            ps.setBigDecimal(4, payment.getAmount());
            ps.setString(5, payment.getPaymentMethod().name());
            ps.setString(6, payment.getPaymentStatus().name());
            ps.setString(7, payment.getTransactionNotes());
            if (payment.getProcessedByUserId() != null) {
                ps.setLong(8, payment.getProcessedByUserId());
            } else {
                ps.setNull(8, java.sql.Types.BIGINT);
            }

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    payment.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public Long insertRefundWithConnection(Connection conn, Refund refund) throws SQLException {
        String sql = "INSERT INTO refunds (refund_reference, payment_id, reservation_id, amount, reason, status, processed_by_user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, refund.getRefundReference());
            if (refund.getPaymentId() != null) {
                ps.setLong(2, refund.getPaymentId());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setLong(3, refund.getReservationId());
            ps.setBigDecimal(4, refund.getAmount());
            ps.setString(5, refund.getReason());
            ps.setString(6, refund.getStatus().name());
            if (refund.getProcessedByUserId() != null) {
                ps.setLong(7, refund.getProcessedByUserId());
            } else {
                ps.setNull(7, java.sql.Types.BIGINT);
            }

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    refund.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public List<Refund> findRefundsByReservationId(Long reservationId) throws SQLException {
        List<Refund> list = new ArrayList<>();
        String sql = "SELECT * FROM refunds WHERE reservation_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Refund ref = new Refund();
                    ref.setId(rs.getLong("id"));
                    ref.setRefundReference(rs.getString("refund_reference"));
                    long payId = rs.getLong("payment_id");
                    if (!rs.wasNull()) ref.setPaymentId(payId);
                    ref.setReservationId(rs.getLong("reservation_id"));
                    ref.setAmount(rs.getBigDecimal("amount"));
                    ref.setReason(rs.getString("reason"));
                    ref.setStatus(Refund.RefundStatus.valueOf(rs.getString("status")));
                    long pUserId = rs.getLong("processed_by_user_id");
                    if (!rs.wasNull()) ref.setProcessedByUserId(pUserId);
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) ref.setCreatedAt(created.toLocalDateTime());
                    list.add(ref);
                }
            }
        }
        return list;
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getLong("id"));
        p.setPaymentReference(rs.getString("payment_reference"));
        p.setReservationId(rs.getLong("reservation_id"));
        long invId = rs.getLong("invoice_id");
        if (!rs.wasNull()) p.setInvoiceId(invId);
        p.setAmount(rs.getBigDecimal("amount"));
        p.setPaymentMethod(PaymentMethod.valueOf(rs.getString("payment_method")));
        p.setPaymentStatus(PaymentStatus.valueOf(rs.getString("payment_status")));
        p.setTransactionNotes(rs.getString("transaction_notes"));
        long processedBy = rs.getLong("processed_by_user_id");
        if (!rs.wasNull()) p.setProcessedByUserId(processedBy);
        Timestamp date = rs.getTimestamp("payment_date");
        if (date != null) p.setPaymentDate(date.toLocalDateTime());
        return p;
    }
}
