package hotel.service;

import hotel.dao.AuditLogDAO;
import hotel.dao.FeedbackDAO;
import hotel.dao.LocalGuideDAO;
import hotel.dao.NotificationDAO;
import hotel.dao.PreCheckinDAO;
import hotel.dao.PreferenceDAO;
import hotel.dao.ReservationDAO;
import hotel.dao.RoomDAO;
import hotel.dao.RoomTypeDAO;
import hotel.dao.ServiceOrderDAO;
import hotel.dao.WaitlistDAO;
import hotel.model.AuditLog;
import hotel.model.CustomerPreference;
import hotel.model.Feedback;
import hotel.model.LocalGuidePlace;
import hotel.model.Notification;
import hotel.model.PreCheckin;
import hotel.model.Reservation;
import hotel.model.ReservationStatus;
import hotel.model.Room;
import hotel.model.RoomType;
import hotel.model.ServiceOrder;
import hotel.model.TripItinerary;
import hotel.model.User;
import hotel.model.WaitlistEntry;
import hotel.util.SessionContext;
import hotel.validation.InputValidator;
import hotel.validation.ValidationResult;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CustomerFeatureService {

    private final PreCheckinDAO preCheckinDAO = new PreCheckinDAO();
    private final ServiceOrderDAO serviceOrderDAO = new ServiceOrderDAO();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final PreferenceDAO preferenceDAO = new PreferenceDAO();
    private final LocalGuideDAO localGuideDAO = new LocalGuideDAO();
    private final WaitlistDAO waitlistDAO = new WaitlistDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final RoomTypeDAO roomTypeDAO = new RoomTypeDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    // -------------------------------------------------------------
    // Digital Pre-Check-in
    // -------------------------------------------------------------
    public ValidationResult submitPreCheckin(Long reservationId, Long userId, String arrivalTime,
                                             String docType, String docNumber, String requests,
                                             String emergencyName, String emergencyPhone) {
        if (arrivalTime == null || arrivalTime.trim().isEmpty()) {
            return ValidationResult.error("Expected arrival time is required.");
        }
        if (docType == null || docType.trim().isEmpty() || docNumber == null || docNumber.trim().isEmpty()) {
            return ValidationResult.error("ID document type and number are required.");
        }
        if (emergencyName != null && !emergencyName.trim().isEmpty()
                && !emergencyName.trim().matches("[\\p{L}][\\p{L} .'-]{1,98}")) {
            return ValidationResult.error("Emergency contact name must contain a person's name, not a phone number.");
        }
        if (emergencyPhone != null && !emergencyPhone.trim().isEmpty()
                && !emergencyPhone.trim().matches("[+0-9() .-]{7,20}")) {
            return ValidationResult.error("Enter a valid emergency contact phone number.");
        }

        try {
            Optional<PreCheckin> existing = preCheckinDAO.findByReservationId(reservationId);
            if (existing.isPresent()) {
                return ValidationResult.error("Pre-check-in has already been submitted for this reservation.");
            }

            PreCheckin pc = new PreCheckin();
            pc.setReservationId(reservationId);
            pc.setUserId(userId);
            pc.setExpectedArrivalTime(arrivalTime.trim());
            pc.setIdDocumentType(docType.trim());
            pc.setIdDocumentNumber(docNumber.trim());
            pc.setSpecialRequests(requests);
            pc.setEmergencyContactName(emergencyName);
            pc.setEmergencyContactPhone(emergencyPhone);
            pc.setStatus(PreCheckin.Status.SUBMITTED);

            preCheckinDAO.insert(pc);

            notificationDAO.insert(new Notification(userId, "Pre-Check-in Submitted",
                    "Your pre-check-in details have been received and are pending staff review.",
                    "PRE_CHECKIN", String.valueOf(reservationId)));

            return ValidationResult.success();
        } catch (SQLException e) {
            return ValidationResult.error("Failed to submit pre-check-in: " + e.getMessage());
        }
    }

    public List<PreCheckin> getPendingPreCheckins() throws SQLException {
        return preCheckinDAO.findPendingReview();
    }

    public ValidationResult reviewPreCheckin(Long preCheckinId, PreCheckin.Status status, String notes, Long reviewerId) {
        try {
            preCheckinDAO.updateStatus(preCheckinId, status, notes, reviewerId);
            return ValidationResult.success();
        } catch (SQLException e) {
            return ValidationResult.error("Failed to update pre-check-in: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------
    // In-Room Service Requests
    // -------------------------------------------------------------
    public ValidationResult requestService(Long reservationId, Long userId, Long guestId,
                                          ServiceOrder.Category category, String description,
                                          String specialInstructions) {
        if (reservationId == null || userId == null || guestId == null) {
            return ValidationResult.error("Select one of your reservations before requesting a service.");
        }
        if (category == null) {
            return ValidationResult.error("Service category is required.");
        }
        if (description == null || description.trim().isEmpty()) {
            return ValidationResult.error("Please provide a description of your request.");
        }

        try {
            Optional<Reservation> reservationOpt = reservationDAO.findById(reservationId);
            if (reservationOpt.isEmpty()
                    || !userId.equals(reservationOpt.get().getUserId())
                    || !guestId.equals(reservationOpt.get().getGuestId())) {
                return ValidationResult.error("The selected reservation does not belong to your account.");
            }

            String orderNum = "SVC-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            ServiceOrder order = new ServiceOrder();
            order.setOrderNumber(orderNum);
            order.setReservationId(reservationId);
            order.setGuestId(guestId);
            order.setCategory(category);
            order.setDescription(description.trim());
            order.setSpecialInstructions(specialInstructions);
            order.setStatus(ServiceOrder.Status.PENDING);
            order.setChargeAmount(BigDecimal.ZERO);

            serviceOrderDAO.insert(order);

            User actor = SessionContext.getCurrentUser();
            if (actor != null) {
                notificationDAO.insert(new Notification(actor.getId(), "Service Request Placed",
                        "Your request for '" + category.getDisplayName() + "' has been logged (" + orderNum + ").",
                        "SERVICE_ORDER", orderNum));
            }

            return ValidationResult.success();
        } catch (SQLException e) {
            return ValidationResult.error("Failed to submit service request: " + e.getMessage());
        }
    }

    public List<ServiceOrder> getServiceOrdersByReservation(Long reservationId) throws SQLException {
        return serviceOrderDAO.findByReservationId(reservationId);
    }

    public List<ServiceOrder> getServiceOrdersByGuest(Long guestId) throws SQLException {
        if (guestId == null) {
            throw new IllegalArgumentException("Guest account is required.");
        }
        return serviceOrderDAO.findByGuestId(guestId);
    }

    /** Staff-only method: returns all service orders. */
    public List<ServiceOrder> getAllServiceOrders() throws SQLException {
        return serviceOrderDAO.findAll();
    }

    public ValidationResult updateServiceOrderStatus(Long orderId, ServiceOrder.Status status, Long staffUserId) {
        try {
            serviceOrderDAO.updateStatus(orderId, status, staffUserId);
            return ValidationResult.success();
        } catch (SQLException e) {
            return ValidationResult.error("Failed to update service order: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------
    // Ratings and Reviews
    // -------------------------------------------------------------
    public ValidationResult submitReview(Long reservationId, Long guestId, Long userId, int rating,
                                         String title, String reviewText) {
        if (rating < 1 || rating > 5) {
            return ValidationResult.error("Rating must be between 1 and 5 stars.");
        }
        if (title == null || title.trim().isEmpty()) {
            return ValidationResult.error("Review title cannot be empty.");
        }
        if (reviewText == null || reviewText.trim().length() < 10) {
            return ValidationResult.error("Review comment must be at least 10 characters long.");
        }

        try {
            Optional<Reservation> resOpt = reservationDAO.findById(reservationId);
            if (resOpt.isEmpty()) {
                return ValidationResult.error("Reservation not found.");
            }
            Reservation res = resOpt.get();
            if (res.getStatus() != ReservationStatus.CHECKED_OUT) {
                return ValidationResult.error("Reviews can only be submitted after checkout has been completed by the front desk.");
            }
            if (guestId == null || userId == null || !guestId.equals(res.getGuestId()) || !userId.equals(res.getUserId())) {
                return ValidationResult.error("You can only review a reservation belonging to your account.");
            }

            if (feedbackDAO.findByReservationId(reservationId).isPresent()) {
                return ValidationResult.error("You have already reviewed this reservation.");
            }

            Feedback fb = new Feedback();
            fb.setReservationId(reservationId);
            fb.setGuestId(guestId);
            fb.setUserId(userId);
            fb.setRating(rating);
            fb.setReviewTitle(title.trim());
            fb.setReviewText(reviewText.trim());
            fb.setModerationStatus(Feedback.ModerationStatus.PENDING);

            feedbackDAO.insert(fb);

            if (userId != null) {
                notificationDAO.insert(new Notification(userId, "Thank you for your feedback!",
                        "Your review has been submitted for moderation. Thank you for sharing your experience!",
                        "REVIEW", String.valueOf(reservationId)));
            }

            return ValidationResult.success();
        } catch (SQLException e) {
            return ValidationResult.error("Failed to submit review: " + e.getMessage());
        }
    }

    public List<Feedback> getApprovedReviews() throws SQLException {
        return feedbackDAO.findApproved();
    }

    public List<Feedback> getAllReviews() throws SQLException {
        return feedbackDAO.findAll();
    }

    public ValidationResult moderateReview(Long feedbackId, Feedback.ModerationStatus status, Long adminUserId) {
        try {
            feedbackDAO.updateModeration(feedbackId, status, adminUserId);
            return ValidationResult.success();
        } catch (SQLException e) {
            return ValidationResult.error("Failed to moderate review: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------
    // Customer Preferences & Recommendations
    // -------------------------------------------------------------
    public Optional<CustomerPreference> getPreferences(Long userId) throws SQLException {
        return preferenceDAO.findByUserId(userId);
    }

    public void savePreferences(CustomerPreference cp) throws SQLException {
        preferenceDAO.saveOrUpdate(cp);
    }

    public List<Room> getRecommendedRooms(Long userId, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        List<Room> available = roomDAO.findAvailableRooms(checkIn, checkOut, null, null);
        Optional<CustomerPreference> prefOpt = preferenceDAO.findByUserId(userId);
        if (prefOpt.isEmpty()) {
            return available;
        }

        CustomerPreference pref = prefOpt.get();
        List<Room> matches = new ArrayList<>();
        List<Room> others = new ArrayList<>();

        for (Room r : available) {
            boolean match = true;
            if (pref.isHighFloor() && r.getFloorNumber() < 2) match = false;
            if (pref.getPreferredBedType() != null && !pref.getPreferredBedType().isEmpty() &&
                    !r.getRoomType().getBedType().toLowerCase().contains(pref.getPreferredBedType().toLowerCase())) {
                match = false;
            }
            if (match) {
                matches.add(r);
            } else {
                others.add(r);
            }
        }
        matches.addAll(others);
        return matches;
    }

    // -------------------------------------------------------------
    // Waitlist Management
    // -------------------------------------------------------------
    public ValidationResult joinWaitlist(Long userId, Long guestId, Long roomTypeId, LocalDate checkIn,
                                         LocalDate checkOut, int guests, String notes) {
        ValidationResult val = InputValidator.validateBookingDates(checkIn, checkOut);
        if (!val.isValid()) return val;

        try {
            WaitlistEntry we = new WaitlistEntry();
            we.setUserId(userId);
            we.setGuestId(guestId);
            we.setRoomTypeId(roomTypeId);
            we.setDesiredCheckIn(checkIn);
            we.setDesiredCheckOut(checkOut);
            we.setNumberOfGuests(guests);
            we.setStatus(WaitlistEntry.Status.ACTIVE);
            we.setNotes(notes);

            waitlistDAO.insert(we);

            if (userId != null) {
                notificationDAO.insert(new Notification(userId, "Added to Waitlist",
                        "We will notify you immediately if a room becomes available for your dates.",
                        "WAITLIST", String.valueOf(roomTypeId)));
            }

            return ValidationResult.success();
        } catch (SQLException e) {
            return ValidationResult.error("Failed to join waitlist: " + e.getMessage());
        }
    }

    public List<WaitlistEntry> getMyWaitlist(Long userId) throws SQLException {
        return waitlistDAO.findByUserId(userId);
    }

    public List<WaitlistEntry> getAllWaitlist() throws SQLException {
        return waitlistDAO.findAll();
    }

    // -------------------------------------------------------------
    // In-App Notifications
    // -------------------------------------------------------------
    public List<Notification> getNotifications(Long userId) throws SQLException {
        return notificationDAO.findByUserId(userId);
    }

    public int getUnreadNotificationCount(Long userId) throws SQLException {
        return notificationDAO.countUnread(userId);
    }

    public void markNotificationRead(Long notificationId) throws SQLException {
        notificationDAO.markAsRead(notificationId);
    }

    public void markAllNotificationsRead(Long userId) throws SQLException {
        notificationDAO.markAllAsRead(userId);
    }
}
