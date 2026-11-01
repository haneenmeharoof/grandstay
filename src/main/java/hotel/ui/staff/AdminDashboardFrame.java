package hotel.ui.staff;

import hotel.dao.AuditLogDAO;
import hotel.dao.FeedbackDAO;
import hotel.dao.InventoryDAO;
import hotel.dao.MaintenanceDAO;
import hotel.dao.ReportDAO;
import hotel.dao.ReservationDAO;
import hotel.dao.RoomDAO;
import hotel.dao.RoomTypeDAO;
import hotel.dao.UserDAO;
import hotel.model.AuditLog;
import hotel.model.Feedback;
import hotel.model.InventoryItem;
import hotel.model.InventoryTransaction;
import hotel.model.MaintenanceTicket;
import hotel.model.Role;
import hotel.model.Room;
import hotel.model.RoomStatus;
import hotel.model.RoomType;
import hotel.model.User;
import hotel.model.UserStatus;
import hotel.dao.HousekeepingDAO;
import hotel.model.HousekeepingTask;
import hotel.service.InventoryService;
import hotel.service.MaintenanceService;
import hotel.ui.common.HeaderPanel;
import hotel.ui.common.StatCard;
import hotel.ui.common.UITheme;
import hotel.util.CsvExporter;
import hotel.util.CurrencyUtil;
import hotel.util.DateUtil;
import hotel.util.PasswordUtil;
import hotel.util.SessionContext;
import hotel.validation.InputValidator;
import hotel.validation.ValidationResult;
import hotel.dao.WaitlistDAO;
import hotel.model.WaitlistEntry;
import hotel.dao.NotificationDAO;
import hotel.model.Notification;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class AdminDashboardFrame extends JFrame {

    private final ReportDAO reportDAO = new ReportDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final RoomTypeDAO roomTypeDAO = new RoomTypeDAO();
    private final UserDAO userDAO = new UserDAO();
    private final MaintenanceDAO maintenanceDAO = new MaintenanceDAO();
    private final MaintenanceService maintenanceService = new MaintenanceService();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final InventoryService inventoryService = new InventoryService();
    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final HousekeepingDAO housekeepingDAO = new HousekeepingDAO();

    // Stats Cards
    private StatCard totalRoomsCard;
    private StatCard availableRoomsCard;
    private StatCard occupiedRoomsCard;
    private StatCard cleaningRoomsCard;
    private StatCard checkInsCard;
    private StatCard activeBookingsCard;
    private StatCard totalRevenueCard;

    // Tables
    private JTable roomsTable;
    private DefaultTableModel roomsModel;

    private JTable usersTable;
    private DefaultTableModel usersModel;

    private JTable maintenanceTable;
    private DefaultTableModel maintenanceModel;

    private JTable inventoryTable;
    private DefaultTableModel inventoryModel;

    private JTable reviewsTable;
    private DefaultTableModel reviewsModel;

    private JTable auditLogsTable;
    private DefaultTableModel auditLogsModel;

    private JTable reportTable;
    private DefaultTableModel reportModel;

    public AdminDashboardFrame() {
        setTitle("GrandStay Hotel - Executive Administrator Portal");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        addWindowFocusListener(new WindowAdapter() {
            @Override public void windowGainedFocus(WindowEvent e) {
                refreshOverviewStats();
                refreshRoomsList();
            }
        });

        // Header Top Bar
        add(new HeaderPanel("Executive Administration"), BorderLayout.NORTH);

        // Tabbed Panel
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.setBackground(Color.WHITE);

        tabs.addTab("  Executive Dashboard  ", createOverviewPanel());
        tabs.addTab("  Rooms & Categories  ", createRoomsPanel());
        tabs.addTab("  Staff & Accounts  ", createUsersPanel());
        tabs.addTab("  Maintenance & Defects  ", createMaintenancePanel());
        tabs.addTab("  Supplies & Inventory  ", createInventoryPanel());
        tabs.addTab("  Guest Reviews Moderation  ", createReviewsPanel());
        tabs.addTab("  Reports & Analytics  ", createReportsPanel());
        tabs.addTab("  Waitlist Management ", createWaitlistPanel());
        tabs.addTab("  Security Audit Trail  ", createAuditLogsPanel());

        add(tabs, BorderLayout.CENTER);

        refreshOverviewStats();
    }

    // =========================================================================
    // 1. OVERVIEW PANEL
    // =========================================================================
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Top KPI Grid
        JPanel statsGrid = new JPanel(new GridLayout(2, 4, 12, 12));
        statsGrid.setOpaque(false);

        totalRoomsCard = new StatCard("Total Rooms", "0", "Property Capacity", UITheme.NAVY_DARK);
        availableRoomsCard = new StatCard("Available Rooms", "0", "Ready for Guests", UITheme.SUCCESS);
        occupiedRoomsCard = new StatCard("Occupied Rooms", "0", "Currently In-House", UITheme.INFO);
        cleaningRoomsCard = new StatCard("Cleaning / Dirty", "0", "Requires Turnover", UITheme.WARNING);

        checkInsCard = new StatCard("Today's Check-Ins", "0", "Scheduled Arrivals", UITheme.NAVY_MEDIUM);
        activeBookingsCard = new StatCard("Active Bookings", "0", "Confirmed or Stay", UITheme.NAVY_DARK);
        totalRevenueCard = new StatCard("Recorded Revenue", "$0.00", "Settled Payments", UITheme.GOLD_DARK);
        StatCard statusCard = new StatCard("System Health", "ONLINE", "MySQL 8.x Active", UITheme.SUCCESS);

        statsGrid.add(totalRoomsCard);
        statsGrid.add(availableRoomsCard);
        statsGrid.add(occupiedRoomsCard);
        statsGrid.add(cleaningRoomsCard);
        statsGrid.add(checkInsCard);
        statsGrid.add(activeBookingsCard);
        statsGrid.add(totalRevenueCard);
        statsGrid.add(statusCard);

        panel.add(statsGrid, BorderLayout.NORTH);

        // Center Welcome & Quick Actions Card
        JPanel centerCard = UITheme.createCardPanel();
        centerCard.setLayout(new BorderLayout(10, 10));

        JLabel welcomeTitle = new JLabel("Welcome to GrandStay Enterprise Administration");
        welcomeTitle.setFont(UITheme.FONT_TITLE);
        welcomeTitle.setForeground(UITheme.NAVY_DARK);

        JLabel welcomeDesc = new JLabel("<html><p style='width:650px; line-height:1.6;'>" +
                "GrandStay Hotel Management System delivers full role-based governance across reservations, room operations, " +
                "guest billing, housekeeping workflows, maintenance tracking, inventory control, and compliance auditing.<br><br>" +
                "• Use the tabs above to manage staff accounts, adjust room rates, monitor supply levels, and export audit reports.<br>" +
                "• All transactions strictly enforce double-entry accuracy using BigDecimal and prepared statements.</p></html>");
        welcomeDesc.setFont(UITheme.FONT_REGULAR);
        welcomeDesc.setForeground(UITheme.TEXT_SECONDARY);

        JButton refreshBtn = UITheme.createPrimaryButton("Refresh Metrics");
        refreshBtn.addActionListener(e -> refreshOverviewStats());

        centerCard.add(welcomeTitle, BorderLayout.NORTH);
        centerCard.add(welcomeDesc, BorderLayout.CENTER);
        centerCard.add(refreshBtn, BorderLayout.SOUTH);

        panel.add(centerCard, BorderLayout.CENTER);
        return panel;
    }

    private void refreshOverviewStats() {
        new SwingWorker<Map<String, Object>, Void>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                return reportDAO.getOverviewStats();
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> stats = get();
                    totalRoomsCard.setValue(String.valueOf(stats.get("totalRooms")));
                    availableRoomsCard.setValue(String.valueOf(stats.get("availableRooms")));
                    occupiedRoomsCard.setValue(String.valueOf(stats.get("occupiedRooms")));
                    cleaningRoomsCard.setValue(String.valueOf(stats.get("cleaningRooms")));
                    checkInsCard.setValue(String.valueOf(stats.get("todayCheckIns")));
                    activeBookingsCard.setValue(String.valueOf(stats.get("activeBookings")));
                    totalRevenueCard.setValue(CurrencyUtil.format((BigDecimal) stats.get("totalRevenue")));
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // =========================================================================
    // 2. ROOMS & CATEGORIES PANEL
    // =========================================================================
    private JPanel createRoomsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] cols = {"Room ID", "Room #", "Category", "Floor", "Nightly Base Rate", "Max Capacity", "Bed Type", "Status"};
        roomsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        roomsTable = new JTable(roomsModel);
        UITheme.styleTable(roomsTable);
        panel.add(new JScrollPane(roomsTable), BorderLayout.CENTER);

        JPanel actions = UITheme.createCardPanel();
        actions.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 8));

        JButton addRoomBtn = UITheme.createGoldButton("+ Add New Room");
        addRoomBtn.addActionListener(e -> openAddRoomDialog());

        JButton addCategoryBtn = UITheme.createSecondaryButton("+ Add Room Category");
        addCategoryBtn.addActionListener(e -> openAddCategoryDialog());

        JButton setStatusBtn = UITheme.createSecondaryButton("Update Room Status");
        setStatusBtn.addActionListener(e -> handleUpdateRoomStatus());

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshRoomsList());

        actions.add(refreshBtn);
        actions.add(setStatusBtn);
        actions.add(addCategoryBtn);
        actions.add(addRoomBtn);
        panel.add(actions, BorderLayout.SOUTH);

        refreshRoomsList();
        return panel;
    }

    private void refreshRoomsList() {
    new SwingWorker<List<Room>, Void>() {
        @Override
        protected List<Room> doInBackground() throws Exception {
            return roomDAO.findAll();
        }

        @Override
        protected void done() {
            try {
                List<Room> rooms = get();
                roomsModel.setRowCount(0);

                for (Room r : rooms) {
                    RoomStatus displayStatus = r.getStatus();

                    // Show reservation commitment without changing
                    // the room's actual physical status.
                    if (displayStatus == RoomStatus.AVAILABLE
                            && reservationDAO.hasUpcomingReservationForRoom(r.getId())) {
                        displayStatus = RoomStatus.RESERVED;
                    }

                    roomsModel.addRow(new Object[]{
                            r.getId(),
                            r.getRoomNumber(),
                            r.getRoomType().getName(),
                            "Floor " + r.getFloorNumber(),
                            CurrencyUtil.format(r.getRoomType().getBasePrice()),
                            r.getRoomType().getMaxCapacity() + " Guests",
                            r.getRoomType().getBedType(),
                            displayStatus.getDisplayName()
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        AdminDashboardFrame.this,
                        "Failed to refresh room list: " + ex.getMessage(),
                        "Room Loading Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }.execute();
}

    private void openAddRoomDialog() {
        try {
            List<RoomType> types = roomTypeDAO.findAll();
            if (types.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please create at least one room category first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JDialog d = new JDialog(this, "Add New Room", true);
            d.setSize(420, 320);
            d.setLocationRelativeTo(this);
            d.setLayout(new BorderLayout());

            JPanel p = new JPanel(new GridBagLayout());
            p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(6, 6, 6, 6);

            JTextField numField = new JTextField(12);
            JComboBox<RoomType> typeCombo = new JComboBox<>();
            for (RoomType rt : types) typeCombo.addItem(rt);
            JSpinner floorSpin = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));

            g.gridx = 0; g.gridy = 0; p.add(new JLabel("Room Number *:"), g);
            g.gridx = 1; p.add(numField, g);

            g.gridx = 0; g.gridy = 1; p.add(new JLabel("Room Category *:"), g);
            g.gridx = 1; p.add(typeCombo, g);

            g.gridx = 0; g.gridy = 2; p.add(new JLabel("Floor Number:"), g);
            g.gridx = 1; p.add(floorSpin, g);

            d.add(p, BorderLayout.CENTER);

            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton save = UITheme.createGoldButton("Save Room");
            save.addActionListener(e -> {
                try {
                    String num = numField.getText().trim();
                    if (num.isEmpty()) return;
                    RoomType selType = (RoomType) typeCombo.getSelectedItem();
                    Room room = new Room(num, selType.getId(), (Integer) floorSpin.getValue(), RoomStatus.AVAILABLE, "Standard unit");
                    roomDAO.insert(room);
                    JOptionPane.showMessageDialog(d, "Room " + num + " added successfully!", "Room Added", JOptionPane.INFORMATION_MESSAGE);
                    d.dispose();
                    refreshRoomsList();
                    refreshOverviewStats();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            foot.add(save);
            d.add(foot, BorderLayout.SOUTH);
            d.setVisible(true);
        } catch (Exception ignored) {}
    }

    private void openAddCategoryDialog() {
        JDialog d = new JDialog(this, "Add Room Category", true);
        d.setSize(460, 420);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 6, 6, 6);

        JTextField codeField = new JTextField("DLX-KNG", 15);
        JTextField nameField = new JTextField("Deluxe King Room", 15);
        JTextField rateField = new JTextField("160.00", 15);
        JSpinner capSpin = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        JTextField bedField = new JTextField("King Bed", 15);
        JTextField amenitiesField = new JTextField("Free Wi-Fi, 4K TV, Mini Fridge, AC, Safe", 15);

        int r = 0;
        addWalkInField(p, g, r++, "Code *:", codeField);
        addWalkInField(p, g, r++, "Name *:", nameField);
        addWalkInField(p, g, r++, "Nightly Rate ($) *:", rateField);
        addWalkInField(p, g, r++, "Max Capacity:", capSpin);
        addWalkInField(p, g, r++, "Bed Configuration:", bedField);
        addWalkInField(p, g, r++, "Amenities List:", amenitiesField);

        d.add(p, BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = UITheme.createGoldButton("Create Category");
        save.addActionListener(e -> {
            try {
                BigDecimal price = new BigDecimal(rateField.getText().trim());
                RoomType rt = new RoomType(codeField.getText().trim(), nameField.getText().trim(),
                        nameField.getText().trim(), price, (Integer) capSpin.getValue(),
                        bedField.getText().trim(), amenitiesField.getText().trim());
                roomTypeDAO.insert(rt);
                JOptionPane.showMessageDialog(d, "Room category created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                d.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        foot.add(save);
        d.add(foot, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void handleUpdateRoomStatus() {
    int selected = roomsTable.getSelectedRow();

    if (selected < 0) {
        JOptionPane.showMessageDialog(
                this,
                "Select a room from the table.",
                "Notice",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    selected = roomsTable.convertRowIndexToModel(selected);

    Long roomId = (Long) roomsTable.getModel().getValueAt(selected, 0);
    String roomNum = (String) roomsTable.getModel().getValueAt(selected, 1);

    RoomStatus newStatus = (RoomStatus) JOptionPane.showInputDialog(
            this,
            "Choose new status for Room " + roomNum + ":",
            "Update Room Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            RoomStatus.values(),
            RoomStatus.AVAILABLE
    );

    if (newStatus == null) {
        return;
    }

    try {
        // Prevent releasing a room with a checked-in guest.
        if ((newStatus == RoomStatus.AVAILABLE
                || newStatus == RoomStatus.INSPECTED)
                && reservationDAO.hasCheckedInReservationForRoom(roomId)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Room " + roomNum
                            + " has a guest currently checked in. "
                            + "Complete guest check-out first.",
                    "Room Occupied",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Prevent releasing a room protected by an active reservation.
        if ((newStatus == RoomStatus.AVAILABLE
                || newStatus == RoomStatus.INSPECTED)
                && reservationDAO.hasUpcomingReservationForRoom(roomId)) {

            JOptionPane.showMessageDialog(
                    this,
                    "Room " + roomNum
                            + " has an active reservation. "
                            + "It cannot be released while that reservation is active.",
                    "Room Reserved",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        roomDAO.updateStatus(roomId, newStatus);

// Automatically create a checkout-cleaning task when a room becomes DIRTY.
if (newStatus == RoomStatus.DIRTY) {
    HousekeepingTask task = new HousekeepingTask();
    task.setRoomId(roomId);
    task.setAssignedToUserId(3l);
    task.setTaskType("CHECKOUT_CLEANING");
    task.setPriority(HousekeepingTask.Priority.HIGH);
    task.setStatus(HousekeepingTask.Status.PENDING);
    task.setNotes("Room marked DIRTY by administrator. Cleaning required.");

    housekeepingDAO.insert(task);
}

        JOptionPane.showMessageDialog(
                this,
                "Room " + roomNum + " updated to "
                        + newStatus.getDisplayName(),
                "Updated",
                JOptionPane.INFORMATION_MESSAGE
        );

        refreshRoomsList();
        refreshOverviewStats();

    } catch (Exception ex) {
        ex.printStackTrace();

        JOptionPane.showMessageDialog(
                this,
                "Error updating room status: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
    // =========================================================================
    // 3. STAFF & ACCOUNTS PANEL
    // =========================================================================
    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] cols = {"User ID", "Username", "Email", "Full Name", "Role", "Status", "Registered At"};
        usersModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        usersTable = new JTable(usersModel);
        UITheme.styleTable(usersTable);
        panel.add(new JScrollPane(usersTable), BorderLayout.CENTER);

        JPanel actions = UITheme.createCardPanel();
        actions.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 8));

        JButton addStaffBtn = UITheme.createGoldButton("+ Add Staff Member");
        addStaffBtn.addActionListener(e -> openAddStaffDialog());

        JButton toggleStatusBtn = UITheme.createSecondaryButton("Enable / Disable Account");
        toggleStatusBtn.addActionListener(e -> handleToggleUserStatus());

        JButton resetPassBtn = UITheme.createSecondaryButton("Reset Password");
        resetPassBtn.addActionListener(e -> handleResetPassword());

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshUsersList());

        actions.add(refreshBtn);
        actions.add(resetPassBtn);
        actions.add(toggleStatusBtn);
        actions.add(addStaffBtn);
        panel.add(actions, BorderLayout.SOUTH);

        refreshUsersList();
        return panel;
    }

    private void refreshUsersList() {
        new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return userDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<User> list = get();
                    usersModel.setRowCount(0);
                    for (User u : list) {
                        usersModel.addRow(new Object[]{
                                u.getId(),
                                u.getUsername(),
                                u.getEmail(),
                                u.getFullName(),
                                u.getRole().getDisplayName(),
                                u.getStatus().name(),
                                DateUtil.formatDateTime(u.getCreatedAt())
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void openAddStaffDialog() {
        JDialog d = new JDialog(this, "Create Staff Account", true);
        d.setSize(440, 380);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 6, 6, 6);

        JTextField unField = new JTextField(15);
        JTextField emField = new JTextField(15);
        JTextField fnField = new JTextField(15);
        JPasswordField pwField = new JPasswordField(15);
        JComboBox<Role> roleCombo = new JComboBox<>(new Role[]{Role.RECEPTIONIST, Role.HOUSEKEEPING, Role.ADMIN});

        int r = 0;
        addWalkInField(p, g, r++, "Username *:", unField);
        addWalkInField(p, g, r++, "Email *:", emField);
        addWalkInField(p, g, r++, "Full Name *:", fnField);
        addWalkInField(p, g, r++, "Initial Password *:", pwField);
        addWalkInField(p, g, r++, "Assign Role *:", roleCombo);

        d.add(p, BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = UITheme.createGoldButton("Create Staff");
        save.addActionListener(e -> {
            try {
                String pw = new String(pwField.getPassword());
                ValidationResult val = InputValidator.validateUserRegistration(unField.getText(), emField.getText(), pw, fnField.getText());
                if (!val.isValid()) {
                    JOptionPane.showMessageDialog(d, val.getFormattedErrors(), "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                User user = new User(unField.getText().trim(), emField.getText().trim(),
                        PasswordUtil.hashPassword(pw), fnField.getText().trim(), "+1-555-0100", (Role) roleCombo.getSelectedItem());
                userDAO.insert(user);
                JOptionPane.showMessageDialog(d, "Staff member created with role " + roleCombo.getSelectedItem(), "Success", JOptionPane.INFORMATION_MESSAGE);
                d.dispose();
                refreshUsersList();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        foot.add(save);
        d.add(foot, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void handleToggleUserStatus() {
        int selected = usersTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select an account to modify.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long userId = (Long) usersTable.getValueAt(selected, 0);
        String username = (String) usersTable.getValueAt(selected, 1);
        String currentStatus = (String) usersTable.getValueAt(selected, 5);

        UserStatus newStatus = "ACTIVE".equals(currentStatus) ? UserStatus.SUSPENDED : UserStatus.ACTIVE;
        try {
            userDAO.updateStatus(userId, newStatus);
            JOptionPane.showMessageDialog(this, "Account '" + username + "' is now " + newStatus.name(), "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshUsersList();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleResetPassword() {
        int selected = usersTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select an account to reset password.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long userId = (Long) usersTable.getValueAt(selected, 0);
        String username = (String) usersTable.getValueAt(selected, 1);

        String newPass = JOptionPane.showInputDialog(this, "Enter new password for '" + username + "' (min 6 chars):", "Reset Password", JOptionPane.QUESTION_MESSAGE);
        if (newPass != null && newPass.trim().length() >= 6) {
            try {
                userDAO.updatePassword(userId, PasswordUtil.hashPassword(newPass.trim()));
                JOptionPane.showMessageDialog(this, "Password for '" + username + "' has been securely reset.", "Password Reset", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // 4. MAINTENANCE & DEFECTS PANEL
    // =========================================================================
    private JPanel createMaintenancePanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] cols = {"ID", "Ticket #", "Room #", "Issue Description", "Priority", "Status", "Room Blocked", "Assigned Staff", "Reported At"};
        maintenanceModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        maintenanceTable = new JTable(maintenanceModel);
        UITheme.styleTable(maintenanceTable);
        panel.add(new JScrollPane(maintenanceTable), BorderLayout.CENTER);

        JPanel actions = UITheme.createCardPanel();
        actions.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 8));

        JButton addTicketBtn = UITheme.createGoldButton("+ Log Maintenance Ticket");
        addTicketBtn.addActionListener(e -> openAddTicketDialog());

        JButton resolveBtn = UITheme.createPrimaryButton("Mark Ticket Resolved");
        resolveBtn.addActionListener(e -> handleResolveTicket());

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshMaintenanceList());

        actions.add(refreshBtn);
        actions.add(resolveBtn);
        actions.add(addTicketBtn);
        panel.add(actions, BorderLayout.SOUTH);

        refreshMaintenanceList();
        return panel;
    }

    private void refreshMaintenanceList() {
        new SwingWorker<List<MaintenanceTicket>, Void>() {
            @Override
            protected List<MaintenanceTicket> doInBackground() throws Exception {
                return maintenanceDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<MaintenanceTicket> list = get();
                    maintenanceModel.setRowCount(0);
                    for (MaintenanceTicket t : list) {
                        maintenanceModel.addRow(new Object[]{
                                t.getId(),
                                t.getTicketNumber(),
                                "Room " + t.getRoomNumber(),
                                t.getIssueDescription(),
                                t.getPriority().name(),
                                t.getStatus().getDisplayName(),
                                t.isRoomMadeUnavailable() ? "YES" : "NO",
                                t.getAssignedStaffName() != null ? t.getAssignedStaffName() : "Unassigned",
                                DateUtil.formatDateTime(t.getCreatedAt())
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void openAddTicketDialog() {
        try {
            List<Room> rooms = roomDAO.findAll();
            JDialog d = new JDialog(this, "Log Maintenance Ticket", true);
            d.setSize(440, 360);
            d.setLocationRelativeTo(this);
            d.setLayout(new BorderLayout());

            JPanel p = new JPanel(new GridBagLayout());
            p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(6, 6, 6, 6);

            JComboBox<Room> roomCombo = new JComboBox<>();
            for (Room r : rooms) roomCombo.addItem(r);
            JTextArea descArea = new JTextArea(3, 16);
            JComboBox<MaintenanceTicket.Priority> prioCombo = new JComboBox<>(MaintenanceTicket.Priority.values());
            JComboBox<String> blockCombo = new JComboBox<>(new String[]{"Yes - Lock room out of service", "No - Minor issue"});

            int r = 0;
            addWalkInField(p, g, r++, "Select Room *:", roomCombo);
            addWalkInField(p, g, r++, "Priority *:", prioCombo);
            addWalkInField(p, g, r++, "Make Room Unavailable?:", blockCombo);

            g.gridx = 0; g.gridy = r++; p.add(new JLabel("Defect Description *:"), g);
            g.gridx = 1; p.add(new JScrollPane(descArea), g);

            d.add(p, BorderLayout.CENTER);

            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton save = UITheme.createGoldButton("Submit Ticket");
            save.addActionListener(e -> {
                Room sel = (Room) roomCombo.getSelectedItem();
                boolean block = blockCombo.getSelectedIndex() == 0;
                ValidationResult res = maintenanceService.createTicket(
                        sel.getId(), descArea.getText(), (MaintenanceTicket.Priority) prioCombo.getSelectedItem(), block, null
                );
                if (res.isValid()) {
                    JOptionPane.showMessageDialog(d, "Maintenance ticket created!", "Ticket Created", JOptionPane.INFORMATION_MESSAGE);
                    d.dispose();
                    refreshMaintenanceList();
                    refreshOverviewStats();
                } else {
                    JOptionPane.showMessageDialog(d, res.getFormattedErrors(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            foot.add(save);
            d.add(foot, BorderLayout.SOUTH);
            d.setVisible(true);
        } catch (Exception ignored) {}
    }

    private void handleResolveTicket() {
        int selected = maintenanceTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select a maintenance ticket.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long ticketId = (Long) maintenanceTable.getValueAt(selected, 0);
        String notes = JOptionPane.showInputDialog(this, "Enter resolution notes / repairs completed:", "Resolve Ticket", JOptionPane.QUESTION_MESSAGE);
        if (notes != null) {
            ValidationResult res = maintenanceService.updateTicketStatus(ticketId, MaintenanceTicket.Status.RESOLVED, notes);
            if (res.isValid()) {
                JOptionPane.showMessageDialog(this, "Ticket marked as RESOLVED. Room availability restored.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshMaintenanceList();
                refreshOverviewStats();
            }
        }
    }

    // =========================================================================
    // 5. SUPPLIES & INVENTORY PANEL
    // =========================================================================
    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] cols = {"ID", "Item Code", "Item Name", "Category", "Quantity On Hand", "Min Stock Level", "Unit", "Unit Cost", "Status"};
        inventoryModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        inventoryTable = new JTable(inventoryModel);
        UITheme.styleTable(inventoryTable);
        panel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JPanel actions = UITheme.createCardPanel();
        actions.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 8));

        JButton addStockBtn = UITheme.createGoldButton("+ Stock In / Purchase");
        addStockBtn.addActionListener(e -> handleStockIn());

        JButton deductStockBtn = UITheme.createSecondaryButton("Stock Out / Deduct");
        deductStockBtn.addActionListener(e -> handleStockOut());

        JButton addItemBtn = UITheme.createSecondaryButton("+ Add New Catalog Item");
        addItemBtn.addActionListener(e -> openAddItemDialog());

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshInventoryList());

        actions.add(refreshBtn);
        actions.add(deductStockBtn);
        actions.add(addStockBtn);
        actions.add(addItemBtn);
        panel.add(actions, BorderLayout.SOUTH);

        refreshInventoryList();
        return panel;
    }

    private void refreshInventoryList() {
        new SwingWorker<List<InventoryItem>, Void>() {
            @Override
            protected List<InventoryItem> doInBackground() throws Exception {
                return inventoryService.getAllItems();
            }

            @Override
            protected void done() {
                try {
                    List<InventoryItem> list = get();
                    inventoryModel.setRowCount(0);
                    for (InventoryItem i : list) {
                        inventoryModel.addRow(new Object[]{
                                i.getId(),
                                i.getItemCode(),
                                i.getItemName(),
                                i.getCategory(),
                                i.getQuantityOnHand(),
                                i.getMinStockLevel(),
                                i.getUnitOfMeasure(),
                                CurrencyUtil.format(i.getUnitCost()),
                                i.isLowStock() ? "⚠️ LOW STOCK ALERT" : "Adequate"
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void handleStockIn() {
        int selected = inventoryTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select an item to restock.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long itemId = (Long) inventoryTable.getValueAt(selected, 0);
        String name = (String) inventoryTable.getValueAt(selected, 2);

        String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity to add to stock for '" + name + "':", "Stock In", JOptionPane.QUESTION_MESSAGE);
        if (qtyStr != null) {
            try {
                int qty = Integer.parseInt(qtyStr.trim());
                ValidationResult res = inventoryService.adjustStock(itemId, InventoryTransaction.Type.STOCK_IN, qty, "Supplier purchase restock");
                if (res.isValid()) {
                    JOptionPane.showMessageDialog(this, "Stock added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshInventoryList();
                } else {
                    JOptionPane.showMessageDialog(this, res.getFormattedErrors(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive number.", "Invalid Number", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleStockOut() {
        int selected = inventoryTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select an item to deduct.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long itemId = (Long) inventoryTable.getValueAt(selected, 0);
        String name = (String) inventoryTable.getValueAt(selected, 2);

        String qtyStr = JOptionPane.showInputDialog(this, "Enter quantity to deduct for '" + name + "':", "Stock Out", JOptionPane.QUESTION_MESSAGE);
        if (qtyStr != null) {
            try {
                int qty = Integer.parseInt(qtyStr.trim());
                ValidationResult res = inventoryService.adjustStock(itemId, InventoryTransaction.Type.STOCK_OUT, qty, "Administrative adjustment");
                if (res.isValid()) {
                    JOptionPane.showMessageDialog(this, "Stock deducted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshInventoryList();
                } else {
                    JOptionPane.showMessageDialog(this, res.getFormattedErrors(), "Stock Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive number.", "Invalid Number", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openAddItemDialog() {
        JDialog d = new JDialog(this, "Add Catalog Supply Item", true);
        d.setSize(440, 380);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 6, 6, 6);

        JTextField codeField = new JTextField("SUP-001", 15);
        JTextField nameField = new JTextField("Luxury Bathrobe", 15);
        JTextField catField = new JTextField("Linens", 15);
        JSpinner qtySpin = new JSpinner(new SpinnerNumberModel(20, 0, 1000, 1));
        JSpinner minSpin = new JSpinner(new SpinnerNumberModel(5, 1, 500, 1));
        JTextField costField = new JTextField("25.00", 15);

        int r = 0;
        addWalkInField(p, g, r++, "Item Code *:", codeField);
        addWalkInField(p, g, r++, "Item Name *:", nameField);
        addWalkInField(p, g, r++, "Category *:", catField);
        addWalkInField(p, g, r++, "Initial Stock:", qtySpin);
        addWalkInField(p, g, r++, "Minimum Threshold:", minSpin);
        addWalkInField(p, g, r++, "Unit Cost ($):", costField);

        d.add(p, BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = UITheme.createGoldButton("Save Supply Item");
        save.addActionListener(e -> {
            try {
                InventoryItem item = new InventoryItem();
                item.setItemCode(codeField.getText().trim());
                item.setItemName(nameField.getText().trim());
                item.setCategory(catField.getText().trim());
                item.setQuantityOnHand((Integer) qtySpin.getValue());
                item.setMinStockLevel((Integer) minSpin.getValue());
                item.setUnitCost(new BigDecimal(costField.getText().trim()));
                item.setUnitOfMeasure("PCS");
                item.setLocation("Central Hotel Warehouse");
                inventoryDAO.insertItem(item);

                JOptionPane.showMessageDialog(d, "Item created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                d.dispose();
                refreshInventoryList();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        foot.add(save);
        d.add(foot, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    // =========================================================================
    // 6. GUEST REVIEWS MODERATION PANEL
    // =========================================================================
    private JPanel createReviewsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] cols = {"ID", "Booking Ref #", "Guest Name", "Rating", "Review Title", "Review Text", "Status", "Submitted At"};
        reviewsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        reviewsTable = new JTable(reviewsModel);
        UITheme.styleTable(reviewsTable);
        panel.add(new JScrollPane(reviewsTable), BorderLayout.CENTER);

        JPanel actions = UITheme.createCardPanel();
        actions.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 8));

        JButton approveBtn = UITheme.createGoldButton("Approve & Publish Review");
        approveBtn.addActionListener(e -> handleModerateReview(Feedback.ModerationStatus.APPROVED));

        JButton rejectBtn = UITheme.createDangerButton("Reject / Hide Review");
        rejectBtn.addActionListener(e -> handleModerateReview(Feedback.ModerationStatus.REJECTED));

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshReviewsList());

        actions.add(refreshBtn);
        actions.add(rejectBtn);
        actions.add(approveBtn);
        panel.add(actions, BorderLayout.SOUTH);

        refreshReviewsList();
        return panel;
    }

    private void refreshReviewsList() {
        new SwingWorker<List<Feedback>, Void>() {
            @Override
            protected List<Feedback> doInBackground() throws Exception {
                return feedbackDAO.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Feedback> list = get();
                    reviewsModel.setRowCount(0);
                    for (Feedback f : list) {
                        reviewsModel.addRow(new Object[]{
                                f.getId(),
                                f.getReservationRef(),
                                f.getGuestName(),
                                "★ ".repeat(f.getRating()),
                                f.getReviewTitle(),
                                f.getReviewText(),
                                f.getModerationStatus().getDisplayName(),
                                DateUtil.formatDateTime(f.getCreatedAt())
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void handleModerateReview(Feedback.ModerationStatus status) {
        int selected = reviewsTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Select a review to moderate.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long fId = (Long) reviewsTable.getValueAt(selected, 0);
        User actor = SessionContext.getCurrentUser();
        Long actorId = actor != null ? actor.getId() : 1L;

        try {
            feedbackDAO.updateModeration(fId, status, actorId);
            JOptionPane.showMessageDialog(this, "Review marked as " + status.getDisplayName(), "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshReviewsList();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================================
    // 7. REPORTS & ANALYTICS PANEL
    // =========================================================================
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel topBar = UITheme.createCardPanel();
        topBar.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 8));

        JComboBox<String> reportTypeCombo = new JComboBox<>(new String[]{
                "Occupancy Rate by Category",
                "Revenue Report (Last 30 Days)",
                "Outstanding Balances Report",
                "Occupancy Forecast (Next 14 Days)",
                "Monthly Revenue Trends (Last 12 Months)",
                "Room Category Performance (Last 12 Months)",
                "Booking Source Analysis (Last 12 Months)",
                "Cancellation Statistics (Last 12 Months)",
                "Housekeeping Performance (Last 30 Days)"
        });

        JButton loadBtn = UITheme.createPrimaryButton("Generate Report");
        loadBtn.addActionListener(e -> generateSelectedReport((String) reportTypeCombo.getSelectedItem()));

        JButton exportBtn = UITheme.createGoldButton("Export Report to CSV");
        exportBtn.addActionListener(e -> exportReportCsv());

        topBar.add(new JLabel("Select Business Report:"));
        topBar.add(reportTypeCombo);
        topBar.add(loadBtn);
        topBar.add(exportBtn);
        panel.add(topBar, BorderLayout.NORTH);

        reportModel = new DefaultTableModel();
        reportTable = new JTable(reportModel);
        UITheme.styleTable(reportTable);
        panel.add(new JScrollPane(reportTable), BorderLayout.CENTER);

        generateSelectedReport("Occupancy Rate by Category");
        return panel;
    }

    private void generateSelectedReport(String reportType) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                reportModel.setRowCount(0);
                reportModel.setColumnCount(0);

                if ("Occupancy Rate by Category".equals(reportType)) {
                    reportModel.setColumnIdentifiers(new String[]{"Room Category", "Total Rooms", "Occupied", "Available", "Cleaning", "Maintenance", "Occupancy %"});
                    List<List<String>> data = reportDAO.getOccupancySummary();
                    for (List<String> row : data) {
                        reportModel.addRow(row.toArray());
                    }
                } else if ("Revenue Report (Last 30 Days)".equals(reportType)) {
                    reportModel.setColumnIdentifiers(new String[]{"Payment Date", "Payment Method", "Transaction Count", "Total Revenue ($)"});
                    List<List<String>> data = reportDAO.getRevenueReport(LocalDate.now().minusDays(30), LocalDate.now());
                    for (List<String> row : data) {
                        reportModel.addRow(row.toArray());
                    }
                } else if ("Outstanding Balances Report".equals(reportType)) {
                    reportModel.setColumnIdentifiers(new String[]{"Booking Ref #", "Guest Name", "Check-in", "Check-out", "Total Folio ($)", "Paid ($)", "Balance Due ($)", "Status"});
                    List<List<String>> data = reportDAO.getOutstandingBalancesReport();
                    for (List<String> row : data) reportModel.addRow(row.toArray());
                } else if ("Occupancy Forecast (Next 14 Days)".equals(reportType)) {
                    reportModel.setColumnIdentifiers(new String[]{"Stay Date", "Booked Rooms", "Booked Room-Nights"});
                    for (List<String> row : reportDAO.getOccupancyForecast(LocalDate.now(), LocalDate.now().plusDays(13))) reportModel.addRow(row.toArray());
                } else if ("Monthly Revenue Trends (Last 12 Months)".equals(reportType)) {
                    reportModel.setColumnIdentifiers(new String[]{"Month", "Completed Transactions", "Revenue ($)"});
                    for (List<String> row : reportDAO.getMonthlyRevenueTrends(LocalDate.now().minusMonths(11).withDayOfMonth(1), LocalDate.now())) reportModel.addRow(row.toArray());
                } else if ("Room Category Performance (Last 12 Months)".equals(reportType)) {
                    reportModel.setColumnIdentifiers(new String[]{"Room Category", "Bookings", "Room-Nights", "Room Revenue ($)"});
                    for (List<String> row : reportDAO.getRoomCategoryPerformance(LocalDate.now().minusMonths(12), LocalDate.now())) reportModel.addRow(row.toArray());
                } else if ("Booking Source Analysis (Last 12 Months)".equals(reportType)) {
                    reportModel.setColumnIdentifiers(new String[]{"Booking Source", "Bookings", "Booking Value ($)"});
                    for (List<String> row : reportDAO.getBookingSourceAnalysis(LocalDate.now().minusMonths(12), LocalDate.now())) reportModel.addRow(row.toArray());
                } else if ("Cancellation Statistics (Last 12 Months)".equals(reportType)) {
                    reportModel.setColumnIdentifiers(new String[]{"Cancellation Month", "Cancelled Reservations"});
                    for (List<String> row : reportDAO.getCancellationStatistics(LocalDate.now().minusMonths(12), LocalDate.now())) reportModel.addRow(row.toArray());
                } else if ("Housekeeping Performance (Last 30 Days)".equals(reportType)) {
                    reportModel.setColumnIdentifiers(new String[]{"Staff Member", "Completed Tasks", "Inspected Tasks"});
                    for (List<String> row : reportDAO.getHousekeepingPerformance(LocalDate.now().minusDays(30), LocalDate.now())) reportModel.addRow(row.toArray());
                }
                return null;
            }

            @Override
            protected void done() {
                UITheme.styleTable(reportTable);
            }
        }.execute();
    }

    private void exportReportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("hotel_analytics_report.csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                CsvExporter.exportTableToCsv(reportTable, chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "Report exported to " + chooser.getSelectedFile().getName(), "CSV Exported", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // 8. SECURITY AUDIT TRAIL PANEL
    // =========================================================================
    private JPanel createAuditLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] cols = {"Log ID", "Actor Username", "Role", "Action Performed", "Entity", "Entity ID", "Details", "Timestamp"};
        auditLogsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        auditLogsTable = new JTable(auditLogsModel);
        UITheme.styleTable(auditLogsTable);
        panel.add(new JScrollPane(auditLogsTable), BorderLayout.CENTER);

        JPanel actions = UITheme.createCardPanel();
        actions.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 8));

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh Logs");
        refreshBtn.addActionListener(e -> refreshAuditLogs());

        JButton exportLogsBtn = UITheme.createGoldButton("Export Audit Logs to CSV");
        exportLogsBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("audit_logs_export.csv"));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    CsvExporter.exportTableToCsv(auditLogsTable, chooser.getSelectedFile());
                    JOptionPane.showMessageDialog(this, "Audit logs exported successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        actions.add(refreshBtn);
        actions.add(exportLogsBtn);
        panel.add(actions, BorderLayout.SOUTH);

        refreshAuditLogs();
        return panel;
    }

    private void refreshAuditLogs() {
        new SwingWorker<List<AuditLog>, Void>() {
            @Override
            protected List<AuditLog> doInBackground() throws Exception {
                return auditLogDAO.findRecent(150);
            }

            @Override
            protected void done() {
                try {
                    List<AuditLog> list = get();
                    auditLogsModel.setRowCount(0);
                    for (AuditLog l : list) {
                        auditLogsModel.addRow(new Object[]{
                                l.getId(),
                                l.getUsername(),
                                l.getRole(),
                                l.getAction(),
                                l.getEntityType(),
                                l.getEntityId(),
                                l.getDetails(),
                                DateUtil.formatDateTime(l.getTimestamp())
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void addWalkInField(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BOLD);
        panel.add(l, gbc);

        gbc.gridx = 1;
        panel.add(comp, gbc);
    }

    // =========================================================================
    // 9.WAITLIST MANAGEMENT PANEL
    // =========================================================================
    private JPanel createWaitlistPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] columns = {
            "Request ID", "Customer", "Room Category",
            "Check-in", "Check-out", "Guests", "Status"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        UITheme.styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = UITheme.createCardPanel();
        actions.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 8));

        JButton approveBtn = UITheme.createGoldButton("Approve Request");
        JButton rejectBtn = UITheme.createSecondaryButton("Reject Request");
        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");

        approveBtn.addActionListener(e ->
            updateSelectedWaitlistRequest(table, model, WaitlistEntry.Status.APPROVED)
        );

        rejectBtn.addActionListener(e ->
            updateSelectedWaitlistRequest(table, model, WaitlistEntry.Status.REJECTED)
        );

        refreshBtn.addActionListener(e -> refreshWaitlistTable(model));

        actions.add(approveBtn);
        actions.add(rejectBtn);
        actions.add(refreshBtn);

        panel.add(actions, BorderLayout.SOUTH);

        refreshWaitlistTable(model);
        return panel;
    }

    private void refreshWaitlistTable(DefaultTableModel model) {
        new SwingWorker<List<WaitlistEntry>, Void>() {
            @Override
            protected List<WaitlistEntry> doInBackground() throws Exception {
                return new WaitlistDAO().findAll();
            }

            @Override
            protected void done() {
                try {
                    List<WaitlistEntry> entries = get();
                    model.setRowCount(0);

                    for (WaitlistEntry entry : entries) {
                        model.addRow(new Object[]{
                            entry.getId(),
                            entry.getGuestName(),
                            entry.getRoomTypeName(),
                            entry.getDesiredCheckIn(),
                            entry.getDesiredCheckOut(),
                            entry.getNumberOfGuests(),
                            entry.getStatus().name()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        AdminDashboardFrame.this,
                        "Could not load waitlist requests: " + ex.getMessage(),
                        "Waitlist Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }

    private void updateSelectedWaitlistRequest(
            JTable table,
            DefaultTableModel model,
            WaitlistEntry.Status newStatus) {

        int selectedRow = table.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a waitlist request first."
            );
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        Long requestId = Long.valueOf(
            model.getValueAt(modelRow, 0).toString()
        );

        String currentStatus = model.getValueAt(modelRow, 6).toString();

        if (!currentStatus.equals("ACTIVE")) {
            JOptionPane.showMessageDialog(
                this,
                "Only active requests can be approved or rejected."
            );
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to " + newStatus.name().toLowerCase()
                + " this waitlist request?",
            "Confirm Waitlist Update",
            JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
    WaitlistDAO waitlistDAO = new WaitlistDAO();

    // Retrieve the request to identify its customer
    WaitlistEntry entry = waitlistDAO.findAll()
        .stream()
        .filter(item -> item.getId().equals(requestId))
        .findFirst()
        .orElse(null);

    if (entry == null) {
        throw new Exception("Waitlist request not found.");
    }

    // Update request status
    boolean updated = waitlistDAO.updateStatus(requestId, newStatus);

    if (updated && entry.getUserId() != null) {
        String title;
        String message;

        if (newStatus == WaitlistEntry.Status.APPROVED) {
            title = "Waitlist Request Approved";
            message = "Your waitlist request for "
                + entry.getRoomTypeName()
                + " has been approved by the hotel.";
        } else {
            title = "Waitlist Request Rejected";
            message = "Your waitlist request for "
                + entry.getRoomTypeName()
                + " has been rejected by the hotel.";
        }

        Notification notification = new Notification(
            entry.getUserId(),
            title,
            message,
            "WAITLIST",
            String.valueOf(requestId)
        );

        new NotificationDAO().insert(notification);
    }

    return updated;
}

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                            AdminDashboardFrame.this,
                            "Waitlist request updated successfully."
                        );
                        refreshWaitlistTable(model);
                    } else {
                        JOptionPane.showMessageDialog(
                            AdminDashboardFrame.this,
                            "No request was updated."
                        );
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                        AdminDashboardFrame.this,
                        "Update failed: " + ex.getMessage(),
                        "Waitlist Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }.execute();
    }
}
