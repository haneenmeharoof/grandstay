package hotel.dao;

import hotel.model.TripItinerary;
import hotel.model.ItineraryItem;
import hotel.database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TripItineraryDAO {

    public Long create(TripItinerary itinerary) throws SQLException {
        String sql = """
            INSERT INTO trip_itineraries
            (user_id, title, start_date, end_date, notes)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, itinerary.getUserId());
            ps.setString(2, itinerary.getTitle());
            ps.setDate(3, Date.valueOf(itinerary.getStartDate()));
            ps.setDate(4, Date.valueOf(itinerary.getEndDate()));
            ps.setString(5, itinerary.getNotes());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    Long id = rs.getLong(1);
                    itinerary.setId(id);
                    return id;
                }
            }
        }

        throw new SQLException("Creating itinerary failed.");
    }

    public List<TripItinerary> findByUserId(Long userId)
            throws SQLException {

        String sql = """
            SELECT * FROM trip_itineraries
            WHERE user_id = ?
            ORDER BY start_date DESC
            """;

        List<TripItinerary> itineraries = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    itineraries.add(mapItinerary(rs));
                }
            }
        }

        return itineraries;
    }

    public Optional<TripItinerary> findById(Long itineraryId, Long userId)
            throws SQLException {

        String sql = """
            SELECT * FROM trip_itineraries
            WHERE id = ? AND user_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, itineraryId);
            ps.setLong(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapItinerary(rs));
                }
            }
        }

        return Optional.empty();
    }

    public void addItem(ItineraryItem item) throws SQLException {
        String sql = """
            INSERT INTO itinerary_items
            (itinerary_id, place_id, activity_name, planned_date,
             planned_time, sequence_order, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, item.getItineraryId());

            if (item.getPlaceId() == null) {
                ps.setNull(2, Types.BIGINT);
            } else {
                ps.setLong(2, item.getPlaceId());
            }

            ps.setString(3, item.getActivityName());
            ps.setDate(4, Date.valueOf(item.getPlannedDate()));
            ps.setString(5, item.getPlannedTime());
            ps.setInt(6, item.getSequenceOrder());
            ps.setString(7, item.getNotes());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    item.setId(rs.getLong(1));
                }
            }
        }
    }

    public List<ItineraryItem> findItems(Long itineraryId)
            throws SQLException {

        String sql = """
            SELECT i.*, p.name AS place_name
            FROM itinerary_items i
            LEFT JOIN local_guide_places p ON i.place_id = p.id
            WHERE i.itinerary_id = ?
            ORDER BY i.planned_date, i.sequence_order
            """;

        List<ItineraryItem> items = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, itineraryId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItineraryItem item = new ItineraryItem();

                    item.setId(rs.getLong("id"));
                    item.setItineraryId(rs.getLong("itinerary_id"));

                    long placeId = rs.getLong("place_id");
                    item.setPlaceId(rs.wasNull() ? null : placeId);

                    item.setPlaceName(rs.getString("place_name"));
                    item.setActivityName(rs.getString("activity_name"));
                    item.setPlannedDate(
                            rs.getDate("planned_date").toLocalDate());
                    item.setPlannedTime(rs.getString("planned_time"));
                    item.setSequenceOrder(rs.getInt("sequence_order"));
                    item.setNotes(rs.getString("notes"));

                    items.add(item);
                }
            }
        }

        return items;
    }

    public boolean delete(Long itineraryId, Long userId)
            throws SQLException {

        String sql = """
            DELETE FROM trip_itineraries
            WHERE id = ? AND user_id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, itineraryId);
            ps.setLong(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    private TripItinerary mapItinerary(ResultSet rs)
            throws SQLException {

        TripItinerary itinerary = new TripItinerary();

        itinerary.setId(rs.getLong("id"));
        itinerary.setUserId(rs.getLong("user_id"));
        itinerary.setTitle(rs.getString("title"));
        itinerary.setStartDate(
                rs.getDate("start_date").toLocalDate());
        itinerary.setEndDate(
                rs.getDate("end_date").toLocalDate());
        itinerary.setNotes(rs.getString("notes"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            itinerary.setCreatedAt(createdAt.toLocalDateTime());
        }

        return itinerary;
    }
}
