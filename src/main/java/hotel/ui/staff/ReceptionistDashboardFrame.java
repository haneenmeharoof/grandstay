package hotel.ui.staff;

import hotel.dao.GuestDAO;
import hotel.dao.InvoiceDAO;
import hotel.dao.PreCheckinDAO;
import hotel.dao.ReservationDAO;
import hotel.dao.RoomDAO;
import hotel.dao.RoomTypeDAO;
import hotel.model.Guest;
import hotel.model.Invoice;
import hotel.model.PaymentMethod;
import hotel.model.PreCheckin;
import hotel.model.Reservation;
import hotel.model.ReservationStatus;
import hotel.model.Room;
import hotel.model.RoomStatus;
import hotel.model.RoomType;
import hotel.model.User;
import hotel.service.BillingService;
import hotel.service.CustomerFeatureService;
import hotel.service.ReservationService;
import hotel.ui.common.HeaderPanel;
import hotel.ui.common.StatCard;
import hotel.ui.common.UITheme;
import hotel.util.CsvExporter;
import hotel.util.CurrencyUtil;
import hotel.util.DateUtil;
import hotel.util.InvoiceGenerator;
import hotel.util.SessionContext;
import hotel.validation.ValidationResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
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
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class ReceptionistDashboardFrame extends JFrame {

    private final ReservationService reservationService = new ReservationService();
    private final BillingService billingService = new BillingService();
    private final CustomerFeatureService customerFeatureService = new CustomerFeatureService();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final RoomTypeDAO roomTypeDAO = new RoomTypeDAO();
    private final GuestDAO guestDAO = new GuestDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final PreCheckinDAO preCheckinDAO = new PreCheckinDAO();

    // Tables
    private JTable arrivalsTable;
    private DefaultTableModel arrivalsModel;

    private JTable departuresTable;
    private DefaultTableModel departuresModel;

    private JTable searchResTable;
    private DefaultTableModel searchResModel;
    private JTable billingTable;
    private DefaultTableModel billingModel;

    private JTable preCheckinTable;
    private DefaultTableModel preCheckinModel;

    private JPanel roomMatrixPanel;

    public ReceptionistDashboardFrame() {
        setTitle("GrandStay Hotel - Front Desk & Reception Dashboard");
        setSize(1240, 780);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Top Bar
        add(new HeaderPanel("Front Desk Operations"), BorderLayout.NORTH);

        // Tabbed Workflows
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.setBackground(Color.WHITE);

        tabs.addTab("  Front Desk Today (Arrivals & Departures)  ", createTodayPanel());
        tabs.addTab("  Reservations & Walk-in  ", createReservationsPanel());
        tabs.addTab("  Billing & Payments  ", createBillingPanel());
        tabs.addTab("  Pre-Check-in Queue  ", createPreCheckinPanel());
        tabs.addTab("  Live Room Status Matrix  ", createRoomMatrixTab());

        add(tabs, BorderLayout.CENTER);

        refreshArrivalsAndDepartures();
    }

    // =========================================================================
    // 1. TODAY'S FRONT DESK PANEL (Arrivals & Departures)
    // =========================================================================
    private JPanel createTodayPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Two Split Grids: Top for Arrivals, Bottom for Departures
        JPanel splitPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        splitPanel.setOpaque(false);

        // Arrivals Section
        JPanel arrivalsPanel = UITheme.createCardPanel();
        arrivalsPanel.setLayout(new BorderLayout(5, 8));
        JLabel arrHeader = new JLabel("Today's Scheduled Arrivals");
        arrHeader.setFont(UITheme.FONT_SUBTITLE);
        arrHeader.setForeground(UITheme.NAVY_DARK);
        arrivalsPanel.add(arrHeader, BorderLayout.NORTH);

        String[] arrCols = {"Res ID", "Booking Ref #", "Guest Name", "Room #", "Nights", "Total Due", "Balance Due", "Status"};
        arrivalsModel = new DefaultTableModel(arrCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        arrivalsTable = new JTable(arrivalsModel);
        UITheme.styleTable(arrivalsTable);
        arrivalsTable.getColumnModel().getColumn(0).setMinWidth(0);
        arrivalsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        arrivalsPanel.add(new JScrollPane(arrivalsTable), BorderLayout.CENTER);

        JButton checkInBtn = UITheme.createGoldButton("✦ Complete Guest Check-In");
        checkInBtn.addActionListener(e -> handleFastCheckIn());
        JPanel arrActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        arrActions.setOpaque(false);
        arrActions.add(checkInBtn);
        arrivalsPanel.add(arrActions, BorderLayout.SOUTH);

        // Departures Section
        JPanel departuresPanel = UITheme.createCardPanel();
        departuresPanel.setLayout(new BorderLayout(5, 8));
        JLabel depHeader = new JLabel("Today's Scheduled Departures");
        depHeader.setFont(UITheme.FONT_SUBTITLE);
        depHeader.setForeground(UITheme.NAVY_DARK);
        departuresPanel.add(depHeader, BorderLayout.NORTH);

        String[] depCols = {"Res ID", "Booking Ref #", "Guest Name", "Room #", "Check-in", "Total Amount", "Balance Due", "Status"};
        departuresModel = new DefaultTableModel(depCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        departuresTable = new JTable(departuresModel);
        UITheme.styleTable(departuresTable);
        departuresTable.getColumnModel().getColumn(0).setMinWidth(0);
        departuresTable.getColumnModel().getColumn(0).setMaxWidth(0);
        departuresPanel.add(new JScrollPane(departuresTable), BorderLayout.CENTER);

        JButton checkOutBtn = UITheme.createPrimaryButton("Process Guest Check-Out");
        checkOutBtn.addActionListener(e -> handleFastCheckOut());
        JPanel depActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        depActions.setOpaque(false);
        depActions.add(checkOutBtn);
        departuresPanel.add(depActions, BorderLayout.SOUTH);

        splitPanel.add(arrivalsPanel);
        splitPanel.add(departuresPanel);

        panel.add(splitPanel, BorderLayout.CENTER);
        return panel;
    }

    private void refreshArrivalsAndDepartures() {
        new SwingWorker<Void, Void>() {
            List<Reservation> arrivals;
            List<Reservation> departures;

            @Override
            protected Void doInBackground() throws Exception {
                arrivals = reservationDAO.findTodayArrivals();
                departures = reservationDAO.findTodayDepartures();
                return null;
            }

            @Override
            protected void done() {
                arrivalsModel.setRowCount(0);
                for (Reservation r : arrivals) {
                    arrivalsModel.addRow(new Object[]{
                            r.getId(),
                            r.getReferenceNumber(),
                            r.getGuest().getFullName(),
                            r.getRoom() != null ? r.getRoom().getRoomNumber() : "Unassigned",
                            r.getNights(),
                            CurrencyUtil.format(r.getTotalAmount()),
                            CurrencyUtil.format(r.getBalanceDue()),
                            r.getStatus().getDisplayName()
                    });
                }

                departuresModel.setRowCount(0);
                for (Reservation r : departures) {
                    departuresModel.addRow(new Object[]{
                            r.getId(),
                            r.getReferenceNumber(),
                            r.getGuest().getFullName(),
                            r.getRoom() != null ? r.getRoom().getRoomNumber() : "TBD",
                            DateUtil.formatDate(r.getCheckInDate()),
                            CurrencyUtil.format(r.getTotalAmount()),
                            CurrencyUtil.format(r.getBalanceDue()),
                            r.getStatus().getDisplayName()
                    });
                }
            }
        }.execute();
    }

    private void handleFastCheckIn() {
        int row = arrivalsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an arrival from the list.", "Select Guest", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long resId = (Long) arrivalsTable.getValueAt(row, 0);
        String name = (String) arrivalsTable.getValueAt(row, 2);
        String status = String.valueOf(arrivalsTable.getValueAt(row, 7));
        if (status.equalsIgnoreCase("Checked In")) {
            JOptionPane.showMessageDialog(this, name + " is already checked in.", "Already Checked In", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm check-in for guest " + name + "?\nRoom will be assigned and marked as OCCUPIED.",
                "Confirm Check-in", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        ValidationResult res = reservationService.checkIn(resId);
        if (res.isValid()) {
            JOptionPane.showMessageDialog(this, "Guest checked in successfully! Key card issued.", "Check-in Successful", JOptionPane.INFORMATION_MESSAGE);
            refreshArrivalsAndDepartures();
            refreshBillingReservations();
            refreshRoomMatrix();
        } else {
            JOptionPane.showMessageDialog(this, res.getFormattedErrors(), "Check-in Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleFastCheckOut() {
        int row = departuresTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a guest departure to check out.", "Select Guest", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long resId = (Long) departuresTable.getValueAt(row, 0);
        String ref = (String) departuresTable.getValueAt(row, 1);
        String balStr = String.valueOf(departuresTable.getValueAt(row, 6));
        String status = String.valueOf(departuresTable.getValueAt(row, 7));
        if (status.equalsIgnoreCase("Checked Out")) {
            JOptionPane.showMessageDialog(this, "This guest has already checked out.", "Already Checked Out", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (!"$0.00".equals(balStr)) {
            JOptionPane.showMessageDialog(this,
                    "Guest has an outstanding balance of " + balStr + ".\nPlease settle the folio in Billing & Payments tab before checking out.",
                    "Outstanding Balance", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ValidationResult res = reservationService.checkOut(resId);
        if (res.isValid()) {
            JOptionPane.showMessageDialog(this,
                    "Guest successfully checked out!\nRoom marked as DIRTY. Housekeeping turnover task has been dispatched.",
                    "Check-out Complete", JOptionPane.INFORMATION_MESSAGE);
            refreshArrivalsAndDepartures();
            refreshBillingReservations();
            refreshRoomMatrix();
        } else {
            JOptionPane.showMessageDialog(this, res.getFormattedErrors(), "Check-out Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // 2. RESERVATIONS & WALK-IN PANEL
    // =========================================================================
    private JPanel createReservationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Search Bar Card
        JPanel searchCard = UITheme.createCardPanel();
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));

        JTextField searchField = new JTextField(20);
        JButton searchBtn = UITheme.createPrimaryButton("Search Reservations");
        searchBtn.addActionListener(e -> searchReservations(searchField.getText().trim()));

        JButton newWalkInBtn = UITheme.createGoldButton("✦ New Walk-in Reservation");
        newWalkInBtn.addActionListener(e -> openWalkInDialog());

        JButton assignRoomBtn = UITheme.createSecondaryButton("Reassign Room");
        assignRoomBtn.addActionListener(e -> handleReassignRoom());

        searchCard.add(new JLabel("Search (Guest, Ref, Room):"));
        searchCard.add(searchField);
        searchCard.add(searchBtn);
        searchCard.add(newWalkInBtn);
        searchCard.add(assignRoomBtn);

        panel.add(searchCard, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Booking Ref #", "Guest Name", "Email", "Phone", "Room #", "Check-in", "Check-out", "Total", "Paid", "Balance", "Status"};
        searchResModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        searchResTable = new JTable(searchResModel);
        UITheme.styleTable(searchResTable);
        searchResTable.getColumnModel().getColumn(0).setMinWidth(0);
        searchResTable.getColumnModel().getColumn(0).setMaxWidth(0);

        panel.add(new JScrollPane(searchResTable), BorderLayout.CENTER);

        searchReservations("");
        return panel;
    }

    private void searchReservations(String query) {
        new SwingWorker<List<Reservation>, Void>() {
            @Override
            protected List<Reservation> doInBackground() throws Exception {
                return reservationDAO.search(query);
            }

            @Override
            protected void done() {
                try {
                    List<Reservation> list = get();
                    searchResModel.setRowCount(0);
                    for (Reservation r : list) {
                        searchResModel.addRow(new Object[]{
                                r.getId(),
                                r.getReferenceNumber(),
                                r.getGuest().getFullName(),
                                r.getGuest().getEmail(),
                                r.getGuest().getPhone(),
                                r.getRoom() != null ? r.getRoom().getRoomNumber() : "TBD",
                                DateUtil.formatDate(r.getCheckInDate()),
                                DateUtil.formatDate(r.getCheckOutDate()),
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

    private void openWalkInDialog() {
        JDialog d = new JDialog(this, "Front Desk Walk-In Reservation", true);
        d.setSize(520, 520);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 5, 5, 5);

        JTextField fnField = new JTextField(15);
        JTextField lnField = new JTextField(15);
        JTextField emField = new JTextField(15);
        JTextField phField = new JTextField(15);
        JTextField inField = new JTextField(LocalDate.now().toString(), 15);
        JTextField outField = new JTextField(LocalDate.now().plusDays(1).toString(), 15);
        JSpinner guestsSpin = new JSpinner(new SpinnerNumberModel(1, 1, 6, 1));
        JComboBox<Room> roomCombo = new JComboBox<>();

        // Populate available rooms
        try {
            List<Room> available = roomDAO.findAvailableRooms(LocalDate.now(), LocalDate.now().plusDays(1), null, 1);
            for (Room r : available) roomCombo.addItem(r);
        } catch (Exception ignored) {}

        int r = 0;
        addWalkInField(form, g, r++, "First Name *:", fnField);
        addWalkInField(form, g, r++, "Last Name *:", lnField);
        addWalkInField(form, g, r++, "Email Address *:", emField);
        addWalkInField(form, g, r++, "Phone Number *:", phField);
        addWalkInField(form, g, r++, "Check-in Date:", inField);
        addWalkInField(form, g, r++, "Check-out Date:", outField);
        addWalkInField(form, g, r++, "Number of Guests:", guestsSpin);
        addWalkInField(form, g, r++, "Assign Room *:", roomCombo);

        d.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = UITheme.createSecondaryButton("Cancel");
        cancel.addActionListener(e -> d.dispose());

        JButton book = UITheme.createGoldButton("Create & Confirm Walk-in");
        book.addActionListener(e -> {
            try {
                if (roomCombo.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(d, "No available room selected.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Room selectedRoom = (Room) roomCombo.getSelectedItem();
                LocalDate cIn = LocalDate.parse(inField.getText().trim());
                LocalDate cOut = LocalDate.parse(outField.getText().trim());

                // Create or find guest
                Guest guest = new Guest(fnField.getText().trim(), lnField.getText().trim(), emField.getText().trim(), phField.getText().trim());
                Long guestId = guestDAO.insert(guest);

                ReservationService.BookingResult result = reservationService.createReservation(
                        guestId, null, selectedRoom.getId(), cIn, cOut, (Integer) guestsSpin.getValue(), "Walk-in guest", BigDecimal.ZERO
                );

                if (result.isSuccess()) {
                    JOptionPane.showMessageDialog(d, "Walk-in booked! Ref: " + result.getReferenceNumber(), "Success", JOptionPane.INFORMATION_MESSAGE);
                    d.dispose();
                    searchReservations("");
                    refreshBillingReservations();
                    refreshArrivalsAndDepartures();
                } else {
                    JOptionPane.showMessageDialog(d, result.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(cancel);
        footer.add(book);
        d.add(footer, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void addWalkInField(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BOLD);
        panel.add(l, gbc);

        gbc.gridx = 1;
        panel.add(comp, gbc);
    }

    private void handleReassignRoom() {
        int selected = searchResTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select a reservation to reassign room.", "Select Reservation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long resId = (Long) searchResTable.getValueAt(selected, 0);
        String roomNumber = JOptionPane.showInputDialog(this, "Enter New Room Number:", "Reassign Room", JOptionPane.QUESTION_MESSAGE);
        if (roomNumber == null || roomNumber.trim().isEmpty()) return;

        try {
            Optional<Room> targetRoomOpt = roomDAO.findByRoomNumber(roomNumber.trim());
            if (targetRoomOpt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Room " + roomNumber + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            reservationDAO.updateRoomAssignment(resId, targetRoomOpt.get().getId());
            JOptionPane.showMessageDialog(this, "Room reassigned to " + roomNumber, "Success", JOptionPane.INFORMATION_MESSAGE);
            searchReservations("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error reassigning room: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // 3. BILLING & PAYMENTS PANEL
    // =========================================================================
    private JPanel createBillingPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel toolbar = UITheme.createCardPanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JButton recordPayBtn = UITheme.createGoldButton("✦ Record Payment");
        recordPayBtn.addActionListener(e -> openRecordPaymentDialog());
        JButton viewInvoiceBtn = UITheme.createSecondaryButton("View / Print Folio");
        viewInvoiceBtn.addActionListener(e -> openInvoiceFolio());
        JButton exportCsvBtn = UITheme.createSecondaryButton("Export Reservations to CSV");
        exportCsvBtn.addActionListener(e -> handleExportCsv());
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh Reservations");
        refreshBtn.addActionListener(e -> refreshBillingReservations());
        toolbar.add(recordPayBtn);
        toolbar.add(viewInvoiceBtn);
        toolbar.add(exportCsvBtn);
        toolbar.add(refreshBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Res ID", "Booking Ref #", "Guest Name", "Email", "Phone", "Room #",
                "Check-in", "Check-out", "Total", "Paid", "Balance", "Status"};
        billingModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        billingTable = new JTable(billingModel);
        UITheme.styleTable(billingTable);
        billingTable.getColumnModel().getColumn(0).setMinWidth(0);
        billingTable.getColumnModel().getColumn(0).setMaxWidth(0);
        panel.add(new JScrollPane(billingTable), BorderLayout.CENTER);

        JLabel info = new JLabel("Select a guest reservation above to record a payment or open its itemized folio.");
        info.setFont(UITheme.FONT_REGULAR);
        info.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(info, BorderLayout.SOUTH);
        refreshBillingReservations();
        return panel;
    }

    private void refreshBillingReservations() {
        if (billingModel == null) return;
        new SwingWorker<List<Reservation>, Void>() {
            @Override protected List<Reservation> doInBackground() throws Exception {
                return reservationDAO.search("");
            }
            @Override protected void done() {
                try {
                    billingModel.setRowCount(0);
                    for (Reservation r : get()) {
                        billingModel.addRow(new Object[]{r.getId(), r.getReferenceNumber(),
                                r.getGuest().getFullName(), r.getGuest().getEmail(), r.getGuest().getPhone(),
                                r.getRoom() != null ? r.getRoom().getRoomNumber() : "TBD",
                                DateUtil.formatDate(r.getCheckInDate()), DateUtil.formatDate(r.getCheckOutDate()),
                                CurrencyUtil.format(r.getTotalAmount()), CurrencyUtil.format(r.getPaidAmount()),
                                CurrencyUtil.format(r.getBalanceDue()), r.getStatus().getDisplayName()});
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ReceptionistDashboardFrame.this,
                            "Could not load billing reservations: " + ex.getMessage(), "Billing Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private JTable getSelectedReservationTable() {
        if (billingTable != null && billingTable.getSelectedRow() >= 0) return billingTable;
        if (searchResTable != null && searchResTable.getSelectedRow() >= 0) return searchResTable;
        return null;
    }

    private void openRecordPaymentDialog() {
        JTable selectedTable = getSelectedReservationTable();
        if (selectedTable == null) {
            JOptionPane.showMessageDialog(this, "Select a guest reservation in this tab first.", "Select Reservation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selected = selectedTable.convertRowIndexToModel(selectedTable.getSelectedRow());
        Long resId = (Long) selectedTable.getModel().getValueAt(selected, 0);
        String ref = (String) selectedTable.getModel().getValueAt(selected, 1);
        String name = (String) selectedTable.getModel().getValueAt(selected, 2);
        String balanceStr = (String) selectedTable.getModel().getValueAt(selected, 10);

        JDialog d = new JDialog(this, "Record Payment - #" + ref, true);
        d.setSize(420, 320);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 6, 6, 6);

        JLabel guestL = new JLabel(name + " (Balance: " + balanceStr + ")");
        guestL.setFont(UITheme.FONT_BOLD);
        JTextField amtField = new JTextField(balanceStr.replace("$", "").replace(",", ""), 12);
        JComboBox<PaymentMethod> methodCombo = new JComboBox<>(PaymentMethod.values());
        JTextField notesField = new JTextField("Front desk payment", 12);

        int r = 0;
        addWalkInField(form, g, r++, "Guest:", guestL);
        addWalkInField(form, g, r++, "Amount to Pay * ($):", amtField);
        addWalkInField(form, g, r++, "Payment Method *:", methodCombo);
        addWalkInField(form, g, r++, "Transaction Notes:", notesField);

        d.add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancel = UITheme.createSecondaryButton("Cancel");
        cancel.addActionListener(e -> d.dispose());

        JButton confirm = UITheme.createGoldButton("Save Payment");
        confirm.addActionListener(e -> {
            try {
                BigDecimal amt = new BigDecimal(amtField.getText().trim());
                ValidationResult res = billingService.recordPayment(
                        resId, amt, (PaymentMethod) methodCombo.getSelectedItem(), notesField.getText()
                );
                if (res.isValid()) {
                    JOptionPane.showMessageDialog(d, "Payment of " + CurrencyUtil.format(amt) + " recorded successfully!", "Payment Complete", JOptionPane.INFORMATION_MESSAGE);
                    d.dispose();
                    searchReservations("");
                    refreshBillingReservations();
                    refreshArrivalsAndDepartures();
                } else {
                    JOptionPane.showMessageDialog(d, res.getFormattedErrors(), "Payment Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Please enter a valid amount (e.g. 150.00)", "Invalid Amount", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(cancel);
        footer.add(confirm);
        d.add(footer, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void openInvoiceFolio() {
        JTable selectedTable = getSelectedReservationTable();
        if (selectedTable == null) {
            JOptionPane.showMessageDialog(this, "Select a guest reservation from the table first.", "Select Reservation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int selected = selectedTable.convertRowIndexToModel(selectedTable.getSelectedRow());
        Long resId = (Long) selectedTable.getModel().getValueAt(selected, 0);
        try {
            Optional<Invoice> invOpt = invoiceDAO.findByReservationId(resId);
            Optional<Reservation> resOpt = reservationDAO.findById(resId);
            if (invOpt.isEmpty() || resOpt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Folio not found.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String html = InvoiceGenerator.generateHtmlInvoice(invOpt.get(), resOpt.get(), resOpt.get().getGuest());

            JDialog dialog = new JDialog(this, "Guest Folio - " + invOpt.get().getInvoiceNumber(), true);
            dialog.setSize(680, 720);
            dialog.setLocationRelativeTo(this);

            JEditorPane pane = new JEditorPane();
            pane.setContentType("text/html");
            pane.setEditable(false);
            pane.setText(html);

            dialog.add(new JScrollPane(pane), BorderLayout.CENTER);

            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton close = UITheme.createPrimaryButton("Close");
            close.addActionListener(e -> dialog.dispose());
            foot.add(close);
            dialog.add(foot, BorderLayout.SOUTH);

            dialog.setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error generating folio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleExportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("reservations_export.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                CsvExporter.exportTableToCsv(searchResTable, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Reservations exported successfully to " + chooser.getSelectedFile().getName(), "CSV Exported", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export error: " + ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // 4. PRE-CHECK-IN QUEUE PANEL
    // =========================================================================
    private JPanel createPreCheckinPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] cols = {"ID", "Booking Ref #", "Expected Arrival", "ID Document", "Document #", "Emergency Contact", "Status"};
        preCheckinModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        preCheckinTable = new JTable(preCheckinModel);
        UITheme.styleTable(preCheckinTable);
        panel.add(new JScrollPane(preCheckinTable), BorderLayout.CENTER);

        JPanel actions = UITheme.createCardPanel();
        actions.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 8));

        JButton approveBtn = UITheme.createGoldButton("Approve Pre-Check-in");
        approveBtn.addActionListener(e -> handleReviewPreCheckin(PreCheckin.Status.APPROVED));

        JButton rejectBtn = UITheme.createDangerButton("Reject / Request Update");
        rejectBtn.addActionListener(e -> handleReviewPreCheckin(PreCheckin.Status.REJECTED));

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh Queue");
        refreshBtn.addActionListener(e -> refreshPreCheckinQueue());

        actions.add(refreshBtn);
        actions.add(rejectBtn);
        actions.add(approveBtn);
        panel.add(actions, BorderLayout.SOUTH);

        refreshPreCheckinQueue();
        return panel;
    }

    private void refreshPreCheckinQueue() {
        new SwingWorker<List<PreCheckin>, Void>() {
            @Override
            protected List<PreCheckin> doInBackground() throws Exception {
                return customerFeatureService.getPendingPreCheckins();
            }

            @Override
            protected void done() {
                try {
                    List<PreCheckin> list = get();
                    preCheckinModel.setRowCount(0);
                    for (PreCheckin p : list) {
                        preCheckinModel.addRow(new Object[]{
                                p.getId(),
                                p.getReservationRef(),
                                p.getExpectedArrivalTime(),
                                p.getIdDocumentType(),
                                p.getIdDocumentNumber(),
                                p.getEmergencyContactName() + " (" + p.getEmergencyContactPhone() + ")",
                                p.getStatus().getDisplayName()
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void handleReviewPreCheckin(PreCheckin.Status status) {
        int selected = preCheckinTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select a pre-check-in submission first.", "Select Row", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long pcId = (Long) preCheckinTable.getValueAt(selected, 0);
        User actor = SessionContext.getCurrentUser();
        Long actorId = actor != null ? actor.getId() : 1L;

        if (status == PreCheckin.Status.APPROVED) {
            try {
                java.util.Optional<PreCheckin> submissionOpt = preCheckinDAO.findById(pcId);
                if (submissionOpt.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Pre-check-in submission was not found.", "Approval Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                PreCheckin submission = submissionOpt.get();
                java.util.Optional<Reservation> reservationOpt = reservationDAO.findByReferenceNumber(submission.getReservationRef());
                if (reservationOpt.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "The linked reservation was not found.", "Approval Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Reservation reservation = reservationOpt.get();
                if (!LocalDate.now().equals(reservation.getCheckInDate())) {
                    JOptionPane.showMessageDialog(this, "This guest can only be checked in on the reservation arrival date.", "Check-In Not Available", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Confirm the guest is physically present and approve check-in for " + reservation.getGuest().getFullName() + "?\nThe reservation will become CHECKED_IN and the room OCCUPIED.",
                        "Confirm Guest Arrival", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) return;

                ValidationResult checkInResult = reservationService.checkIn(reservation.getId());
                if (!checkInResult.isValid()) {
                    JOptionPane.showMessageDialog(this, checkInResult.getFormattedErrors(), "Check-In Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                ValidationResult reviewResult = customerFeatureService.reviewPreCheckin(pcId, PreCheckin.Status.APPROVED,
                        "Guest physically arrived; check-in completed by front desk staff", actorId);
                if (!reviewResult.isValid()) {
                    JOptionPane.showMessageDialog(this, "Guest was checked in, but the pre-check-in record could not be updated: " + reviewResult.getFormattedErrors(),
                            "Record Update Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(this, "Pre-check-in approved and guest checked in successfully.", "Check-In Successful", JOptionPane.INFORMATION_MESSAGE);
                refreshPreCheckinQueue();
                refreshArrivalsAndDepartures();
                refreshBillingReservations();
                refreshRoomMatrix();
                return;
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not approve pre-check-in: " + ex.getMessage(), "Approval Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        ValidationResult res = customerFeatureService.reviewPreCheckin(pcId, status, "Reviewed by front desk staff", actorId);
        if (res.isValid()) {
            JOptionPane.showMessageDialog(this, "Pre-check-in marked as " + status.getDisplayName(), "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshPreCheckinQueue();
        }
    }

    // =========================================================================
    // 5. LIVE ROOM STATUS MATRIX PANEL
    // =========================================================================
    private JPanel createRoomMatrixTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));

        JLabel legend = new JLabel("<html><strong>Legend:</strong> " +
        "<span style='color:#10b981;'>■ Available</span> | " +
        "<span style='color:#ef4444;'>■ Occupied</span> | " +
        "<span style='color:#f59e0b;'>■ Dirty / Cleaning</span> | " +
        "<span style='color:#64748b;'>■ Maintenance</span> | " +
        "<span style='color:#6366f1;'>■ Reserved for Upcoming Booking</span></html>");
        legend.setFont(UITheme.FONT_REGULAR);
        topBar.add(legend);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh Matrix");
        refreshBtn.addActionListener(e -> refreshRoomMatrix());
        topBar.add(refreshBtn);

        panel.add(topBar, BorderLayout.NORTH);

        roomMatrixPanel = new JPanel(new GridLayout(0, 5, 12, 12));
        roomMatrixPanel.setBackground(UITheme.BG_LIGHT);
        panel.add(new JScrollPane(roomMatrixPanel), BorderLayout.CENTER);

        refreshRoomMatrix();
        return panel;
    }

    private void refreshRoomMatrix() {
    new SwingWorker<List<Room>, Void>() {
        @Override
        protected List<Room> doInBackground() throws Exception {
            return roomDAO.findAll();
        }

        @Override
        protected void done() {
            try {
                List<Room> rooms = get();
                roomMatrixPanel.removeAll();

                for (Room r : rooms) {
                    RoomStatus physicalStatus = r.getStatus();
                    RoomStatus displayStatus = physicalStatus;

                    if (physicalStatus == RoomStatus.AVAILABLE
                            || physicalStatus == RoomStatus.INSPECTED) {

                        if (reservationDAO.hasUpcomingReservationForRoom(r.getId())) {
                            displayStatus = RoomStatus.RESERVED;
                        }
                    }

                    JPanel card = new JPanel(new BorderLayout(4, 4));
                    card.setPreferredSize(new Dimension(160, 90));
                    card.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(
                                    getStatusColor(displayStatus), 2, true),
                            BorderFactory.createEmptyBorder(8, 10, 8, 10)
                    ));
                    card.setBackground(Color.WHITE);

                    JLabel numLabel = new JLabel("Room " + r.getRoomNumber());
                    numLabel.setFont(UITheme.FONT_BOLD);
                    numLabel.setForeground(UITheme.NAVY_DARK);

                    JLabel typeLabel = new JLabel(r.getRoomType().getName());
                    typeLabel.setFont(UITheme.FONT_SMALL);
                    typeLabel.setForeground(UITheme.TEXT_SECONDARY);

                    JLabel statusBadge = new JLabel(displayStatus.getDisplayName());
                    statusBadge.setFont(UITheme.FONT_SMALL);
                    statusBadge.setForeground(getStatusColor(displayStatus));

                    card.add(numLabel, BorderLayout.NORTH);
                    card.add(typeLabel, BorderLayout.CENTER);
                    card.add(statusBadge, BorderLayout.SOUTH);

                    roomMatrixPanel.add(card);
                }

                roomMatrixPanel.revalidate();
                roomMatrixPanel.repaint();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        ReceptionistDashboardFrame.this,
                        "Failed to refresh room matrix: " + ex.getMessage(),
                        "Room Matrix Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }.execute();
}

    private Color getStatusColor(RoomStatus status) {
        return switch (status) {
            case AVAILABLE, INSPECTED -> UITheme.SUCCESS;
            case RESERVED -> new Color(37, 99, 235);
            case OCCUPIED -> UITheme.DANGER;
            case DIRTY, CLEANING -> UITheme.WARNING;
            case MAINTENANCE, OUT_OF_SERVICE -> UITheme.NAVY_LIGHT;
        };
    }
}
