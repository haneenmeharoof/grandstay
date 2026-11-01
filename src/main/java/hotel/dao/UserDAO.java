package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.Role;
import hotel.model.User;
import hotel.model.UserStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    public Optional<User> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByUsernameOrEmail(String identifier) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? OR email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<User> findAll() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        }
        return list;
    }

    public List<User> findByRole(Role role) throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY full_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        }
        return list;
    }

    public Long insert(User user) throws SQLException {
        String sql = "INSERT INTO users (guest_id, username, email, password_hash, full_name, phone, role, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (user.getGuestId() != null) {
                ps.setLong(1, user.getGuestId());
            } else {
                ps.setNull(1, java.sql.Types.BIGINT);
            }
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getFullName());
            ps.setString(6, user.getPhone());
            ps.setString(7, user.getRole().name());
            ps.setString(8, user.getStatus().name());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long generatedId = rs.getLong(1);
                    user.setId(generatedId);
                    return generatedId;
                }
            }
        }
        return null;
    }

    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, phone = ?, role = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPhone());
            ps.setString(3, user.getRole().name());
            ps.setString(4, user.getStatus().name());
            ps.setLong(5, user.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(Long userId, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(Long userId, UserStatus status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        long guestId = rs.getLong("guest_id");
        if (!rs.wasNull()) {
            u.setGuestId(guestId);
        }
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        u.setRole(Role.valueOf(rs.getString("role")));
        u.setStatus(UserStatus.valueOf(rs.getString("status")));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) u.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) u.setUpdatedAt(updated.toLocalDateTime());
        return u;
    }
}
