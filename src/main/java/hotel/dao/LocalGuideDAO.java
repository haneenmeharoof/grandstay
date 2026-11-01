package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.ItineraryItem;
import hotel.model.LocalGuidePlace;
import hotel.model.TripItinerary;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocalGuideDAO {

    public List<LocalGuidePlace> findAllActive() throws SQLException {
        List<LocalGuidePlace> list = new ArrayList<>();
        String sql = "SELECT * FROM local_guide_places WHERE is_active = TRUE ORDER BY category ASC, name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToPlace(rs));
            }
        }
        return list;
    }

    public List<LocalGuidePlace> search(String query) throws SQLException {
        List<LocalGuidePlace> list = new ArrayList<>();
        String sql = "SELECT * FROM local_guide_places WHERE is_active = TRUE AND " +
                "(name LIKE ? OR category LIKE ? OR description LIKE ?) ORDER BY name ASC";
        String pattern = "%" + (query != null ? query.trim() : "") + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPlace(rs));
                }
            }
        }
        return list;
    }

    public Long insertPlace(LocalGuidePlace p) throws SQLException {
        String sql = "INSERT INTO local_guide_places (name, category, description, address, opening_hours, " +
                "estimated_duration_minutes, estimated_cost, recommended_tips, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setString(3, p.getDescription());
            ps.setString(4, p.getAddress());
            ps.setString(5, p.getOpeningHours());
            ps.setInt(6, p.getEstimatedDurationMinutes());
            ps.setBigDecimal(7, p.getEstimatedCost());
            ps.setString(8, p.getRecommendedTips());
            ps.setBoolean(9, p.isActive());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    p.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public List<TripItinerary> findItinerariesByUserId(Long userId) throws SQLException {
        List<TripItinerary> list = new ArrayList<>();
        String sql = "SELECT * FROM trip_itineraries WHERE user_id = ? ORDER BY start_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TripItinerary ti = new TripItinerary();
                    ti.setId(rs.getLong("id"));
                    ti.setUserId(rs.getLong("user_id"));
                    ti.setTitle(rs.getString("title"));
                    ti.setStartDate(rs.getDate("start_date").toLocalDate());
                    ti.setEndDate(rs.getDate("end_date").toLocalDate());
                    ti.setNotes(rs.getString("notes"));
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) ti.setCreatedAt(created.toLocalDateTime());
                    ti.setItems(findItineraryItems(conn, ti.getId()));
                    list.add(ti);
                }
            }
        }
        return list;
    }

    public Long createItinerary(TripItinerary ti) throws SQLException {
        String sql = "INSERT INTO trip_itineraries (user_id, title, start_date, end_date, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, ti.getUserId());
            ps.setString(2, ti.getTitle());
            ps.setDate(3, Date.valueOf(ti.getStartDate()));
            ps.setDate(4, Date.valueOf(ti.getEndDate()));
            ps.setString(5, ti.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    ti.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public Long addItineraryItem(ItineraryItem item) throws SQLException {
        String sql = "INSERT INTO itinerary_items (itinerary_id, place_id, activity_name, planned_date, planned_time, sequence_order, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, item.getItineraryId());
            if (item.getPlaceId() != null) {
                ps.setLong(2, item.getPlaceId());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setString(3, item.getActivityName());
            ps.setDate(4, Date.valueOf(item.getPlannedDate()));
            ps.setString(5, item.getPlannedTime());
            ps.setInt(6, item.getSequenceOrder());
            ps.setString(7, item.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    item.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    private List<ItineraryItem> findItineraryItems(Connection conn, Long itineraryId) throws SQLException {
        List<ItineraryItem> items = new ArrayList<>();
        String sql = "SELECT ii.*, p.name as place_name " +
                "FROM itinerary_items ii " +
                "LEFT JOIN local_guide_places p ON ii.place_id = p.id " +
                "WHERE ii.itinerary_id = ? ORDER BY ii.planned_date ASC, ii.sequence_order ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, itineraryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItineraryItem ii = new ItineraryItem();
                    ii.setId(rs.getLong("id"));
                    ii.setItineraryId(rs.getLong("itinerary_id"));
                    long pId = rs.getLong("place_id");
                    if (!rs.wasNull()) ii.setPlaceId(pId);
                    ii.setPlaceName(rs.getString("place_name"));
                    ii.setActivityName(rs.getString("activity_name"));
                    ii.setPlannedDate(rs.getDate("planned_date").toLocalDate());
                    ii.setPlannedTime(rs.getString("planned_time"));
                    ii.setSequenceOrder(rs.getInt("sequence_order"));
                    ii.setNotes(rs.getString("notes"));
                    items.add(ii);
                }
            }
        }
        return items;
    }

    private LocalGuidePlace mapResultSetToPlace(ResultSet rs) throws SQLException {
        LocalGuidePlace p = new LocalGuidePlace();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setCategory(rs.getString("category"));
        p.setDescription(rs.getString("description"));
        p.setAddress(rs.getString("address"));
        p.setOpeningHours(rs.getString("opening_hours"));
        p.setEstimatedDurationMinutes(rs.getInt("estimated_duration_minutes"));
        p.setEstimatedCost(rs.getBigDecimal("estimated_cost"));
        p.setRecommendedTips(rs.getString("recommended_tips"));
        p.setActive(rs.getBoolean("is_active"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) p.setCreatedAt(created.toLocalDateTime());
        return p;
    }
}
