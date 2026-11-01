package hotel.ui.customer;

import hotel.dao.FeedbackDAO;
import hotel.dao.GuestDAO;
import hotel.dao.InvoiceDAO;
import hotel.dao.LocalGuideDAO;
import hotel.dao.ReservationDAO;
import hotel.dao.RoomDAO;
import hotel.dao.RoomTypeDAO;
import hotel.model.Feedback;
import hotel.model.Guest;
import hotel.model.Invoice;
import hotel.model.LocalGuidePlace;
import hotel.model.Reservation;
import hotel.model.Room;
import hotel.model.RoomType;
import hotel.model.ServiceOrder;
import hotel.model.User;
import hotel.service.CustomerFeatureService;
import hotel.service.ReservationService;
import hotel.service.TripItineraryService;
import hotel.ui.common.HeaderPanel;
import hotel.ui.common.UITheme;
import hotel.util.CurrencyUtil;
import hotel.util.DateUtil;
import hotel.util.InvoiceGenerator;
import hotel.util.SessionContext;
import hotel.validation.ValidationResult;
import hotel.dao.WaitlistDAO;
import hotel.model.WaitlistEntry;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import hotel.model.TripItinerary;
import hotel.model.ItineraryItem;
import javax.swing.SpinnerDateModel;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

public class CustomerDashboardFrame extends JFrame {

    private final ReservationService reservationService = new ReservationService();
    private final CustomerFeatureService customerFeatureService = new CustomerFeatureService();
    private final TripItineraryService tripItineraryService = new TripItineraryService();
    private final RoomDAO roomDAO = new RoomDAO();
    private final RoomTypeDAO roomTypeDAO = new RoomTypeDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final LocalGuideDAO localGuideDAO = new LocalGuideDAO();
    private final GuestDAO guestDAO = new GuestDAO();

    // Components - Booking Tab
    private JTextField checkInField;
    private JTextField checkOutField;
    private JSpinner guestsSpinner;
    private JComboBox<String> roomTypeCombo;
    private JTable availableRoomsTable;
    private DefaultTableModel availableRoomsModel;
    private JLabel costEstimateLabel;

    // Components - My Reservations Tab
    private JTable myBookingsTable;
    private DefaultTableModel myBookingsModel;

    // Components - Service Requests Tab
    private JTable serviceOrdersTable;
    private DefaultTableModel serviceOrdersModel;

    // Components - Local Guide Tab
    private JTable guideTable;
    private DefaultTableModel guideModel;

    public CustomerDashboardFrame() {
        setTitle("GrandStay Hotel - Guest Experience Portal");
        setSize(1180, 750);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Top Bar
        add(new HeaderPanel("Guest Portal"), BorderLayout.NORTH);

        // Main Tabbed Navigation
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.setBackground(Color.WHITE);

        tabs.addTab("  ✦ Search & Book Rooms  ", createBookingPanel());
        tabs.addTab("  My Reservations  ", createMyReservationsPanel());
        tabs.addTab("  In-Room Services  ", createServicesPanel());
        tabs.addTab("  Preferences & Favorites  ", createPreferencesPanel());
        tabs.addTab("  Local Guide  ", createGuidePanel());
        tabs.addTab("  Trip Planner  ", createTripPlannerPanel());
        tabs.addTab("  Waitlist  ", createWaitlistPanel());

        add(tabs, BorderLayout.CENTER);

        // Initial Data Load
        refreshAvailableRooms();
        refreshMyReservations();
    }

    // =========================================================================
    // 1. SEARCH & BOOK ROOMS PANEL
    // =========================================================================
    private JPanel createBookingPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Filter / Search Card
        JPanel searchCard = UITheme.createCardPanel();
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

        JLabel inLabel = new JLabel("Check-in (YYYY-MM-DD):");
        inLabel.setFont(UITheme.FONT_BOLD);
        checkInField = new JTextField(LocalDate.now().toString(), 9);

        JLabel outLabel = new JLabel("Check-out (YYYY-MM-DD):");
        outLabel.setFont(UITheme.FONT_BOLD);
        checkOutField = new JTextField(LocalDate.now().plusDays(2).toString(), 9);

