package hotel.service;

import hotel.config.AppConfig;
import hotel.dao.AuditLogDAO;
import hotel.dao.HousekeepingDAO;
import hotel.dao.InvoiceDAO;
import hotel.dao.NotificationDAO;
import hotel.dao.ReservationDAO;
import hotel.dao.RoomDAO;
import hotel.dao.RoomTypeDAO;
import hotel.dao.UserDAO;
import hotel.database.DatabaseConnection;
import hotel.model.AuditLog;
import hotel.model.Guest;
import hotel.model.HousekeepingTask;
import hotel.model.Invoice;
import hotel.model.InvoiceItem;
import hotel.model.InvoiceStatus;
import hotel.model.Notification;
import hotel.model.Reservation;
import hotel.model.ReservationStatus;
import hotel.model.Room;
import hotel.model.RoomStatus;
import hotel.model.RoomType;
import hotel.model.User;
import hotel.util.CurrencyUtil;
import hotel.util.DateUtil;
import hotel.util.SessionContext;
import hotel.validation.InputValidator;
import hotel.validation.ValidationResult;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final RoomTypeDAO roomTypeDAO = new RoomTypeDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final HousekeepingDAO housekeepingDAO = new HousekeepingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    /**
     * Calculates cost estimation breakdown for a stay.
     */
    public CostBreakdown calculateCost(BigDecimal baseRate, LocalDate checkIn, LocalDate checkOut, BigDecimal discount) {
        int nights = DateUtil.calculateNights(checkIn, checkOut);
        BigDecimal rate = baseRate != null ? baseRate : BigDecimal.ZERO;
        BigDecimal roomCharge = CurrencyUtil.roundMoney(rate.multiply(BigDecimal.valueOf(nights)));
        BigDecimal tax = CurrencyUtil.calculateTax(roomCharge, AppConfig.getTaxRate());
        BigDecimal serviceFee = AppConfig.getServiceFee();
        BigDecimal disc = discount != null ? discount : BigDecimal.ZERO;
        BigDecimal total = CurrencyUtil.calculateTotal(roomCharge, tax, serviceFee, disc);

        return new CostBreakdown(nights, rate, roomCharge, tax, serviceFee, disc, total);
    }

    public static record CostBreakdown(
            int nights,
            BigDecimal roomRate,
            BigDecimal totalRoomCharge,
            BigDecimal taxAmount,
            BigDecimal serviceFee,
            BigDecimal discountAmount,
            BigDecimal totalAmount
    ) {}

    /**
     * Books a room inside a database transaction.
     * Prevents overlapping bookings, validates capacity, creates invoice & notification.
     */
    public BookingResult createReservation(Long guestId, Long userId, Long roomId, LocalDate checkIn,
                                           LocalDate checkOut, int numberOfGuests, String specialRequests,
                                           BigDecimal discount) {
        // Step 1: Validate dates
        ValidationResult dateVal = InputValidator.validateBookingDates(checkIn, checkOut);
        if (!dateVal.isValid()) {
            return BookingResult.failure(dateVal.getFormattedErrors());
        }

        try {
            // Step 2: Validate room and capacity
            Optional<Room> roomOpt = roomDAO.findById(roomId);
            if (roomOpt.isEmpty()) {
                return BookingResult.failure("Selected room does not exist.");
            }
            Room room = roomOpt.get();

            ValidationResult capVal = InputValidator.validateCapacity(numberOfGuests, room.getRoomType().getMaxCapacity());
            if (!capVal.isValid()) {
                return BookingResult.failure(capVal.getFormattedErrors());
            }

            // Step 3: Check overlapping bookings
            if (reservationDAO.hasOverlappingReservation(roomId, checkIn, checkOut, null)) {
                return BookingResult.failure("Room " + room.getRoomNumber() + " is already reserved for the selected dates. Please choose another room or date range.");
            }

            // Step 4: Calculate financials
            CostBreakdown cost = calculateCost(room.getRoomType().getBasePrice(), checkIn, checkOut, discount);

            // Generate unique reference
            String refNumber = "GS-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            String invNumber = "INV-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Step 5: Execute atomic booking transaction
            // Step 5: Execute atomic booking transaction
try (Connection conn = DatabaseConnection.getConnection()) {
    conn.setAutoCommit(false);

    try {
        // Lock the selected room until this transaction completes.
        Optional<Room> lockedRoomOpt =
                roomDAO.findByIdForUpdate(conn, roomId);

        if (lockedRoomOpt.isEmpty()) {
            conn.rollback();
            return BookingResult.failure("Selected room no longer exists.");
        }

        Room lockedRoom = lockedRoomOpt.get();

        // Revalidate guest capacity using the locked room.
        ValidationResult lockedCapacity =
                InputValidator.validateCapacity(
                        numberOfGuests,
                        lockedRoom.getRoomType().getMaxCapacity()
                );

        if (!lockedCapacity.isValid()) {
            conn.rollback();
            return BookingResult.failure(lockedCapacity.getFormattedErrors());
        }

        // Check for overlapping reservations inside the same transaction.
        if (reservationDAO.hasOverlappingReservation(
                conn, roomId, checkIn, checkOut, null)) {

            conn.rollback();
            return BookingResult.failure(
                    "Conflict detected! This room is already reserved for the selected dates."
            );
        }

        Reservation res = new Reservation();
        res.setReferenceNumber(refNumber);
        res.setGuestId(guestId);
        res.setUserId(userId);
        res.setRoomId(roomId);
        res.setCheckInDate(checkIn);
        res.setCheckOutDate(checkOut);
        res.setNumberOfGuests(numberOfGuests);
        res.setNights(cost.nights());
        res.setRoomRate(cost.roomRate());
        res.setTotalRoomCharge(cost.totalRoomCharge());
        res.setTaxAmount(cost.taxAmount());
        res.setServiceFee(cost.serviceFee());
        res.setDiscountAmount(cost.discountAmount());
        res.setTotalAmount(cost.totalAmount());
        res.setPaidAmount(BigDecimal.ZERO);
        res.setStatus(ReservationStatus.CONFIRMED);
        res.setSpecialRequests(specialRequests);

        Long resId = reservationDAO.insertWithConnection(conn, res);

        // Create invoice within the same transaction.
        Invoice inv = new Invoice();
        inv.setInvoiceNumber(invNumber);
        inv.setReservationId(resId);
        inv.setGuestId(guestId);
        inv.setSubtotal(cost.totalRoomCharge());
        inv.setTaxAmount(cost.taxAmount());
        inv.setDiscountAmount(cost.discountAmount());
        inv.setTotalAmount(cost.totalAmount());
        inv.setPaidAmount(BigDecimal.ZERO);
        inv.setBalanceDue(cost.totalAmount());
        inv.setStatus(InvoiceStatus.ISSUED);
        inv.setNotes("Reservation folio for booking " + refNumber);

        inv.addItem(new InvoiceItem(
                lockedRoom.getRoomType().getName() + " (" +
                cost.nights() + " nights @ " +
                CurrencyUtil.format(cost.roomRate()) + ")",
                cost.nights(),
                cost.roomRate(),
                cost.totalRoomCharge(),
                "ROOM"
        ));

        inv.addItem(new InvoiceItem(
                "Hotel Service Fee", 1,
                cost.serviceFee(), cost.serviceFee(), "FEE"
        ));

        invoiceDAO.insertWithConnection(conn, inv);

        conn.commit();

        // Send notification after successful booking.
        if (userId != null) {
            notificationDAO.insert(new Notification(
                    userId,
                    "Reservation Confirmed!",
                    "Your booking #" + refNumber +
                    " for Room " + lockedRoom.getRoomNumber() +
                    " has been confirmed.",
                    "BOOKING_CONFIRMATION",
                    refNumber
            ));
        }

        User actor = SessionContext.getCurrentUser();
        String actorName = actor != null ? actor.getUsername() : "Guest";

        auditLogDAO.log(new AuditLog(
                actor != null ? actor.getId() : userId,
                actorName,
                actor != null ? actor.getRole().name() : "CUSTOMER",
                "BOOKING_CREATED",
                "RESERVATION",
                String.valueOf(resId),
                "Created reservation " + refNumber +
                " for Room " + lockedRoom.getRoomNumber()
        ));

        logger.info(
                "Successfully created reservation {} for Room {}",
                refNumber, lockedRoom.getRoomNumber()
        );

        return BookingResult.success(res, refNumber);

    } catch (Exception e) {
        conn.rollback();
        throw e;
    } finally {
        conn.setAutoCommit(true);
    }
}
        } catch (SQLException e) {
            logger.error("Error creating reservation: {}", e.getMessage(), e);
            return BookingResult.failure("Failed to create reservation: " + e.getMessage());
        }
    }

    /**
     * Check-in guest workflow.
     */
    public ValidationResult checkIn(Long reservationId) {
        try {
            Optional<Reservation> resOpt = reservationDAO.findById(reservationId);
            if (resOpt.isEmpty()) {
                return ValidationResult.error("Reservation not found.");
            }
            Reservation res = resOpt.get();
            if (res.getStatus() != ReservationStatus.CONFIRMED && res.getStatus() != ReservationStatus.PENDING) {
                return ValidationResult.error("Cannot check in. Current reservation status is " + res.getStatus().getDisplayName());
            }

            Optional<Room> roomOpt = roomDAO.findById(res.getRoomId());
            if (roomOpt.isEmpty()) {
                return ValidationResult.error("Assigned room not found.");
            }
            Room room = roomOpt.get();
            if (room.getStatus() == RoomStatus.MAINTENANCE || room.getStatus() == RoomStatus.OUT_OF_SERVICE) {
                return ValidationResult.error("Cannot check in: Room " + room.getRoomNumber() + " is currently under " + room.getStatus().getDisplayName() + ". Please reassign the room first.");
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    reservationDAO.updateStatusWithConnection(conn, res.getId(), ReservationStatus.CHECKED_IN, null);
                    roomDAO.updateStatusWithConnection(conn, room.getId(), RoomStatus.OCCUPIED);
                    conn.commit();

                    User actor = SessionContext.getCurrentUser();
                    auditLogDAO.log(new AuditLog(actor != null ? actor.getId() : null,
                            actor != null ? actor.getUsername() : "Staff",
                            actor != null ? actor.getRole().name() : "STAFF",
                            "CHECK_IN", "RESERVATION", String.valueOf(res.getId()),
                            "Checked in guest to Room " + room.getRoomNumber()));
                    return ValidationResult.success();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            return ValidationResult.error("Check-in failed: " + e.getMessage());
        }
    }

    /**
     * Check-out guest workflow.
     */
    public ValidationResult checkOut(Long reservationId) {
        try {
            Optional<Reservation> resOpt = reservationDAO.findById(reservationId);
            if (resOpt.isEmpty()) {
                return ValidationResult.error("Reservation not found.");
            }
            Reservation res = resOpt.get();
            if (res.getStatus() != ReservationStatus.CHECKED_IN) {
                return ValidationResult.error("Cannot check out. Guest is not currently checked in.");
            }

            // Check outstanding balance
            if (res.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {
                return ValidationResult.error("Cannot check out: Outstanding balance of " +
                        CurrencyUtil.format(res.getBalanceDue()) + " must be settled first.");
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    reservationDAO.updateStatusWithConnection(conn, res.getId(), ReservationStatus.CHECKED_OUT, null);
                    // Room becomes DIRTY and requires housekeeping turnover
                    roomDAO.updateStatusWithConnection(conn, res.getRoomId(), RoomStatus.DIRTY);

                    Long housekeeperId = userDAO.findByUsernameOrEmail("housekeeper")
        .map(User::getId)
        .orElseThrow(() -> new SQLException("Housekeeping account not found."));

                    // Create housekeeping cleaning task automatically
                    HousekeepingTask task = new HousekeepingTask();
                    task.setAssignedToUserId(housekeeperId);
                    task.setRoomId(res.getRoomId());
                    task.setTaskType("CHECKOUT_TURNOVER");
                    task.setPriority(HousekeepingTask.Priority.HIGH);
                    task.setStatus(HousekeepingTask.Status.PENDING);
                    task.setNotes("Guest checked out. Full linen replacement and deep sanitization required.");
                    housekeepingDAO.insertWithConnection(conn, task);

                    conn.commit();

                    User actor = SessionContext.getCurrentUser();
                    auditLogDAO.log(new AuditLog(actor != null ? actor.getId() : null,
                            actor != null ? actor.getUsername() : "Staff",
                            actor != null ? actor.getRole().name() : "STAFF",
                            "CHECK_OUT", "RESERVATION", String.valueOf(res.getId()),
                            "Checked out guest. Room " + res.getRoom().getRoomNumber() + " marked DIRTY. Housekeeping task generated."));
                    return ValidationResult.success();
                } catch (Exception e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            return ValidationResult.error("Check-out failed: " + e.getMessage());
        }
    }

    /**
     * Cancels reservation if eligible.
     */
    public ValidationResult cancelReservation(Long reservationId, String reason) {
        try {
            Optional<Reservation> resOpt = reservationDAO.findById(reservationId);
            if (resOpt.isEmpty()) {
                return ValidationResult.error("Reservation not found.");
            }
            Reservation res = resOpt.get();

            if (!res.getStatus().isCancellable()) {
                return ValidationResult.error("Cannot cancel reservation with status: " + res.getStatus().getDisplayName());
            }

            boolean updated = reservationDAO.updateStatus(
        res.getId(), ReservationStatus.CANCELLED, reason
);

if (!updated) {
    return ValidationResult.error("Cancellation failed: no reservation row was updated.");
}

            // Notify user
            if (res.getUserId() != null) {
                notificationDAO.insert(new Notification(res.getUserId(), "Reservation Cancelled",
                        "Your reservation #" + res.getReferenceNumber() + " has been cancelled.",
                        "BOOKING_CANCELLATION", res.getReferenceNumber()));
            }

            User actor = SessionContext.getCurrentUser();
            auditLogDAO.log(new AuditLog(actor != null ? actor.getId() : null,
                    actor != null ? actor.getUsername() : "Customer",
                    actor != null ? actor.getRole().name() : "CUSTOMER",
                    "BOOKING_CANCELLED", "RESERVATION", String.valueOf(res.getId()),
                    "Cancelled booking " + res.getReferenceNumber() + ". Reason: " + reason));

            return ValidationResult.success();
        } catch (SQLException e) {
            return ValidationResult.error("Cancellation failed: " + e.getMessage());
        }
    }

    public static class BookingResult {
        private final boolean success;
        private final String message;
        private final Reservation reservation;
        private final String referenceNumber;

        private BookingResult(boolean success, String message, Reservation reservation, String referenceNumber) {
            this.success = success;
            this.message = message;
            this.reservation = reservation;
            this.referenceNumber = referenceNumber;
        }

        public static BookingResult success(Reservation reservation, String referenceNumber) {
            return new BookingResult(true, "Booking confirmed successfully!", reservation, referenceNumber);
        }

        public static BookingResult failure(String message) {
            return new BookingResult(false, message, null, null);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Reservation getReservation() { return reservation; }
        public String getReferenceNumber() { return referenceNumber; }
    }
}
