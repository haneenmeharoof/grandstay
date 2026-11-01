package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.model.InventoryItem;
import hotel.model.InventoryTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InventoryDAO {

    public Optional<InventoryItem> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM inventory_items WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToItem(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<InventoryItem> findAll() throws SQLException {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory_items WHERE is_active = TRUE ORDER BY category ASC, item_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToItem(rs));
            }
        }
        return list;
    }

    public List<InventoryItem> findLowStockItems() throws SQLException {
        List<InventoryItem> list = new ArrayList<>();
        String sql = "SELECT * FROM inventory_items WHERE is_active = TRUE AND quantity_on_hand <= min_stock_level ORDER BY quantity_on_hand ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToItem(rs));
            }
        }
        return list;
    }

    public Long insertItem(InventoryItem item) throws SQLException {
        String sql = "INSERT INTO inventory_items (item_code, item_name, category, quantity_on_hand, min_stock_level, " +
                "unit_of_measure, unit_cost, location, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getItemCode());
            ps.setString(2, item.getItemName());
            ps.setString(3, item.getCategory());
            ps.setInt(4, item.getQuantityOnHand());
            ps.setInt(5, item.getMinStockLevel());
            ps.setString(6, item.getUnitOfMeasure());
            ps.setBigDecimal(7, item.getUnitCost());
            ps.setString(8, item.getLocation());
            ps.setBoolean(9, item.isActive());

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

    public boolean updateItem(InventoryItem item) throws SQLException {
        String sql = "UPDATE inventory_items SET item_name = ?, category = ?, min_stock_level = ?, " +
                "unit_of_measure = ?, unit_cost = ?, location = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getItemName());
            ps.setString(2, item.getCategory());
            ps.setInt(3, item.getMinStockLevel());
            ps.setString(4, item.getUnitOfMeasure());
            ps.setBigDecimal(5, item.getUnitCost());
            ps.setString(6, item.getLocation());
            ps.setBoolean(7, item.isActive());
            ps.setLong(8, item.getId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Executes atomic stock change with transactional protection against negative balances.
     */
    public boolean processStockTransaction(Long itemId, InventoryTransaction.Type type, int quantity,
                                          String notes, Long userId) throws SQLException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Lock row for update
                String selectSql = "SELECT quantity_on_hand FROM inventory_items WHERE id = ? FOR UPDATE";
                int currentQty;
                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setLong(1, itemId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        currentQty = rs.getInt("quantity_on_hand");
                    }
                }

                int newQty;
                if (type == InventoryTransaction.Type.STOCK_IN || type == InventoryTransaction.Type.ADJUSTMENT) {
                    newQty = currentQty + quantity;
                } else {
                    // STOCK_OUT or ROOM_RESTOCK
                    if (currentQty < quantity) {
                        conn.rollback();
                        throw new IllegalStateException("Insufficient stock! Available: " + currentQty + ", Requested: " + quantity);
                    }
                    newQty = currentQty - quantity;
                }

                // Update stock
                String updateSql = "UPDATE inventory_items SET quantity_on_hand = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, newQty);
                    ps.setLong(2, itemId);
                    ps.executeUpdate();
                }

                // Insert transaction record
                String txSql = "INSERT INTO inventory_transactions (item_id, transaction_type, quantity, balance_after, reference_notes, performed_by_user_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(txSql)) {
                    ps.setLong(1, itemId);
                    ps.setString(2, type.name());
                    ps.setInt(3, quantity);
                    ps.setInt(4, newQty);
                    ps.setString(5, notes);
                    if (userId != null) {
                        ps.setLong(6, userId);
                    } else {
                        ps.setNull(6, java.sql.Types.BIGINT);
                    }
                    ps.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<InventoryTransaction> findTransactionsByItemId(Long itemId) throws SQLException {
        List<InventoryTransaction> list = new ArrayList<>();
        String sql = "SELECT tx.*, item.item_name, u.full_name as performed_by_name " +
                "FROM inventory_transactions tx " +
                "JOIN inventory_items item ON tx.item_id = item.id " +
                "LEFT JOIN users u ON tx.performed_by_user_id = u.id " +
                "WHERE tx.item_id = ? ORDER BY tx.transaction_date DESC LIMIT 100";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTx(rs));
                }
            }
        }
        return list;
    }

    public List<InventoryTransaction> findRecentTransactions() throws SQLException {
        List<InventoryTransaction> list = new ArrayList<>();
        String sql = "SELECT tx.*, item.item_name, u.full_name as performed_by_name " +
                "FROM inventory_transactions tx " +
                "JOIN inventory_items item ON tx.item_id = item.id " +
                "LEFT JOIN users u ON tx.performed_by_user_id = u.id " +
                "ORDER BY tx.transaction_date DESC LIMIT 100";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToTx(rs));
            }
        }
        return list;
    }

    private InventoryItem mapResultSetToItem(ResultSet rs) throws SQLException {
        InventoryItem item = new InventoryItem();
        item.setId(rs.getLong("id"));
        item.setItemCode(rs.getString("item_code"));
        item.setItemName(rs.getString("item_name"));
        item.setCategory(rs.getString("category"));
        item.setQuantityOnHand(rs.getInt("quantity_on_hand"));
        item.setMinStockLevel(rs.getInt("min_stock_level"));
        item.setUnitOfMeasure(rs.getString("unit_of_measure"));
        item.setUnitCost(rs.getBigDecimal("unit_cost"));
        item.setLocation(rs.getString("location"));
        item.setActive(rs.getBoolean("is_active"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) item.setCreatedAt(created.toLocalDateTime());
        return item;
    }

    private InventoryTransaction mapResultSetToTx(ResultSet rs) throws SQLException {
        InventoryTransaction tx = new InventoryTransaction();
        tx.setId(rs.getLong("id"));
        tx.setItemId(rs.getLong("item_id"));
        tx.setItemName(rs.getString("item_name"));
        tx.setTransactionType(InventoryTransaction.Type.valueOf(rs.getString("transaction_type")));
        tx.setQuantity(rs.getInt("quantity"));
        tx.setBalanceAfter(rs.getInt("balance_after"));
        tx.setReferenceNotes(rs.getString("reference_notes"));
        long uId = rs.getLong("performed_by_user_id");
        if (!rs.wasNull()) tx.setPerformedByUserId(uId);
        tx.setPerformedByName(rs.getString("performed_by_name"));
        Timestamp date = rs.getTimestamp("transaction_date");
        if (date != null) tx.setTransactionDate(date.toLocalDateTime());
        return tx;
    }
}
