package hotel.model;

import java.time.LocalDateTime;

public class InventoryTransaction {
    public enum Type {
        STOCK_IN("Stock In / Purchase"),
        STOCK_OUT("Stock Out / Consumption"),
        ADJUSTMENT("Inventory Adjustment"),
        ROOM_RESTOCK("Room Restock");

        private final String displayName;

        Type(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private Long id;
    private Long itemId;
    private String itemName;
    private Type transactionType;
    private int quantity;
    private int balanceAfter;
    private String referenceNotes;
    private Long performedByUserId;
    private String performedByName;
    private LocalDateTime transactionDate;

    public InventoryTransaction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Type getTransactionType() { return transactionType; }
    public void setTransactionType(Type transactionType) { this.transactionType = transactionType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(int balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getReferenceNotes() { return referenceNotes; }
    public void setReferenceNotes(String referenceNotes) { this.referenceNotes = referenceNotes; }

    public Long getPerformedByUserId() { return performedByUserId; }
    public void setPerformedByUserId(Long performedByUserId) { this.performedByUserId = performedByUserId; }

    public String getPerformedByName() { return performedByName; }
    public void setPerformedByName(String performedByName) { this.performedByName = performedByName; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
}
