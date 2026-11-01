package hotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Invoice {
    private Long id;
    private String invoiceNumber;
    private Long reservationId;
    private Long guestId;
    private LocalDateTime issueDate;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal paidAmount = BigDecimal.ZERO;
    private BigDecimal balanceDue = BigDecimal.ZERO;
    private InvoiceStatus status = InvoiceStatus.ISSUED;
    private String notes;
    private LocalDateTime createdAt;
    private List<InvoiceItem> items = new ArrayList<>();

    public Invoice() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }

    public LocalDateTime getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal != null ? subtotal : BigDecimal.ZERO; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount != null ? taxAmount : BigDecimal.ZERO; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount != null ? paidAmount : BigDecimal.ZERO; }

    public BigDecimal getBalanceDue() { return balanceDue; }
    public void setBalanceDue(BigDecimal balanceDue) { this.balanceDue = balanceDue != null ? balanceDue : BigDecimal.ZERO; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<InvoiceItem> items() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items != null ? items : new ArrayList<>(); }
    public void addItem(InvoiceItem item) { if (this.items == null) this.items = new ArrayList<>(); this.items.add(item); }
}
