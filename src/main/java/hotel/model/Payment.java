package hotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private Long id;
    private String paymentReference;
    private Long reservationId;
    private Long invoiceId;
    private BigDecimal amount = BigDecimal.ZERO;
    private PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;
    private PaymentStatus paymentStatus = PaymentStatus.COMPLETED;
    private String transactionNotes;
    private Long processedByUserId;
    private LocalDateTime paymentDate;

    public Payment() {}

    public Payment(String paymentReference, Long reservationId, Long invoiceId, BigDecimal amount,
                   PaymentMethod paymentMethod, String transactionNotes, Long processedByUserId) {
        this.paymentReference = paymentReference;
        this.reservationId = reservationId;
        this.invoiceId = invoiceId;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.CREDIT_CARD;
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.transactionNotes = transactionNotes;
        this.processedByUserId = processedByUserId;
        this.paymentDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount != null ? amount : BigDecimal.ZERO; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getTransactionNotes() { return transactionNotes; }
    public void setTransactionNotes(String transactionNotes) { this.transactionNotes = transactionNotes; }

    public Long getProcessedByUserId() { return processedByUserId; }
    public void setProcessedByUserId(Long processedByUserId) { this.processedByUserId = processedByUserId; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
}
