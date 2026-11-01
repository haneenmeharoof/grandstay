package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.ServiceOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServiceOrderDAO {

    public Optional<ServiceOrder> findById(Long id) throws SQLException {
        String sql = baseOrderSelect() + " WHERE so.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOrder(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<ServiceOrder> findByReservationId(Long reservationId) throws SQLException {
        List<ServiceOrder> list = new ArrayList<>();
        String sql = baseOrderSelect() + " WHERE so.reservation_id = ? ORDER BY so.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
            }
        }
        return list;
    }

    /** Returns only service orders belonging to the specified guest. */
    public List<ServiceOrder> findByGuestId(Long guestId) throws SQLException {
        List<ServiceOrder> list = new ArrayList<>();
        String sql = baseOrderSelect() + " WHERE so.guest_id = ? ORDER BY so.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
            }
        }
        return list;
    }

    public List<ServiceOrder> findAll() throws SQLException {
        List<ServiceOrder> list = new ArrayList<>();
        String sql = baseOrderSelect() + " ORDER BY so.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToOrder(rs));
            }
        }
        return list;
    }

    public Long insert(ServiceOrder order) throws SQLException {
        String sql = "INSERT INTO service_orders (order_number, reservation_id, guest_id, category, description, " +
                "special_instructions, status, charge_amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, order.getOrderNumber());
            ps.setLong(2, order.getReservationId());
            ps.setLong(3, order.getGuestId());
            ps.setString(4, order.getCategory().name());
            ps.setString(5, order.getDescription());
            ps.setString(6, order.getSpecialInstructions());
            ps.setString(7, order.getStatus().name());
            ps.setBigDecimal(8, order.getChargeAmount());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    order.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public boolean updateStatus(Long orderId, ServiceOrder.Status status, Long staffUserId) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE service_orders SET status = ? ");
        if (staffUserId != null) {
            sql.append(", assigned_to_user_id = ? ");
        }
        if (status == ServiceOrder.Status.COMPLETED) {
            sql.append(", completed_at = CURRENT_TIMESTAMP ");
        }
        sql.append("WHERE id = ?");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setString(1, status.name());
            int idx = 2;
            if (staffUserId != null) {
                ps.setLong(idx++, staffUserId);
            }
            ps.setLong(idx, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    private String baseOrderSelect() {
        return "SELECT so.*, res.reference_number as res_ref, " +
                "CONCAT(g.first_name, ' ', g.last_name) as guest_full_name, " +
                "u.full_name as assigned_staff_name " +
                "FROM service_orders so " +
                "JOIN reservations res ON so.reservation_id = res.id " +
                "JOIN guests g ON so.guest_id = g.id " +
                "LEFT JOIN users u ON so.assigned_to_user_id = u.id ";
    }

    private ServiceOrder mapResultSetToOrder(ResultSet rs) throws SQLException {
        ServiceOrder so = new ServiceOrder();
        so.setId(rs.getLong("id"));
        so.setOrderNumber(rs.getString("order_number"));
        so.setReservationId(rs.getLong("reservation_id"));
        so.setReservationRef(rs.getString("res_ref"));
        so.setGuestId(rs.getLong("guest_id"));
        so.setGuestName(rs.getString("guest_full_name"));
        so.setCategory(ServiceOrder.Category.valueOf(rs.getString("category")));
        so.setDescription(rs.getString("description"));
        so.setSpecialInstructions(rs.getString("special_instructions"));
        so.setStatus(ServiceOrder.Status.valueOf(rs.getString("status")));
        long assId = rs.getLong("assigned_to_user_id");
        if (!rs.wasNull()) so.setAssignedToUserId(assId);
        so.setAssignedStaffName(rs.getString("assigned_staff_name"));
        so.setChargeAmount(rs.getBigDecimal("charge_amount"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) so.setCreatedAt(created.toLocalDateTime());
        Timestamp comp = rs.getTimestamp("completed_at");
        if (comp != null) so.setCompletedAt(comp.toLocalDateTime());
        return so;
    }
}
