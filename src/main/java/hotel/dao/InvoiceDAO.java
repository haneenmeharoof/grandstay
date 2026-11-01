package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.Invoice;
import hotel.model.InvoiceItem;
import hotel.model.InvoiceStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InvoiceDAO {

    public Optional<Invoice> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM invoices WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice inv = mapResultSetToInvoice(rs);
                    inv.setItems(findItemsByInvoiceId(conn, inv.getId()));
                    return Optional.of(inv);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Invoice> findByReservationId(Long reservationId) throws SQLException {
        String sql = "SELECT * FROM invoices WHERE reservation_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice inv = mapResultSetToInvoice(rs);
                    inv.setItems(findItemsByInvoiceId(conn, inv.getId()));
                    return Optional.of(inv);
                }
            }
        }
        return Optional.empty();
    }

    public List<Invoice> findByGuestId(Long guestId) throws SQLException {
        List<Invoice> list = new ArrayList<>();
        String sql = "SELECT * FROM invoices WHERE guest_id = ? ORDER BY issue_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToInvoice(rs));
                }
            }
        }
        return list;
    }

    public Long insertWithConnection(Connection conn, Invoice invoice) throws SQLException {
        String sql = "INSERT INTO invoices (invoice_number, reservation_id, guest_id, subtotal, tax_amount, " +
                "discount_amount, total_amount, paid_amount, balance_due, status, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, invoice.getInvoiceNumber());
            ps.setLong(2, invoice.getReservationId());
            ps.setLong(3, invoice.getGuestId());
            ps.setBigDecimal(4, invoice.getSubtotal());
            ps.setBigDecimal(5, invoice.getTaxAmount());
            ps.setBigDecimal(6, invoice.getDiscountAmount());
            ps.setBigDecimal(7, invoice.getTotalAmount());
            ps.setBigDecimal(8, invoice.getPaidAmount());
            ps.setBigDecimal(9, invoice.getBalanceDue());
            ps.setString(10, invoice.getStatus().name());
            ps.setString(11, invoice.getNotes());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    invoice.setId(id);

                    // Insert invoice items
                    if (invoice.items() != null && !invoice.items().isEmpty()) {
                        String itemSql = "INSERT INTO invoice_items (invoice_id, description, quantity, unit_price, total_price, item_type) " +
                                "VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement itemPs = conn.prepareStatement(itemSql)) {
                            for (InvoiceItem item : invoice.items()) {
                                itemPs.setLong(1, id);
                                itemPs.setString(2, item.getDescription());
                                itemPs.setInt(3, item.getQuantity());
                                itemPs.setBigDecimal(4, item.getUnitPrice());
                                itemPs.setBigDecimal(5, item.getTotalPrice());
                                itemPs.setString(6, item.getItemType());
                                itemPs.addBatch();
                            }
                            itemPs.executeBatch();
                        }
                    }
                    return id;
                }
            }
        }
        return null;
    }

    public boolean updatePaymentWithConnection(Connection conn, Long invoiceId, java.math.BigDecimal newPaidAmount,
                                               java.math.BigDecimal newBalanceDue, InvoiceStatus status) throws SQLException {
        String sql = "UPDATE invoices SET paid_amount = ?, balance_due = ?, status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newPaidAmount);
            ps.setBigDecimal(2, newBalanceDue);
            ps.setString(3, status.name());
            ps.setLong(4, invoiceId);
            return ps.executeUpdate() > 0;
        }
    }

    private List<InvoiceItem> findItemsByInvoiceId(Connection conn, Long invoiceId) throws SQLException {
        List<InvoiceItem> items = new ArrayList<>();
        String sql = "SELECT * FROM invoice_items WHERE invoice_id = ? ORDER BY id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceItem item = new InvoiceItem();
                    item.setId(rs.getLong("id"));
                    item.setInvoiceId(rs.getLong("invoice_id"));
                    item.setDescription(rs.getString("description"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setTotalPrice(rs.getBigDecimal("total_price"));
                    item.setItemType(rs.getString("item_type"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getLong("id"));
        inv.setInvoiceNumber(rs.getString("invoice_number"));
        inv.setReservationId(rs.getLong("reservation_id"));
        inv.setGuestId(rs.getLong("guest_id"));
        Timestamp issue = rs.getTimestamp("issue_date");
        if (issue != null) inv.setIssueDate(issue.toLocalDateTime());
        inv.setSubtotal(rs.getBigDecimal("subtotal"));
        inv.setTaxAmount(rs.getBigDecimal("tax_amount"));
        inv.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        inv.setTotalAmount(rs.getBigDecimal("total_amount"));
        inv.setPaidAmount(rs.getBigDecimal("paid_amount"));
        inv.setBalanceDue(rs.getBigDecimal("balance_due"));
        inv.setStatus(InvoiceStatus.valueOf(rs.getString("status")));
        inv.setNotes(rs.getString("notes"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) inv.setCreatedAt(created.toLocalDateTime());
        return inv;
    }
}
