package hotel.service;

import hotel.dao.AuditLogDAO;
import hotel.dao.InvoiceDAO;
import hotel.dao.NotificationDAO;
import hotel.dao.PaymentDAO;
import hotel.dao.ReservationDAO;
import hotel.database.DatabaseConnection;
import hotel.model.AuditLog;
import hotel.model.Invoice;
import hotel.model.InvoiceStatus;
import hotel.model.Notification;
import hotel.model.Payment;
import hotel.model.PaymentMethod;
import hotel.model.PaymentStatus;
import hotel.model.Refund;
import hotel.model.Reservation;
import hotel.model.User;
import hotel.util.CurrencyUtil;
import hotel.util.SessionContext;
import hotel.validation.InputValidator;
import hotel.validation.ValidationResult;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingService.class);

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    /**
     * Records a payment against a reservation and its invoice in an atomic transaction.
     */
    public ValidationResult recordPayment(Long reservationId, BigDecimal amount, PaymentMethod method, String notes) {
        ValidationResult val = InputValidator.validatePositiveAmount(amount, "Payment amount");
        if (!val.isValid()) {
            return val;
        }

        try {
            Optional<Reservation> resOpt = reservationDAO.findById(reservationId);
            if (resOpt.isEmpty()) {
                return ValidationResult.error("Reservation not found.");
            }
            Reservation res = resOpt.get();

            Optional<Invoice> invOpt = invoiceDAO.findByReservationId(reservationId);
            if (invOpt.isEmpty()) {
                return ValidationResult.error("No active invoice associated with this reservation.");
            }
            Invoice inv = invOpt.get();

            BigDecimal currentPaid = res.getPaidAmount() != null ? res.getPaidAmount() : BigDecimal.ZERO;
            BigDecimal newPaid = currentPaid.add(amount);
            BigDecimal total = res.getTotalAmount();
            BigDecimal newBalance = total.subtract(newPaid);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                newBalance = BigDecimal.ZERO;
            }

            InvoiceStatus newInvStatus = newBalance.compareTo(BigDecimal.ZERO) <= 0 ? InvoiceStatus.PAID : InvoiceStatus.PARTIALLY_PAID;
            String payRef = "PAY-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            User actor = SessionContext.getCurrentUser();
            Long actorId = actor != null ? actor.getId() : null;

            Payment payment = new Payment(payRef, reservationId, inv.getId(), amount, method, notes, actorId);

            // Execute atomic transaction
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    paymentDAO.insertWithConnection(conn, payment);
                    invoiceDAO.updatePaymentWithConnection(conn, inv.getId(), newPaid, newBalance, newInvStatus);
                    reservationDAO.updatePaidAmountWithConnection(conn, reservationId, newPaid);
                    conn.commit();

                    // Notification
                    if (res.getUserId() != null) {
                        notificationDAO.insert(new Notification(res.getUserId(), "Payment Received",
                                "We have received your payment of " + CurrencyUtil.format(amount) + " via " + method.getDisplayName() +
                                        ". Remaining balance: " + CurrencyUtil.format(newBalance),
                                "PAYMENT", payRef));
                    }

                    auditLogDAO.log(new AuditLog(actorId, actor != null ? actor.getUsername() : "System",
                            actor != null ? actor.getRole().name() : "STAFF", "PAYMENT_RECORDED",
                            "PAYMENT", payRef, "Recorded " + CurrencyUtil.format(amount) + " for booking " + res.getReferenceNumber()));

                    logger.info("Recorded payment {} of {} for reservation {}", payRef, amount, res.getReferenceNumber());
                    return ValidationResult.success();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            logger.error("Error recording payment: {}", e.getMessage(), e);
            return ValidationResult.error("Payment failed: " + e.getMessage());
        }
    }

    /**
     * Records a refund record for college demonstration purposes.
     */
    public ValidationResult recordRefund(Long reservationId, BigDecimal refundAmount, String reason) {
        ValidationResult val = InputValidator.validatePositiveAmount(refundAmount, "Refund amount");
        if (!val.isValid()) {
            return val;
        }

        try {
            Optional<Reservation> resOpt = reservationDAO.findById(reservationId);
            if (resOpt.isEmpty()) {
                return ValidationResult.error("Reservation not found.");
            }
            Reservation res = resOpt.get();

            BigDecimal paid = res.getPaidAmount() != null ? res.getPaidAmount() : BigDecimal.ZERO;
            if (refundAmount.compareTo(paid) > 0) {
                return ValidationResult.error("Refund amount (" + CurrencyUtil.format(refundAmount) +
                        ") cannot exceed total amount paid (" + CurrencyUtil.format(paid) + ").");
            }

            String refundRef = "REF-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            User actor = SessionContext.getCurrentUser();
            Long actorId = actor != null ? actor.getId() : null;

            Refund refund = new Refund(refundRef, null, reservationId, refundAmount, reason, actorId);

            BigDecimal adjustedPaid = paid.subtract(refundAmount);
            if (adjustedPaid.compareTo(BigDecimal.ZERO) < 0) adjustedPaid = BigDecimal.ZERO;

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    paymentDAO.insertRefundWithConnection(conn, refund);
                    reservationDAO.updatePaidAmountWithConnection(conn, reservationId, adjustedPaid);

                    Optional<Invoice> invOpt = invoiceDAO.findByReservationId(reservationId);
                    if (invOpt.isPresent()) {
                        Invoice inv = invOpt.get();
                        BigDecimal newBal = inv.getTotalAmount().subtract(adjustedPaid);
                        invoiceDAO.updatePaymentWithConnection(conn, inv.getId(), adjustedPaid, newBal, InvoiceStatus.PARTIALLY_PAID);
                    }

                    conn.commit();

                    if (res.getUserId() != null) {
                        notificationDAO.insert(new Notification(res.getUserId(), "Refund Processed",
                                "A refund of " + CurrencyUtil.format(refundAmount) + " has been recorded for booking #" + res.getReferenceNumber(),
                                "REFUND", refundRef));
                    }

                    auditLogDAO.log(new AuditLog(actorId, actor != null ? actor.getUsername() : "Staff",
                            actor != null ? actor.getRole().name() : "STAFF", "REFUND_RECORDED",
                            "REFUND", refundRef, "Recorded refund of " + CurrencyUtil.format(refundAmount) + " for booking " + res.getReferenceNumber()));

                    return ValidationResult.success();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            return ValidationResult.error("Refund recording failed: " + e.getMessage());
        }
    }
}
