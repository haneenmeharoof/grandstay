package hotel.dao;

import hotel.database.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO {

    public Map<String, Object> getOverviewStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Room counts
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM rooms");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("totalRooms", rs.next() ? rs.getInt(1) : 0);
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM rooms WHERE status IN ('AVAILABLE', 'INSPECTED')");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("availableRooms", rs.next() ? rs.getInt(1) : 0);
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM rooms WHERE status = 'OCCUPIED'");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("occupiedRooms", rs.next() ? rs.getInt(1) : 0);
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM rooms WHERE status IN ('DIRTY', 'CLEANING')");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("cleaningRooms", rs.next() ? rs.getInt(1) : 0);
            }

            // Today's Check-ins & Check-outs
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM reservations WHERE check_in_date = CURDATE() AND status IN ('CONFIRMED', 'PENDING', 'CHECKED_IN')");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("todayCheckIns", rs.next() ? rs.getInt(1) : 0);
            }
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM reservations WHERE check_out_date = CURDATE() AND status = 'CHECKED_IN'");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("todayCheckOuts", rs.next() ? rs.getInt(1) : 0);
            }

            // Active reservations
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM reservations WHERE status IN ('CONFIRMED', 'CHECKED_IN')");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("activeBookings", rs.next() ? rs.getInt(1) : 0);
            }

            // Total revenue from completed payments
            try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE payment_status = 'COMPLETED'");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("totalRevenue", rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO);
            }

            // Low stock items count
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM inventory_items WHERE quantity_on_hand <= min_stock_level AND is_active = TRUE");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("lowStockCount", rs.next() ? rs.getInt(1) : 0);
            }

            // Open maintenance tickets
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM maintenance_tickets WHERE status IN ('OPEN', 'IN_PROGRESS')");
                 ResultSet rs = ps.executeQuery()) {
                stats.put("openTicketsCount", rs.next() ? rs.getInt(1) : 0);
            }
        }
        return stats;
    }

    public List<List<String>> getRevenueReport(LocalDate start, LocalDate end) throws SQLException {
        List<List<String>> rows = new ArrayList<>();
        String sql = "SELECT DATE(payment_date) as pay_date, payment_method, COUNT(*) as tx_count, SUM(amount) as total_amount " +
                "FROM payments " +
                "WHERE payment_status = 'COMPLETED' AND DATE(payment_date) BETWEEN ? AND ? " +
                "GROUP BY DATE(payment_date), payment_method " +
                "ORDER BY pay_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    row.add(rs.getDate("pay_date").toString());
                    row.add(rs.getString("payment_method"));
                    row.add(String.valueOf(rs.getInt("tx_count")));
                    row.add(rs.getBigDecimal("total_amount").toPlainString());
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public List<List<String>> getOutstandingBalancesReport() throws SQLException {
        List<List<String>> rows = new ArrayList<>();
        String sql = "SELECT res.reference_number, CONCAT(g.first_name, ' ', g.last_name) as guest_name, " +
                "res.check_in_date, res.check_out_date, res.total_amount, res.paid_amount, " +
                "(res.total_amount - res.paid_amount) as balance_due, res.status " +
                "FROM reservations res " +
                "JOIN guests g ON res.guest_id = g.id " +
                "WHERE (res.total_amount - res.paid_amount) > 0.00 " +
                "ORDER BY balance_due DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                row.add(rs.getString("reference_number"));
                row.add(rs.getString("guest_name"));
                row.add(rs.getDate("check_in_date").toString());
                row.add(rs.getDate("check_out_date").toString());
                row.add(rs.getBigDecimal("total_amount").toPlainString());
                row.add(rs.getBigDecimal("paid_amount").toPlainString());
                row.add(rs.getBigDecimal("balance_due").toPlainString());
                row.add(rs.getString("status"));
                rows.add(row);
            }
        }
        return rows;
    }

    public List<List<String>> getOccupancySummary() throws SQLException {
        List<List<String>> rows = new ArrayList<>();
        String sql = "SELECT rt.name as room_type, " +
                "COUNT(r.id) as total_rooms, " +
                "SUM(CASE WHEN r.status = 'OCCUPIED' THEN 1 ELSE 0 END) as occupied_count, " +
                "SUM(CASE WHEN r.status IN ('AVAILABLE', 'INSPECTED') THEN 1 ELSE 0 END) as available_count, " +
                "SUM(CASE WHEN r.status IN ('DIRTY', 'CLEANING') THEN 1 ELSE 0 END) as cleaning_count, " +
                "SUM(CASE WHEN r.status = 'MAINTENANCE' THEN 1 ELSE 0 END) as maintenance_count " +
                "FROM room_types rt " +
                "LEFT JOIN rooms r ON rt.id = r.room_type_id " +
                "GROUP BY rt.id, rt.name " +
                "ORDER BY rt.name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                List<String> row = new ArrayList<>();
                row.add(rs.getString("room_type"));
                int total = rs.getInt("total_rooms");
                int occupied = rs.getInt("occupied_count");
                row.add(String.valueOf(total));
                row.add(String.valueOf(occupied));
                row.add(String.valueOf(rs.getInt("available_count")));
                row.add(String.valueOf(rs.getInt("cleaning_count")));
                row.add(String.valueOf(rs.getInt("maintenance_count")));
                double rate = total > 0 ? ((double) occupied / total) * 100.0 : 0.0;
                row.add(String.format("%.1f%%", rate));
                rows.add(row);
            }
        }
        return rows;
    }

    /** Future occupancy forecast based on booked room-nights, not current physical room state. */
    public List<List<String>> getOccupancyForecast(LocalDate start, LocalDate end) throws SQLException {
        List<List<String>> rows = new ArrayList<>();
        String sql = "SELECT d.stay_date, COUNT(DISTINCT r.room_id) AS booked_rooms, " +
                "COALESCE(SUM(1),0) AS booked_room_nights " +
                "FROM (SELECT DATE_ADD(?, INTERVAL n DAY) AS stay_date " +
                "      FROM (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 " +
                "            UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 " +
                "            UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 " +
                "            UNION ALL SELECT 14 UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 " +
                "            UNION ALL SELECT 18 UNION ALL SELECT 19 UNION ALL SELECT 20 UNION ALL SELECT 21 " +
                "            UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24 UNION ALL SELECT 25 " +
                "            UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29) nums) d " +
                "LEFT JOIN reservations r ON r.check_in_date <= d.stay_date AND r.check_out_date > d.stay_date " +
                "AND r.status IN ('PENDING','CONFIRMED','CHECKED_IN') " +
                "WHERE d.stay_date <= ? GROUP BY d.stay_date ORDER BY d.stay_date";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    row.add(rs.getDate("stay_date").toString());
                    row.add(String.valueOf(rs.getInt("booked_rooms")));
                    row.add(String.valueOf(rs.getInt("booked_room_nights")));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    /** Revenue by calendar month; based on completed payment transactions. */
    public List<List<String>> getMonthlyRevenueTrends(LocalDate start, LocalDate end) throws SQLException {
        List<List<String>> rows = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(payment_date, '%Y-%m') AS revenue_month, COUNT(*) AS transactions, " +
                "COALESCE(SUM(amount),0) AS revenue FROM payments " +
                "WHERE payment_status='COMPLETED' AND DATE(payment_date) BETWEEN ? AND ? " +
                "GROUP BY DATE_FORMAT(payment_date, '%Y-%m') ORDER BY revenue_month";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start)); ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { List<String> row = new ArrayList<>(); row.add(rs.getString("revenue_month")); row.add(String.valueOf(rs.getInt("transactions"))); row.add(rs.getBigDecimal("revenue").toPlainString()); rows.add(row); }
            }
        }
        return rows;
    }

    /** Room-category performance combines completed payments with room-night reservations. */
    public List<List<String>> getRoomCategoryPerformance(LocalDate start, LocalDate end) throws SQLException {
        List<List<String>> rows = new ArrayList<>();
        String sql = "SELECT rt.name AS category, COUNT(res.id) AS bookings, " +
                "COALESCE(SUM(res.nights),0) AS room_nights, COALESCE(SUM(res.total_room_charge),0) AS room_revenue " +
                "FROM room_types rt JOIN rooms rm ON rm.room_type_id=rt.id " +
                "LEFT JOIN reservations res ON res.room_id=rm.id AND res.status IN ('CONFIRMED','CHECKED_IN','CHECKED_OUT') " +
                "AND res.check_in_date <= ? AND res.check_out_date >= ? " +
                "GROUP BY rt.id, rt.name ORDER BY room_revenue DESC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(end)); ps.setDate(2, Date.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { List<String> row = new ArrayList<>(); row.add(rs.getString("category")); row.add(String.valueOf(rs.getInt("bookings"))); row.add(String.valueOf(rs.getInt("room_nights"))); row.add(rs.getBigDecimal("room_revenue").toPlainString()); rows.add(row); }
            }
        }
        return rows;
    }

    /** Booking channel inferred from the schema's user_id: customer-account vs front-desk walk-in. */
    public List<List<String>> getBookingSourceAnalysis(LocalDate start, LocalDate end) throws SQLException {
        List<List<String>> rows = new ArrayList<>();
        String sql = "SELECT CASE WHEN user_id IS NULL THEN 'Front Desk / Walk-in' ELSE 'Customer Account / Online' END AS source, " +
                "COUNT(*) AS bookings, COALESCE(SUM(total_amount),0) AS booking_value " +
                "FROM reservations WHERE created_at >= ? AND created_at < DATE_ADD(?, INTERVAL 1 DAY) " +
                "AND status <> 'CANCELLED' GROUP BY source ORDER BY bookings DESC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start)); ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) { List<String> row = new ArrayList<>(); row.add(rs.getString("source")); row.add(String.valueOf(rs.getInt("bookings"))); row.add(rs.getBigDecimal("booking_value").toPlainString()); rows.add(row); }
            }
        }
        return rows;
    }

    public List<List<String>> getCancellationStatistics(LocalDate start, LocalDate end) throws SQLException {
        List<List<String>> rows = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(COALESCE(cancelled_at, updated_at), '%Y-%m') AS cancel_month, COUNT(*) AS cancellations " +
                "FROM reservations WHERE status='CANCELLED' AND DATE(COALESCE(cancelled_at, updated_at)) BETWEEN ? AND ? " +
                "GROUP BY DATE_FORMAT(COALESCE(cancelled_at, updated_at), '%Y-%m') ORDER BY cancel_month";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start)); ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) { List<String> row = new ArrayList<>(); row.add(rs.getString("cancel_month")); row.add(String.valueOf(rs.getInt("cancellations"))); rows.add(row); } }
        }
        return rows;
    }

    public List<List<String>> getHousekeepingPerformance(LocalDate start, LocalDate end) throws SQLException {
        List<List<String>> rows = new ArrayList<>();
        String sql = "SELECT COALESCE(u.full_name, 'Unassigned') AS staff_name, COUNT(*) AS tasks_completed, " +
                "SUM(CASE WHEN h.status='INSPECTED' THEN 1 ELSE 0 END) AS inspected_tasks " +
                "FROM housekeeping_tasks h LEFT JOIN users u ON u.id=h.assigned_to_user_id " +
                "WHERE h.completed_at IS NOT NULL AND DATE(h.completed_at) BETWEEN ? AND ? " +
                "GROUP BY u.id, u.full_name ORDER BY tasks_completed DESC";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start)); ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) { List<String> row = new ArrayList<>(); row.add(rs.getString("staff_name")); row.add(String.valueOf(rs.getInt("tasks_completed"))); row.add(String.valueOf(rs.getInt("inspected_tasks"))); rows.add(row); } }
        }
        return rows;
    }

}
