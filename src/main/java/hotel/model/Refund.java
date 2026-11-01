package hotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Refund {
    public enum RefundStatus {
        PENDING("Pending Approval"),
        PROCESSED("Processed"),
        REJECTED("Rejected");

        private final String displayName;

        RefundStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private Long id;
    private String refundReference;
    private Long paymentId;
    private Long reservationId;
    private BigDecimal amount = BigDecimal.ZERO;
    private String reason;
    private RefundStatus status = RefundStatus.PROCESSED;
    private Long processedByUserId;
    private LocalDateTime createdAt;

    public Refund() {}

    public Refund(String refundReference, Long paymentId, Long reservationId, BigDecimal amount, String reason, Long processedByUserId) {
        this.refundReference = refundReference;
        this.paymentId = paymentId;
        this.reservationId = reservationId;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.reason = reason;
        this.status = RefundStatus.PROCESSED;
        this.processedByUserId = processedByUserId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRefundReference() { return refundReference; }
    public void setRefundReference(String refundReference) { this.refundReference = refundReference; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount != null ? amount : BigDecimal.ZERO; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public RefundStatus getStatus() { return status; }
    public void setStatus(RefundStatus status) { this.status = status; }

    public Long getProcessedByUserId() { return processedByUserId; }
    public void setProcessedByUserId(Long processedByUserId) { this.processedByUserId = processedByUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
