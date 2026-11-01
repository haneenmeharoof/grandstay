package hotel.model;

import java.math.BigDecimal;

public class InvoiceItem {
    private Long id;
    private Long invoiceId;
    private String description;
    private int quantity;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal totalPrice = BigDecimal.ZERO;
    private String itemType = "ROOM";

    public InvoiceItem() {}

    public InvoiceItem(String description, int quantity, BigDecimal unitPrice, BigDecimal totalPrice, String itemType) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        this.totalPrice = totalPrice != null ? totalPrice : BigDecimal.ZERO;
        this.itemType = itemType != null ? itemType : "ROOM";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice != null ? totalPrice : BigDecimal.ZERO; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
}
