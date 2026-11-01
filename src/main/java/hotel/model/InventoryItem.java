package hotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryItem {
    private Long id;
    private String itemCode;
    private String itemName;
    private String category;
    private int quantityOnHand;
    private int minStockLevel = 5;
    private String unitOfMeasure = "PCS";
    private BigDecimal unitCost = BigDecimal.ZERO;
    private String location;
    private boolean active = true;
    private LocalDateTime createdAt;

    public InventoryItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(int quantityOnHand) { this.quantityOnHand = quantityOnHand; }

    public int getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(int minStockLevel) { this.minStockLevel = minStockLevel; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost != null ? unitCost : BigDecimal.ZERO; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isLowStock() {
        return quantityOnHand <= minStockLevel;
    }

    @Override
    public String toString() {
        return itemName + " [" + itemCode + "] (" + quantityOnHand + " " + unitOfMeasure + ")";
    }
}
