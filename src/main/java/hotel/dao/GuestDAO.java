package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.Guest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuestDAO {

    public Optional<Guest> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM guests WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuest(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Guest> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM guests WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuest(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Guest> search(String query) throws SQLException {
        List<Guest> list = new ArrayList<>();
        String sql = "SELECT * FROM guests WHERE first_name LIKE ? OR last_name LIKE ? OR email LIKE ? OR phone LIKE ? ORDER BY last_name ASC LIMIT 100";
        String pattern = "%" + (query != null ? query.trim() : "") + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGuest(rs));
                }
            }
        }
        return list;
    }

    public Long insert(Guest guest) throws SQLException {
        String sql = "INSERT INTO guests (first_name, last_name, email, phone, address, id_type, id_number, nationality, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, guest.getFirstName());
            ps.setString(2, guest.getLastName());
            ps.setString(3, guest.getEmail());
            ps.setString(4, guest.getPhone());
            ps.setString(5, guest.getAddress());
            ps.setString(6, guest.getIdType());
            ps.setString(7, guest.getIdNumber());
            ps.setString(8, guest.getNationality());
            ps.setString(9, guest.getNotes());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    guest.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public boolean update(Guest guest) throws SQLException {
        String sql = "UPDATE guests SET first_name = ?, last_name = ?, email = ?, phone = ?, address = ?, " +
                "id_type = ?, id_number = ?, nationality = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, guest.getFirstName());
            ps.setString(2, guest.getLastName());
            ps.setString(3, guest.getEmail());
            ps.setString(4, guest.getPhone());
            ps.setString(5, guest.getAddress());
            ps.setString(6, guest.getIdType());
            ps.setString(7, guest.getIdNumber());
            ps.setString(8, guest.getNationality());
            ps.setString(9, guest.getNotes());
            ps.setLong(10, guest.getId());
            return ps.executeUpdate() > 0;
        }
    }

    private Guest mapResultSetToGuest(ResultSet rs) throws SQLException {
        Guest g = new Guest();
        g.setId(rs.getLong("id"));
        g.setFirstName(rs.getString("first_name"));
        g.setLastName(rs.getString("last_name"));
        g.setEmail(rs.getString("email"));
        g.setPhone(rs.getString("phone"));
        g.setAddress(rs.getString("address"));
        g.setIdType(rs.getString("id_type"));
        g.setIdNumber(rs.getString("id_number"));
        g.setNationality(rs.getString("nationality"));
        g.setNotes(rs.getString("notes"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) g.setCreatedAt(created.toLocalDateTime());
        return g;
    }
}
