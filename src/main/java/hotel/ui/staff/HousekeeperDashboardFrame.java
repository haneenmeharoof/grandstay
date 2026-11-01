package hotel.ui.staff;

import hotel.dao.HousekeepingDAO;
import hotel.dao.InventoryDAO;
import hotel.model.HousekeepingTask;
import hotel.model.InventoryItem;
import hotel.model.InventoryTransaction;
import hotel.model.User;
import hotel.service.HousekeepingService;
import hotel.service.InventoryService;
import hotel.ui.common.HeaderPanel;
import hotel.ui.common.UITheme;
import hotel.util.DateUtil;
import hotel.util.SessionContext;
import hotel.validation.ValidationResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class HousekeeperDashboardFrame extends JFrame {

    private final HousekeepingService housekeepingService = new HousekeepingService();
    private final InventoryService inventoryService = new InventoryService();
    private final HousekeepingDAO housekeepingDAO = new HousekeepingDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    private JTable tasksTable;
    private DefaultTableModel tasksModel;

    private JTable inventoryTable;
    private DefaultTableModel inventoryModel;

    public HousekeeperDashboardFrame() {
        setTitle("GrandStay Hotel - Housekeeping & Turnover Operations");
        setSize(1140, 720);
        setMinimumSize(new Dimension(960, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header Top Bar
        add(new HeaderPanel("Housekeeping Operations"), BorderLayout.NORTH);

        // Tabbed Panel
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.setBackground(Color.WHITE);

        tabs.addTab("  My Cleaning Tasks & Turnovers  ", createTasksPanel());
        tabs.addTab("  Housekeeping Inventory & Supplies  ", createSuppliesPanel());

        add(tabs, BorderLayout.CENTER);

        refreshTasks();
    }

    private JPanel createTasksPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] cols = {"Task ID", "Room #", "Task Type", "Priority", "Assigned Staff", "Status", "Created At", "Completed At", "Notes"};
        tasksModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tasksTable = new JTable(tasksModel);
        UITheme.styleTable(tasksTable);
        panel.add(new JScrollPane(tasksTable), BorderLayout.CENTER);

        // Actions
        JPanel actions = UITheme.createCardPanel();
        actions.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 8));

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshTasks());

        JButton inProgressBtn = UITheme.createSecondaryButton("Mark In-Progress");
        inProgressBtn.addActionListener(e -> updateStatus(HousekeepingTask.Status.IN_PROGRESS));

        JButton completedBtn = UITheme.createPrimaryButton("Mark Completed");
        completedBtn.addActionListener(e -> updateStatus(HousekeepingTask.Status.COMPLETED));

        JButton inspectedBtn = UITheme.createGoldButton("✦ Inspected & Ready for Guests");
        inspectedBtn.addActionListener(e -> updateStatus(HousekeepingTask.Status.INSPECTED));

        actions.add(refreshBtn);
        actions.add(inProgressBtn);
        actions.add(completedBtn);
        actions.add(inspectedBtn);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshTasks() {
        new SwingWorker<List<HousekeepingTask>, Void>() {
            @Override
            protected List<HousekeepingTask> doInBackground() throws Exception {
                return housekeepingService.getAllTasks();
            }

            @Override
            protected void done() {
                try {
                    List<HousekeepingTask> list = get();
                    tasksModel.setRowCount(0);
                    for (HousekeepingTask t : list) {
                        tasksModel.addRow(new Object[]{
                                t.getId(),
                                "Room " + t.getRoomNumber(),
                                t.getTaskType(),
                                t.getPriority().name(),
                                t.getAssignedStaffName() != null ? t.getAssignedStaffName() : "Unassigned",
                                t.getStatus().getDisplayName(),
                                DateUtil.formatDateTime(t.getCreatedAt()),
                                t.getCompletedAt() != null ? DateUtil.formatDateTime(t.getCompletedAt()) : "-",
                                t.getNotes() != null ? t.getNotes() : ""
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void updateStatus(HousekeepingTask.Status status) {
        int selected = tasksTable.getSelectedRow();
        if (selected < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task from the table.", "Select Task", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long taskId = (Long) tasksTable.getValueAt(selected, 0);
        String room = (String) tasksTable.getValueAt(selected, 1);

        String notes = JOptionPane.showInputDialog(this,
                "Optional notes for " + room + " (" + status.getDisplayName() + "):",
                "Task Update", JOptionPane.PLAIN_MESSAGE);

        ValidationResult res = housekeepingService.updateTaskStatus(taskId, status, notes);
        if (res.isValid()) {
            JOptionPane.showMessageDialog(this,
                    "Task updated to: " + status.getDisplayName() +
                            (status == HousekeepingTask.Status.INSPECTED ? "\nRoom is now AVAILABLE for new check-ins!" : ""),
                    "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshTasks();
        } else {
            JOptionPane.showMessageDialog(this, res.getFormattedErrors(), "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createSuppliesPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        String[] cols = {"Item Code", "Item Name", "Category", "In Stock", "Min Level", "Unit", "Location", "Alert"};
        inventoryModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        inventoryTable = new JTable(inventoryModel);
        UITheme.styleTable(inventoryTable);
        panel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JPanel actions = UITheme.createCardPanel();
        actions.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 8));

        JButton useStockBtn = UITheme.createGoldButton("Record Used Supplies (Room Turnover)");
        useStockBtn.addActionListener(e -> openStockOutDialog());

        JButton refreshSuppliesBtn = UITheme.createSecondaryButton("Refresh Stock");
        refreshSuppliesBtn.addActionListener(e -> refreshSupplies());

        actions.add(refreshSuppliesBtn);
        actions.add(useStockBtn);
        panel.add(actions, BorderLayout.SOUTH);

        refreshSupplies();
        return panel;
    }

    private void refreshSupplies() {
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
                                i.getItemCode(),
                                i.getItemName(),
                                i.getCategory(),
                                i.getQuantityOnHand(),
                                i.getMinStockLevel(),
                                i.getUnitOfMeasure(),
                                i.getLocation(),
                                i.isLowStock() ? "⚠️ LOW STOCK" : "OK"
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void openStockOutDialog() {
        try {
            List<InventoryItem> items = inventoryDAO.findAll();
            if (items.isEmpty()) return;

            JDialog d = new JDialog(this, "Record Turnover Consumption", true);
            d.setSize(420, 300);
            d.setLocationRelativeTo(this);
            d.setLayout(new BorderLayout());

            JPanel p = new JPanel(new GridBagLayout());
            p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            GridBagConstraints g = new GridBagConstraints();
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(6, 6, 6, 6);

            JComboBox<InventoryItem> itemCombo = new JComboBox<>();
            for (InventoryItem i : items) itemCombo.addItem(i);

            JSpinner qtySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 50, 1));
            JTextArea reasonArea = new JTextArea("Room turnover restock", 2, 15);

            g.gridx = 0; g.gridy = 0; p.add(new JLabel("Select Supply:"), g);
            g.gridx = 1; p.add(itemCombo, g);

            g.gridx = 0; g.gridy = 1; p.add(new JLabel("Quantity Used:"), g);
            g.gridx = 1; p.add(qtySpinner, g);

            g.gridx = 0; g.gridy = 2; p.add(new JLabel("Room / Notes:"), g);
            g.gridx = 1; p.add(new JScrollPane(reasonArea), g);

            d.add(p, BorderLayout.CENTER);

            JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton submit = UITheme.createPrimaryButton("Record Usage");
            submit.addActionListener(e -> {
                InventoryItem selected = (InventoryItem) itemCombo.getSelectedItem();
                int qty = (Integer) qtySpinner.getValue();
                ValidationResult res = inventoryService.adjustStock(
                        selected.getId(), InventoryTransaction.Type.ROOM_RESTOCK, qty, reasonArea.getText()
                );
                if (res.isValid()) {
                    JOptionPane.showMessageDialog(d, "Recorded consumption of " + qty + " " + selected.getUnitOfMeasure(), "Success", JOptionPane.INFORMATION_MESSAGE);
                    d.dispose();
                    refreshSupplies();
                } else {
                    JOptionPane.showMessageDialog(d, res.getFormattedErrors(), "Stock Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            foot.add(submit);
            d.add(foot, BorderLayout.SOUTH);
            d.setVisible(true);
        } catch (Exception ignored) {}
    }
}