        JLabel guestLabel = new JLabel("Guests:");
        guestLabel.setFont(UITheme.FONT_BOLD);
        guestsSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 6, 1));

        JLabel typeLabel = new JLabel("Room Type:");
        typeLabel.setFont(UITheme.FONT_BOLD);
        roomTypeCombo = new JComboBox<>(new String[]{"All Room Types"});
        loadRoomTypeFilter();

        JButton searchBtn = UITheme.createPrimaryButton("Search Rooms");
        searchBtn.addActionListener(e -> refreshAvailableRooms());

        searchCard.add(inLabel);
        searchCard.add(checkInField);
        searchCard.add(outLabel);
        searchCard.add(checkOutField);
        searchCard.add(guestLabel);
        searchCard.add(guestsSpinner);
        searchCard.add(typeLabel);
        searchCard.add(roomTypeCombo);
        searchCard.add(searchBtn);

        panel.add(searchCard, BorderLayout.NORTH);

        // Available Rooms Grid
        String[] cols = {"Room ID", "Room #", "Room Category", "Capacity","Bed Type", "Nightly Rate", "Status", "Amenities","Recommendation"};
        availableRoomsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        availableRoomsTable = new JTable(availableRoomsModel);
        UITheme.styleTable(availableRoomsTable);

        // Hide ID column
        availableRoomsTable.getColumnModel().getColumn(0).setMinWidth(0);
        availableRoomsTable.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane tableScroll = new JScrollPane(availableRoomsTable);
        panel.add(tableScroll, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = UITheme.createCardPanel();
        bottomBar.setLayout(new BorderLayout());

        costEstimateLabel = new JLabel("Select dates and a room above to calculate estimated cost.");
        costEstimateLabel.setFont(UITheme.FONT_BOLD);
        costEstimateLabel.setForeground(UITheme.NAVY_DARK);

        JButton bookBtn = UITheme.createGoldButton("✦ Book Selected Room");
        bookBtn.addActionListener(e -> openBookRoomDialog());

        bottomBar.add(costEstimateLabel, BorderLayout.WEST);
        bottomBar.add(bookBtn, BorderLayout.EAST);
        panel.add(bottomBar, BorderLayout.SOUTH);

        // Listen for table selection to update estimate
        availableRoomsTable.getSelectionModel().addListSelectionListener(e -> updateCostEstimate());

        return panel;
    }

    private void loadRoomTypeFilter() {
        try {
            List<RoomType> types = roomTypeDAO.findAllActive();
            for (RoomType rt : types) {
                roomTypeCombo.addItem(rt.getName());
            }
        } catch (Exception ignored) {}
    }

    private void refreshAvailableRooms() {
        LocalDate checkIn;
        LocalDate checkOut;
        try {
            checkIn = LocalDate.parse(checkInField.getText().trim());
            checkOut = LocalDate.parse(checkOutField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Please enter dates in YYYY-MM-DD format.", "Invalid Dates", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int guests = (Integer) guestsSpinner.getValue();

        new SwingWorker<List<Room>, Void>() {
            @Override
protected List<Room> doInBackground() throws Exception {
    String selectedRoomType = (String) roomTypeCombo.getSelectedItem();
    Long selectedRoomTypeId = null;

    if (selectedRoomType != null && !selectedRoomType.equals("All Room Types")) {
        for (RoomType type : roomTypeDAO.findAllActive()) {
            if (type.getName().equals(selectedRoomType)) {
                selectedRoomTypeId = type.getId();
                break;
            }
        }
    }
    final Long roomTypeId = selectedRoomTypeId;
    return customerFeatureService.getRecommendedRooms(
        SessionContext.getCurrentUser().getId(),
        checkIn,
        checkOut
).stream()
 .filter(room -> roomTypeId == null
        || room.getRoomType().getId().equals(roomTypeId))
 .filter(room -> room.getRoomType().getMaxCapacity() >= guests)
 .filter(room -> room.getStatus() != null)
 .toList();

            }

            @Override
            protected void done() {
                try {
                    List<Room> rooms = get();
                    availableRoomsModel.setRowCount(0);
                    Optional<hotel.model.CustomerPreference> prefOpt =
        customerFeatureService.getPreferences(
                SessionContext.getCurrentUser().getId()
        );
                    for (Room r : rooms) {
                        boolean recommended = false;

if (prefOpt.isPresent()) {
    hotel.model.CustomerPreference pref = prefOpt.get();

    recommended = true;

    if (pref.isHighFloor() && r.getFloorNumber() < 2) {
        recommended = false;
    }

    String preferredBed = pref.getPreferredBedType();

    if (preferredBed != null && !preferredBed.isEmpty()
            && !r.getRoomType().getBedType()
                    .toLowerCase()
                    .contains(preferredBed.toLowerCase())) {
        recommended = false;
    }
}
                        availableRoomsModel.addRow(new Object[]{
        r.getId(),
        r.getRoomNumber(),
        r.getRoomType().getName(),
        r.getRoomType().getMaxCapacity() + " Guests",
        r.getRoomType().getBedType(),
        CurrencyUtil.format(r.getRoomType().getBasePrice()),
        r.getStatus().getDisplayName(),
        r.getRoomType().getAmenities(),
        recommended ? "Recommended for you" : "Standard option"
});
                    }
                    if (rooms.isEmpty()) {
                        costEstimateLabel.setText("No rooms available matching criteria. Consider checking another date or joining the waitlist.");
                    } else {
                        costEstimateLabel.setText("Found " + rooms.size() + " available room(s). Select one to proceed.");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(CustomerDashboardFrame.this, "Error fetching available rooms: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void updateCostEstimate() {
        int selected = availableRoomsTable.getSelectedRow();
        if (selected < 0) return;

        try {
            LocalDate checkIn = LocalDate.parse(checkInField.getText().trim());
            LocalDate checkOut = LocalDate.parse(checkOutField.getText().trim());
            String rateStr = availableRoomsTable.getValueAt(selected, 5).toString().replace("$", "").replace(",", "");
            BigDecimal rate = new BigDecimal(rateStr);

            ReservationService.CostBreakdown breakdown = reservationService.calculateCost(rate, checkIn, checkOut, BigDecimal.ZERO);
            costEstimateLabel.setText(String.format("Estimated Total: %s (%d nights @ %s + %s Tax & %s Fees)",
                    CurrencyUtil.format(breakdown.totalAmount()),
                    breakdown.nights(),
                    CurrencyUtil.format(breakdown.roomRate()),
                    CurrencyUtil.format(breakdown.taxAmount()),
                    CurrencyUtil.format(breakdown.serviceFee())));
        } catch (Exception ignored) {}
    }

    private void openBookRoomDialog() {
        int selected = availableRoomsTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select an available room from the list.", "Select Room", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long roomId = (Long) availableRoomsTable.getValueAt(selected, 0);
        String roomNumber = availableRoomsTable.getValueAt(selected, 1).toString();
        String roomTypeName = availableRoomsTable.getValueAt(selected, 2).toString();
        LocalDate checkIn = LocalDate.parse(checkInField.getText().trim());
        LocalDate checkOut = LocalDate.parse(checkOutField.getText().trim());
        int guests = (Integer) guestsSpinner.getValue();

        JDialog dialog = new JDialog(this, "Confirm Booking - Room " + roomNumber, true);
        dialog.setSize(450, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UITheme.BG_LIGHT);
        p.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        int row = 0;
        addDialogRow(p, gbc, row++, "Room:", roomNumber + " (" + roomTypeName + ")");
        addDialogRow(p, gbc, row++, "Check-in:", DateUtil.formatDate(checkIn));
        addDialogRow(p, gbc, row++, "Check-out:", DateUtil.formatDate(checkOut));
        addDialogRow(p, gbc, row++, "Total Cost:", costEstimateLabel.getText().replace("Estimated Total: ", ""));

        gbc.gridx = 0; gbc.gridy = row++;
        p.add(new JLabel("Special Requests:"), gbc);
        gbc.gridx = 1;
        JTextArea reqArea = new JTextArea(3, 18);
        reqArea.setLineWrap(true);
        p.add(new JScrollPane(reqArea), gbc);

        dialog.add(p, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton cancel = UITheme.createSecondaryButton("Cancel");
        cancel.addActionListener(e -> dialog.dispose());

        JButton confirm = UITheme.createGoldButton("Confirm & Reserve");
        confirm.addActionListener(e -> {
            User user = SessionContext.getCurrentUser();
            Long guestId = user != null ? user.getGuestId() : null;
            if (guestId == null) {
                guestId = 1L; // Fallback
            }

            ReservationService.BookingResult result = reservationService.createReservation(
                    guestId, user != null ? user.getId() : null, roomId, checkIn, checkOut, guests, reqArea.getText(), BigDecimal.ZERO
            );

            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(dialog,
                        "Reservation confirmed successfully!\nYour Reference Number is: " + result.getReferenceNumber(),
                        "Booking Confirmed",
                        JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshAvailableRooms();
                refreshMyReservations();
            } else {
                JOptionPane.showMessageDialog(dialog, result.getMessage(), "Booking Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(cancel);
        footer.add(confirm);
        dialog.add(footer, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addDialogRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BOLD);
        panel.add(l, gbc);

        gbc.gridx = 1;
        JLabel v = new JLabel(value);
        v.setFont(UITheme.FONT_REGULAR);
        panel.add(v, gbc);
    }

    // =========================================================================
    // 2. MY RESERVATIONS PANEL
    // =========================================================================
    private JPanel createMyReservationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] cols = {"ID", "Reference #", "Room #", "Check-in", "Check-out", "Nights", "Total Rate", "Paid", "Balance", "Status"};
        myBookingsModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        myBookingsTable = new JTable(myBookingsModel);
        UITheme.styleTable(myBookingsTable);

        // Hide ID
        myBookingsTable.getColumnModel().getColumn(0).setMinWidth(0);
        myBookingsTable.getColumnModel().getColumn(0).setMaxWidth(0);

        panel.add(new JScrollPane(myBookingsTable), BorderLayout.CENTER);

        // Action Buttons Bar
        JPanel actions = UITheme.createCardPanel();
        actions.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 8));

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshMyReservations());

        JButton viewInvoiceBtn = UITheme.createSecondaryButton("View Folio / Invoice");
        viewInvoiceBtn.addActionListener(e -> openInvoiceDialog());

        JButton preCheckinBtn = UITheme.createPrimaryButton("Digital Pre-Check-in");
        preCheckinBtn.addActionListener(e -> openPreCheckinDialog());

        JButton reviewBtn = UITheme.createSecondaryButton("Leave Review");
        reviewBtn.addActionListener(e -> openReviewDialog());

        JButton cancelBtn = UITheme.createDangerButton("Cancel Booking");
        cancelBtn.addActionListener(e -> handleCancelBooking());

        actions.add(refreshBtn);
        actions.add(viewInvoiceBtn);
        actions.add(preCheckinBtn);
        actions.add(reviewBtn);
        actions.add(cancelBtn);

        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshMyReservations() {
        User user = SessionContext.getCurrentUser();
        if (user == null) return;

        new SwingWorker<List<Reservation>, Void>() {
            @Override
            protected List<Reservation> doInBackground() throws Exception {
                return reservationDAO.findByUserId(user.getId());
            }

            @Override
            protected void done() {
                try {
                    List<Reservation> list = get();
                    myBookingsModel.setRowCount(0);
                    for (Reservation r : list) {
                        myBookingsModel.addRow(new Object[]{
                                r.getId(),
                                r.getReferenceNumber(),
                                r.getRoom() != null ? r.getRoom().getRoomNumber() : "TBD",
                                DateUtil.formatDate(r.getCheckInDate()),
                                DateUtil.formatDate(r.getCheckOutDate()),
                                r.getNights(),
                                CurrencyUtil.format(r.getTotalAmount()),
                                CurrencyUtil.format(r.getPaidAmount()),
                                CurrencyUtil.format(r.getBalanceDue()),
                                r.getStatus().getDisplayName()
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void openInvoiceDialog() {
        int selected = myBookingsTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a reservation to view its folio.", "Select Reservation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long resId = (Long) myBookingsTable.getValueAt(selected, 0);
        try {
            Optional<Invoice> invOpt = invoiceDAO.findByReservationId(resId);
            Optional<Reservation> resOpt = reservationDAO.findById(resId);
            if (invOpt.isEmpty() || resOpt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Invoice not yet generated.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            Invoice inv = invOpt.get();
            Reservation res = resOpt.get();
            Guest guest = res.getGuest();

            String html = InvoiceGenerator.generateHtmlInvoice(inv, res, guest);

            JDialog dialog = new JDialog(this, "Guest Folio & Invoice - " + inv.getInvoiceNumber(), true);
            dialog.setSize(650, 700);
            dialog.setLocationRelativeTo(this);

            JEditorPane pane = new JEditorPane();
            pane.setEditable(false);
            pane.setContentType("text/html");
            pane.setText(html);

            dialog.add(new JScrollPane(pane), BorderLayout.CENTER);

            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton closeBtn = UITheme.createPrimaryButton("Close");
            closeBtn.addActionListener(e -> dialog.dispose());
            footer.add(closeBtn);
            dialog.add(footer, BorderLayout.SOUTH);

            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error generating invoice: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openPreCheckinDialog() {
        int selected = myBookingsTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a confirmed reservation for pre-check-in.", "Select Reservation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long resId = (Long) myBookingsTable.getValueAt(selected, 0);
        String ref = (String) myBookingsTable.getValueAt(selected, 1);

        JDialog d = new JDialog(this, "Digital Pre-Check-in #" + ref, true);
        d.setSize(440, 400);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 6, 6, 6);

        JTextField arrField = new JTextField(java.time.LocalTime.now().plusHours(1).withSecond(0).withNano(0).toString(), 15);
        JComboBox<String> docTypeCombo = new JComboBox<>(new String[]{"PASSPORT", "NATIONAL_ID", "DRIVING_LICENSE"});
        JTextField docNumField = new JTextField(15);
        JTextField emNameField = new JTextField(15);
        JTextField emPhoneField = new JTextField(15);

        int r = 0;
        addFormField(form, g, r++, "Expected Arrival Time *:", arrField);
        addFormField(form, g, r++, "ID Document Type *:", docTypeCombo);
        addFormField(form, g, r++, "ID Document Number *:", docNumField);
        addFormField(form, g, r++, "Emergency Contact Name:", emNameField);
        addFormField(form, g, r++, "Emergency Contact Phone:", emPhoneField);

        d.add(form, BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submit = UITheme.createPrimaryButton("Submit Pre-Check-in");
        submit.addActionListener(e -> {
            User u = SessionContext.getCurrentUser();
            ValidationResult res = customerFeatureService.submitPreCheckin(
                    resId, u != null ? u.getId() : 1L, arrField.getText(),
                    (String) docTypeCombo.getSelectedItem(), docNumField.getText(),
                    "None", emNameField.getText(), emPhoneField.getText()
            );
            if (res.isValid()) {
                JOptionPane.showMessageDialog(d, "Pre-check-in details submitted successfully! Front desk staff will prepare your room.", "Success", JOptionPane.INFORMATION_MESSAGE);
                d.dispose();
            } else {
                JOptionPane.showMessageDialog(d, res.getFormattedErrors(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        });
        foot.add(submit);
        d.add(foot, BorderLayout.SOUTH);

        d.setVisible(true);
    }

    private void openReviewDialog() {
        int selected = myBookingsTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a completed stay to review.", "Select Reservation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long resId = (Long) myBookingsTable.getValueAt(selected, 0);
        User u = SessionContext.getCurrentUser();

        JDialog d = new JDialog(this, "Rate Your Stay", true);
        d.setSize(440, 360);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 6, 6, 6);

        JSpinner starSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        JTextField titleField = new JTextField("Wonderful stay at GrandStay!", 16);
        JTextArea commentArea = new JTextArea(4, 16);
        commentArea.setLineWrap(true);

        int r = 0;
        addFormField(form, g, r++, "Star Rating (1 - 5):", starSpinner);
        addFormField(form, g, r++, "Review Title *:", titleField);

        g.gridx = 0; g.gridy = r++;
        form.add(new JLabel("Review Comments *:"), g);
        g.gridx = 1;
        form.add(new JScrollPane(commentArea), g);

        d.add(form, BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submit = UITheme.createGoldButton("Submit Review");
        submit.addActionListener(e -> {
            if (u == null || u.getGuestId() == null || u.getId() == null) {
                JOptionPane.showMessageDialog(d, "Your customer session has expired. Please log in again.", "Session Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Long guestId = u.getGuestId();
            Long userId = u.getId();

            ValidationResult res = customerFeatureService.submitReview(
                    resId, guestId, userId, (Integer) starSpinner.getValue(),
                    titleField.getText(), commentArea.getText()
            );

            if (res.isValid()) {
                JOptionPane.showMessageDialog(d, "Thank you! Your review has been submitted for moderation.", "Review Submitted", JOptionPane.INFORMATION_MESSAGE);
                d.dispose();
            } else {
                JOptionPane.showMessageDialog(d, res.getFormattedErrors(), "Review Notice", JOptionPane.WARNING_MESSAGE);
            }
        });
        foot.add(submit);
        d.add(foot, BorderLayout.SOUTH);

        d.setVisible(true);
    }

    private void handleCancelBooking() {
        int selected = myBookingsTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a reservation to cancel.", "Select Reservation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long resId = (Long) myBookingsTable.getValueAt(selected, 0);
        String ref = (String) myBookingsTable.getValueAt(selected, 1);

        String reason = JOptionPane.showInputDialog(this, "Please enter a reason for cancellation:", "Cancel Reservation #" + ref, JOptionPane.QUESTION_MESSAGE);
        if (reason == null || reason.trim().isEmpty()) return;

        ValidationResult res = reservationService.cancelReservation(resId, reason.trim());
        if (res.isValid()) {
            JOptionPane.showMessageDialog(this, "Reservation #" + ref + " has been cancelled.", "Cancellation Successful", JOptionPane.INFORMATION_MESSAGE);
            refreshMyReservations();
            refreshAvailableRooms();
        } else {
            JOptionPane.showMessageDialog(this, res.getFormattedErrors(), "Cancellation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // 3. IN-ROOM SERVICES PANEL
    // =========================================================================
    private JPanel createServicesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Request Form
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

        JComboBox<ServiceOrder.Category> catCombo = new JComboBox<>(ServiceOrder.Category.values());
        JTextField descField = new JTextField("e.g. 2 extra bath towels and toiletries", 22);
        JButton reqBtn = UITheme.createPrimaryButton("Submit Request");

        reqBtn.addActionListener(e -> {
            User u = SessionContext.getCurrentUser();
            int selectedRes = myBookingsTable != null ? myBookingsTable.getSelectedRow() : -1;
            if (u == null || u.getId() == null || u.getGuestId() == null) {
                JOptionPane.showMessageDialog(this, "Please log in again.",
                        "Session Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (selectedRes < 0) {
                JOptionPane.showMessageDialog(this, "Select one of your reservations in My Reservations first.",
                        "Reservation Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int modelRow = myBookingsTable.convertRowIndexToModel(selectedRes);
            Object reservationValue = myBookingsModel.getValueAt(modelRow, 0);
            if (!(reservationValue instanceof Number)) {
                JOptionPane.showMessageDialog(this, "The selected reservation is invalid.",
                        "Reservation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Long resId = ((Number) reservationValue).longValue();

            ValidationResult res = customerFeatureService.requestService(
                    resId, u.getId(), u.getGuestId(),
                    (ServiceOrder.Category) catCombo.getSelectedItem(),
                    descField.getText(), "Deliver to room"
            );
            if (res.isValid()) {
                JOptionPane.showMessageDialog(this, "Your request has been dispatched to housekeeping & guest services!", "Request Logged", JOptionPane.INFORMATION_MESSAGE);
                refreshServiceOrders();
            } else {
                JOptionPane.showMessageDialog(this, res.getFormattedErrors(), "Notice", JOptionPane.WARNING_MESSAGE);
            }
        });

        card.add(new JLabel("Request Category:"));
        card.add(catCombo);
        card.add(new JLabel("Details:"));
        card.add(descField);
        card.add(reqBtn);
        panel.add(card, BorderLayout.NORTH);

        // Service requests history table
        String[] cols = {"Order #", "Category", "Description", "Assigned Staff", "Status", "Requested At"};
        serviceOrdersModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        serviceOrdersTable = new JTable(serviceOrdersModel);
        UITheme.styleTable(serviceOrdersTable);
        panel.add(new JScrollPane(serviceOrdersTable), BorderLayout.CENTER);

        refreshServiceOrders();
        return panel;
    }

    private void refreshServiceOrders() {
        new SwingWorker<List<ServiceOrder>, Void>() {
            @Override
            protected List<ServiceOrder> doInBackground() throws Exception {
                User user = SessionContext.getCurrentUser();
                if (user == null || user.getGuestId() == null) {
                    return java.util.Collections.emptyList();
                }
                return customerFeatureService.getServiceOrdersByGuest(user.getGuestId());
            }

            @Override
            protected void done() {
                try {
                    List<ServiceOrder> list = get();
                    serviceOrdersModel.setRowCount(0);
                    for (ServiceOrder s : list) {
                        serviceOrdersModel.addRow(new Object[]{
                                s.getOrderNumber(),
                                s.getCategory().getDisplayName(),
                                s.getDescription(),
                                s.getAssignedStaffName() != null ? s.getAssignedStaffName() : "In Queue",
                                s.getStatus().getDisplayName(),
                                DateUtil.formatDateTime(s.getCreatedAt())
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(CustomerDashboardFrame.this,
                            "Unable to load your service requests: " + ex.getMessage(),
                            "Service History Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // =========================================================================
    // 4. PREFERENCES & RECOMMENDATIONS PANEL
    // =========================================================================
    private JPanel createPreferencesPanel() {
    JPanel panel = new JPanel(new BorderLayout(15, 15));
    panel.setBackground(UITheme.BG_LIGHT);
    panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

    JPanel formCard = UITheme.createCardPanel();
    formCard.setLayout(new GridBagLayout());

    GridBagConstraints g = new GridBagConstraints();
    g.fill = GridBagConstraints.HORIZONTAL;
    g.insets = new Insets(8, 8, 8, 8);

    JComboBox<String> bedPref = new JComboBox<>(
            new String[]{"King Bed", "2 Twin Beds", "Any Bed Type"});

    JComboBox<String> floorPref = new JComboBox<>(
            new String[]{"High Floor", "Ground Floor", "No Preference"});

    JTextField dietaryField = new JTextField(20);
    JTextField notesField = new JTextField(20);

    User currentUser = SessionContext.getCurrentUser();

    // Load saved preferences when the tab is created.
    if (currentUser != null) {
        try {
            Optional<hotel.model.CustomerPreference> saved =
                    customerFeatureService.getPreferences(currentUser.getId());

            if (saved.isPresent()) {
                hotel.model.CustomerPreference cp = saved.get();

                if (cp.getPreferredBedType() != null) {
                    bedPref.setSelectedItem(cp.getPreferredBedType());
                }

                if (cp.getPreferredFloor() != null) {
                    floorPref.setSelectedItem(cp.getPreferredFloor());
                }

                dietaryField.setText(
                        cp.getDietaryNeeds() == null ? "" : cp.getDietaryNeeds());

                notesField.setText(
                        cp.getNotes() == null ? "" : cp.getNotes());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load preferences: " + ex.getMessage(),
                    "Preferences Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    int r = 0;
    addFormField(formCard, g, r++, "Preferred Bedding:", bedPref);
    addFormField(formCard, g, r++, "Preferred Floor:", floorPref);
    addFormField(formCard, g, r++, "Dietary Restrictions:", dietaryField);
    addFormField(formCard, g, r++, "Special Stay Notes:", notesField);

    JButton saveBtn = UITheme.createGoldButton("Save My Preferences");

    saveBtn.addActionListener(e -> {
        User user = SessionContext.getCurrentUser();

        if (user == null) {
            JOptionPane.showMessageDialog(
                    this, "Please log in again to save preferences.",
                    "Login Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            hotel.model.CustomerPreference cp =
                    customerFeatureService.getPreferences(user.getId())
                            .orElse(new hotel.model.CustomerPreference());

            cp.setUserId(user.getId());
            cp.setPreferredBedType((String) bedPref.getSelectedItem());
            cp.setPreferredFloor((String) floorPref.getSelectedItem());
            cp.setHighFloor("High Floor".equals(floorPref.getSelectedItem()));
            cp.setDietaryNeeds(dietaryField.getText().trim());
            cp.setNotes(notesField.getText().trim());

            customerFeatureService.savePreferences(cp);

            JOptionPane.showMessageDialog(
                    this,
                    "Preferences saved successfully!",
                    "Preferences Saved",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not save preferences: " + ex.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    });

    g.gridx = 1;
    g.gridy = r;
    formCard.add(saveBtn, g);

    panel.add(formCard, BorderLayout.NORTH);

    JLabel infoLabel = new JLabel(
            "Personalized suggestions based on your profile will appear on your room search results.");
    infoLabel.setFont(UITheme.FONT_SUBTITLE);
    infoLabel.setForeground(UITheme.NAVY_DARK);
    panel.add(infoLabel, BorderLayout.CENTER);

    return panel;
    }

    // =========================================================================
    // 5. LOCAL GUIDE & TRIP PLANNER PANEL
    // =========================================================================
    private JPanel createGuidePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] cols = {"Place Name", "Category", "Description", "Address", "Hours", "Duration", "Cost", "Local Tips"};
        guideModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        guideTable = new JTable(guideModel);
        UITheme.styleTable(guideTable);
        guideTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

guideTable.getSelectionModel().addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        int row = guideTable.getSelectedRow();

        if (row >= 0) {
            String placeName = guideModel.getValueAt(row, 0).toString();
            String category = guideModel.getValueAt(row, 1).toString();
            String description = guideModel.getValueAt(row, 2).toString();
            String address = guideModel.getValueAt(row, 3).toString();
            String hours = guideModel.getValueAt(row, 4).toString();
            String duration = guideModel.getValueAt(row, 5).toString();
            String cost = guideModel.getValueAt(row, 6).toString();
            String tips = guideModel.getValueAt(row, 7).toString();

            JOptionPane.showMessageDialog(this,
                    "Place: " + placeName
                    + "\nCategory: " + category
                    + "\nDescription: " + description
                    + "\nAddress: " + address
                    + "\nOpening Hours: " + hours
                    + "\nDuration: " + duration
                    + "\nEstimated Cost: " + cost
                    + "\nLocal Tips: " + tips,
                    "Local Guide Details",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
});
        panel.add(new JScrollPane(guideTable), BorderLayout.CENTER);

        new SwingWorker<List<LocalGuidePlace>, Void>() {
            @Override
            protected List<LocalGuidePlace> doInBackground() throws Exception {
                return localGuideDAO.findAllActive();
            }

            @Override
            protected void done() {
                try {
                    List<LocalGuidePlace> list = get();
                    guideModel.setRowCount(0);
                    for (LocalGuidePlace p : list) {
                        guideModel.addRow(new Object[]{
                                p.getName(),
                                p.getCategory(),
                                p.getDescription(),
                                p.getAddress(),
                                p.getOpeningHours(),
                                p.getEstimatedDurationMinutes() + " mins",
                                CurrencyUtil.format(p.getEstimatedCost()),
                                p.getRecommendedTips()
                        });
                    }
                } catch (Exception ex) {
    ex.printStackTrace();
    JOptionPane.showMessageDialog(
            CustomerDashboardFrame.this,
            "Unable to load local guide places:\n" + ex.getMessage(),
            "Local Guide Error",
            JOptionPane.ERROR_MESSAGE
    );
    }
            }
        }.execute();

        return panel;
    }
    private JPanel createTripPlannerPanel() {
    JPanel panel = new JPanel(new BorderLayout(15, 15));
    panel.setBackground(UITheme.BG_LIGHT);
    panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

    JPanel form = UITheme.createCardPanel();
    form.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 12));

    JLabel titleLabel = new JLabel("Trip Title:");
    titleLabel.setFont(UITheme.FONT_BOLD);
    JTextField titleField = new JTextField(15);

    JLabel startLabel = new JLabel("Start Date (YYYY-MM-DD):");
    startLabel.setFont(UITheme.FONT_BOLD);
    JTextField startField = new JTextField(
            LocalDate.now().plusDays(1).toString(), 10);

    JLabel endLabel = new JLabel("End Date (YYYY-MM-DD):");
    endLabel.setFont(UITheme.FONT_BOLD);
    JTextField endField = new JTextField(
            LocalDate.now().plusDays(3).toString(), 10);

    JLabel notesLabel = new JLabel("Notes:");
    notesLabel.setFont(UITheme.FONT_BOLD);
    JTextField notesField = new JTextField(15);

    JButton createButton =
            UITheme.createPrimaryButton("Create Itinerary");

    form.add(titleLabel);
    form.add(titleField);
    form.add(startLabel);
    form.add(startField);
    form.add(endLabel);
    form.add(endField);
    form.add(notesLabel);
    form.add(notesField);
    form.add(createButton);

    String[] columns = {
        "ID", "Trip Title", "Start Date", "End Date", "Notes"
    };

    DefaultTableModel model = new DefaultTableModel(columns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    JTable table = new JTable(model);
    UITheme.styleTable(table);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    table.getColumnModel().getColumn(0).setMinWidth(0);
    table.getColumnModel().getColumn(0).setMaxWidth(0);

    JButton addActivityButton = UITheme.createPrimaryButton("Add Activity");
    JButton viewActivitiesButton = UITheme.createGoldButton("View Activities");
    JButton deleteItineraryButton = UITheme.createPrimaryButton("Delete Itinerary");
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
    actions.setOpaque(false);
    actions.add(addActivityButton);
    actions.add(viewActivitiesButton);
    actions.add(deleteItineraryButton);

    createButton.addActionListener(e -> {
        User user = SessionContext.getCurrentUser();

        if (user == null || user.getId() == null) {
            JOptionPane.showMessageDialog(
                    this, "Please log in again.",
                    "Session Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            LocalDate startDate =
                    LocalDate.parse(startField.getText().trim());
            LocalDate endDate =
                    LocalDate.parse(endField.getText().trim());

            tripItineraryService.createItinerary(
                    user.getId(),
                    titleField.getText(),
                    startDate,
                    endDate,
                    notesField.getText()
            );

            JOptionPane.showMessageDialog(
                    this, "Itinerary created successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            titleField.setText("");
            notesField.setText("");

            refreshItineraryTable(model);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Unable to Create Itinerary",
                    JOptionPane.WARNING_MESSAGE);
        }
    });

    addActivityButton.addActionListener(e -> {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an itinerary first.",
                    "Trip Planner", JOptionPane.WARNING_MESSAGE);
            return;
        }
        User user = SessionContext.getCurrentUser();
        if (user == null || user.getId() == null) return;
        int modelRow = table.convertRowIndexToModel(selectedRow);
        Long itineraryId = ((Number) model.getValueAt(modelRow, 0)).longValue();
        try {
            TripItinerary selected = tripItineraryService.getMyItineraries(user.getId())
                    .stream().filter(it -> it.getId().equals(itineraryId)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Itinerary not found."));
            String activity = JOptionPane.showInputDialog(this, "Activity name:", "Add Activity", JOptionPane.PLAIN_MESSAGE);
            if (activity == null) return;
            String dateText = JOptionPane.showInputDialog(this, "Activity date (YYYY-MM-DD):", selected.getStartDate().toString());
            if (dateText == null) return;
            String timeText = JOptionPane.showInputDialog(this, "Activity time (e.g. 10:30 AM):", "10:00 AM");
            if (timeText == null) return;
            String notesText = JOptionPane.showInputDialog(this, "Notes (optional):", "");
            if (notesText == null) return;

            ItineraryItem item = new ItineraryItem();
            item.setActivityName(activity.trim());
            item.setPlannedDate(LocalDate.parse(dateText.trim()));
            item.setPlannedTime(timeText.trim());
            item.setNotes(notesText.trim());
            tripItineraryService.addActivity(itineraryId, user.getId(), item);
            JOptionPane.showMessageDialog(this, "Activity saved to your itinerary.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Unable to Add Activity",
                    JOptionPane.WARNING_MESSAGE);
        }
    });

    viewActivitiesButton.addActionListener(e -> {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an itinerary first.");
            return;
        }
        User user = SessionContext.getCurrentUser();
        if (user == null || user.getId() == null) return;
        int modelRow = table.convertRowIndexToModel(selectedRow);
        Long itineraryId = ((Number) model.getValueAt(modelRow, 0)).longValue();
        try {
            TripItinerary selected = tripItineraryService.getMyItineraries(user.getId())
                    .stream().filter(it -> it.getId().equals(itineraryId)).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Itinerary not found."));
            String[] activityColumns = {"Date", "Time", "Activity", "Notes"};
            DefaultTableModel activityModel = new DefaultTableModel(activityColumns, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            for (ItineraryItem item : selected.getItems()) {
                activityModel.addRow(new Object[]{item.getPlannedDate(), item.getPlannedTime(),
                        item.getActivityName(), item.getNotes()});
            }
            JTable activityTable = new JTable(activityModel);
            UITheme.styleTable(activityTable);
            JOptionPane.showMessageDialog(this, new JScrollPane(activityTable),
                    "Activities — " + selected.getTitle(), JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Unable to Load Activities",
                    JOptionPane.ERROR_MESSAGE);
        }
    });

    deleteItineraryButton.addActionListener(e -> {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an itinerary first.");
            return;
        }
        User user = SessionContext.getCurrentUser();
        if (user == null || user.getId() == null) return;
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete this itinerary and all its activities?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;
        int modelRow = table.convertRowIndexToModel(selectedRow);
        Long itineraryId = ((Number) model.getValueAt(modelRow, 0)).longValue();
        try {
            tripItineraryService.deleteItinerary(itineraryId, user.getId());
            refreshItineraryTable(model);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Unable to Delete Itinerary",
                    JOptionPane.ERROR_MESSAGE);
        }
    });

    panel.add(form, BorderLayout.NORTH);
    panel.add(new JScrollPane(table), BorderLayout.CENTER);
    panel.add(actions, BorderLayout.SOUTH);

    refreshItineraryTable(model);

    return panel;
}

private void refreshItineraryTable(DefaultTableModel model) {
    User user = SessionContext.getCurrentUser();
    model.setRowCount(0);

    if (user == null || user.getId() == null) {
        return;
    }

    new SwingWorker<List<TripItinerary>, Void>() {

        @Override
        protected List<TripItinerary> doInBackground() throws Exception {
            return tripItineraryService.getMyItineraries(user.getId());
        }

        @Override
        protected void done() {
            try {
                List<TripItinerary> itineraries = get();

                for (TripItinerary itinerary : itineraries) {
                    model.addRow(new Object[]{
                        itinerary.getId(),
                        itinerary.getTitle(),
                        itinerary.getStartDate(),
                        itinerary.getEndDate(),
                        itinerary.getNotes()
                    });
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        CustomerDashboardFrame.this,
                        "Unable to load itineraries: " + ex.getMessage(),
                        "Trip Planner Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }.execute();
}
    


    // =========================================================================
    // 6. WAITLIST PANEL
    // =========================================================================
    private JPanel createWaitlistPanel() {
    JPanel panel = new JPanel(new BorderLayout(15, 15));
    panel.setBackground(UITheme.BG_LIGHT);
    panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

    JPanel joinCard = UITheme.createCardPanel();
    joinCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

    JComboBox<String> roomTypeWait = new JComboBox<>(new String[]{
        "Deluxe Ocean View Suite",
        "Executive Presidential Suite",
        "Standard King Room"
    });

    JTextField inW = new JTextField(
        LocalDate.now().plusDays(5).toString(), 10);
    JTextField outW = new JTextField(
        LocalDate.now().plusDays(7).toString(), 10);

    JButton joinBtn = UITheme.createPrimaryButton("Join Waitlist");

    joinBtn.addActionListener(e -> {
        User user = SessionContext.getCurrentUser();

        if (user == null || user.getGuestId() == null) {
            JOptionPane.showMessageDialog(
                this,
                "Please log in with a customer account before joining the waitlist.",
                "Login Required",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        LocalDate checkIn;
        LocalDate checkOut;

        try {
            checkIn = LocalDate.parse(inW.getText().trim());
            checkOut = LocalDate.parse(outW.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Enter valid dates in YYYY-MM-DD format.",
                "Invalid Dates",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String selectedType = (String) roomTypeWait.getSelectedItem();
        if (selectedType == null) {
            JOptionPane.showMessageDialog(
                this, "Please select a room category.",
                "Room Category Required", JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            Long roomTypeId = null;

            for (RoomType type : roomTypeDAO.findAllActive()) {
                if (type.getName().equals(selectedType)) {
                    roomTypeId = type.getId();
                    break;
                }
            }

            if (roomTypeId == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "The selected room category could not be found.",
                    "Room Category Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            ValidationResult result = customerFeatureService.joinWaitlist(
                user.getId(),
                user.getGuestId(),
                roomTypeId,
                checkIn,
                checkOut,
                1,
                ""
            );

            if (result.isValid()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Your waitlist request has been saved.",
                    "Waitlist Joined",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    result.getFormattedErrors(),
                    "Waitlist Request Failed",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Could not save the waitlist request: " + ex.getMessage(),
                "Waitlist Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    });

    joinCard.add(new JLabel("Room Category:"));
    joinCard.add(roomTypeWait);
    joinCard.add(new JLabel("Check-in:"));
    joinCard.add(inW);
    joinCard.add(new JLabel("Check-out:"));
    joinCard.add(outW);
    joinCard.add(joinBtn);

    panel.add(joinCard, BorderLayout.NORTH);

    JPanel historyPanel = new JPanel(new BorderLayout(10, 10));
historyPanel.setBackground(UITheme.BG_LIGHT);

JLabel historyTitle = new JLabel("My Waitlist Requests");
historyTitle.setFont(UITheme.FONT_BOLD);
historyPanel.add(historyTitle, BorderLayout.NORTH);

String[] columns = {
    "Request ID", "Room Category", "Check-in",
    "Check-out", "Guests", "Status"
};

DefaultTableModel waitlistModel = new DefaultTableModel(columns, 0) {
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
};

JTable waitlistTable = new JTable(waitlistModel);
UITheme.styleTable(waitlistTable);
historyPanel.add(new JScrollPane(waitlistTable), BorderLayout.CENTER);

JButton refreshWaitlist = UITheme.createSecondaryButton("Refresh Requests");

refreshWaitlist.addActionListener(e -> {
    User currentUser = SessionContext.getCurrentUser();

    if (currentUser == null) return;

    new SwingWorker<List<WaitlistEntry>, Void>() {
        @Override
        protected List<WaitlistEntry> doInBackground() throws Exception {
            return new WaitlistDAO().findByUserId(currentUser.getId());
        }

        @Override
        protected void done() {
            try {
                waitlistModel.setRowCount(0);

                for (WaitlistEntry entry : get()) {
                    waitlistModel.addRow(new Object[]{
                        entry.getId(),
                        entry.getRoomTypeName(),
                        entry.getDesiredCheckIn(),
                        entry.getDesiredCheckOut(),
                        entry.getNumberOfGuests(),
                        entry.getStatus().getDisplayName()
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    CustomerDashboardFrame.this,
                    "Could not load waitlist requests: " + ex.getMessage(),
                    "Waitlist Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }.execute();
});

historyPanel.add(refreshWaitlist, BorderLayout.SOUTH);

panel.add(historyPanel, BorderLayout.CENTER);

refreshWaitlist.doClick();

    return panel;
    }
    private void addFormField(
        JPanel panel,
        GridBagConstraints gbc,
        int row,
        String label,
        java.awt.Component field) {

    gbc.gridy = row;
    gbc.gridx = 0;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.LINE_END;
    gbc.insets = new Insets(5, 5, 5, 10);

    panel.add(new JLabel(label), gbc);

    gbc.gridx = 1;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.insets = new Insets(5, 5, 5, 5);

    panel.add(field, gbc);
}
}
