package hotel.service;

import hotel.dao.AuditLogDAO;
import hotel.dao.InventoryDAO;
import hotel.model.AuditLog;
import hotel.model.InventoryItem;
import hotel.model.InventoryTransaction;
import hotel.model.User;
import hotel.util.SessionContext;
import hotel.validation.InputValidator;
import hotel.validation.ValidationResult;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class InventoryService {

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    public List<InventoryItem> getAllItems() throws SQLException {
        return inventoryDAO.findAll();
    }

    public List<InventoryItem> getLowStockAlerts() throws SQLException {
        return inventoryDAO.findLowStockItems();
    }

    public List<InventoryTransaction> getRecentTransactions() throws SQLException {
        return inventoryDAO.findRecentTransactions();
    }

    public ValidationResult adjustStock(Long itemId, InventoryTransaction.Type type, int quantity, String notes) {
        if (itemId == null) {
            return ValidationResult.error("Please select an inventory item.");
        }

        try {
            Optional<InventoryItem> itemOpt = inventoryDAO.findById(itemId);
            if (itemOpt.isEmpty()) {
                return ValidationResult.error("Inventory item not found.");
            }
            InventoryItem item = itemOpt.get();

            boolean isDeduction = (type == InventoryTransaction.Type.STOCK_OUT || type == InventoryTransaction.Type.ROOM_RESTOCK);
            ValidationResult val = InputValidator.validateInventoryQuantity(item.getQuantityOnHand(), quantity, isDeduction);
            if (!val.isValid()) {
                return val;
            }

            User actor = SessionContext.getCurrentUser();
            Long actorId = actor != null ? actor.getId() : null;

            inventoryDAO.processStockTransaction(itemId, type, quantity, notes, actorId);

            auditLogDAO.log(new AuditLog(actorId, actor != null ? actor.getUsername() : "Staff",
                    actor != null ? actor.getRole().name() : "STAFF", "INVENTORY_" + type.name(),
                    "INVENTORY", String.valueOf(itemId),
                    type.getDisplayName() + ": " + quantity + " " + item.getUnitOfMeasure() + " for " + item.getItemName()));

            return ValidationResult.success();
        } catch (IllegalStateException e) {
            return ValidationResult.error(e.getMessage());
        } catch (SQLException e) {
            return ValidationResult.error("Database error while adjusting inventory: " + e.getMessage());
        }
    }
}
