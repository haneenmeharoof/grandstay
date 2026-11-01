package hotel.ui.auth;

import hotel.config.AppConfig;
import hotel.database.DatabaseConnection;
import hotel.database.DatabaseInitializer;
import hotel.model.Role;
import hotel.model.User;
import hotel.service.AuthService;
import hotel.ui.common.UITheme;
import hotel.ui.customer.CustomerDashboardFrame;
import hotel.ui.staff.AdminDashboardFrame;
import hotel.ui.staff.HousekeeperDashboardFrame;
import hotel.ui.staff.ReceptionistDashboardFrame;
import hotel.util.SessionContext;
import hotel.validation.ValidationResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();

    private final JTextField identifierField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JButton loginButton = UITheme.createGoldButton("Sign In to GrandStay");
    private final JLabel statusLabel = new JLabel(" ");

    public LoginFrame() {
    this(false);
}

public LoginFrame(boolean customerLogin) {
        setTitle("GrandStay Hotel Management System - Sign In");
        setSize(480, 620);
        setMinimumSize(new Dimension(440, 560));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // Top Brand Header
JPanel headerPanel = new JPanel(new BorderLayout());
headerPanel.setBackground(UITheme.NAVY_DARK);
headerPanel.setPreferredSize(new Dimension(0, 150));
headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 20, 20));

JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
brandRow.setOpaque(false);

JLabel starLogo = new JLabel("✦");
starLogo.setFont(new Font("Segoe UI", Font.BOLD, 36));
starLogo.setForeground(UITheme.GOLD);

JLabel brandTitle = new JLabel("GRANDSTAY");
brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 25));
brandTitle.setForeground(Color.WHITE);

brandRow.add(starLogo);
brandRow.add(brandTitle);

JLabel brandSub = new JLabel("HOTEL & LUXURY SUITES", JLabel.CENTER);
brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
brandSub.setForeground(UITheme.GOLD_LIGHT);

headerPanel.add(brandRow, BorderLayout.CENTER);
headerPanel.add(brandSub, BorderLayout.SOUTH);

add(headerPanel, BorderLayout.NORTH);

        // Center Login Form
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(UITheme.BG_LIGHT);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 15, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel welcomeLabel = new JLabel("Welcome Back", JLabel.CENTER);
        welcomeLabel.setFont(UITheme.FONT_SUBTITLE);
        welcomeLabel.setForeground(UITheme.NAVY_DARK);

        JLabel unLabel = new JLabel("Username or Email:");
        unLabel.setFont(UITheme.FONT_BOLD);
        identifierField.setPreferredSize(new Dimension(0, 34));

        JLabel pwLabel = new JLabel("Password:");
        pwLabel.setFont(UITheme.FONT_BOLD);
        passwordField.setPreferredSize(new Dimension(0, 34));

        statusLabel.setFont(UITheme.FONT_SMALL);
        statusLabel.setForeground(UITheme.DANGER);
        statusLabel.setHorizontalAlignment(JLabel.CENTER);

        loginButton.setFont(UITheme.FONT_BOLD);
        loginButton.setPreferredSize(new Dimension(0, 40));
        loginButton.addActionListener(e -> handleLogin());

        // Submit on Enter
        passwordField.addActionListener(e -> handleLogin());
        identifierField.addActionListener(e -> handleLogin());

        // Registration link — shown only when customer login is selected
        JButton registerLink = new JButton(
        "<html>Don't have an account? <span style='color:#b8860b; font-weight:bold;'>Register as Guest</span></html>"
        );
        registerLink.setFont(UITheme.FONT_REGULAR);
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setFocusPainted(false);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.addActionListener(e -> new RegisterDialog(this).setVisible(true));
        registerLink.setVisible(false);

        JButton customerLoginButton = new JButton("Customer Login");
JButton staffLoginButton = new JButton("Staff Login");

customerLoginButton.addActionListener(e -> {
    welcomeLabel.setText("Customer Login");
    registerLink.setVisible(true);
    revalidate();
    repaint();
});

staffLoginButton.addActionListener(e -> {
    welcomeLabel.setText("Staff Login");
    registerLink.setVisible(false);
    revalidate();
    repaint();
});

JPanel loginModePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
loginModePanel.setOpaque(false);
loginModePanel.add(staffLoginButton);
loginModePanel.add(customerLoginButton);


        int r = 0;
        gbc.gridx = 0; gbc.gridy = r++; centerPanel.add(welcomeLabel, gbc);
        gbc.gridx = 0; gbc.gridy = r++; centerPanel.add(loginModePanel, gbc);
        gbc.gridx = 0; gbc.gridy = r++; centerPanel.add(unLabel, gbc);
        gbc.gridx = 0; gbc.gridy = r++; centerPanel.add(identifierField, gbc);
        gbc.gridx = 0; gbc.gridy = r++; centerPanel.add(pwLabel, gbc);
        gbc.gridx = 0; gbc.gridy = r++; centerPanel.add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy = r++; centerPanel.add(statusLabel, gbc);
        gbc.gridx = 0; gbc.gridy = r++; centerPanel.add(loginButton, gbc);
        gbc.gridx = 0; gbc.gridy = r++; centerPanel.add(registerLink, gbc);
        gbc.gridx = 0; gbc.gridy = r++; centerPanel.add(Box.createVerticalStrut(8), gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom Bar with DB Settings
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(UITheme.BG_LIGHT);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JButton dbBtn = new JButton("⚙ Database Connection Settings");
        dbBtn.setFont(UITheme.FONT_SMALL);
        dbBtn.setBorderPainted(false);
        dbBtn.setContentAreaFilled(false);
        dbBtn.setForeground(UITheme.TEXT_MUTED);
        dbBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dbBtn.addActionListener(e -> openDatabaseConfigDialog());
        footer.add(dbBtn);

        add(footer, BorderLayout.SOUTH);
    }


    private void handleLogin() {
        String identifier = identifierField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (identifier.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            statusLabel.setForeground(UITheme.DANGER);
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setText("Authenticating credentials...");
        statusLabel.setForeground(UITheme.INFO);

        new SwingWorker<ValidationResult, Void>() {
            @Override
            protected ValidationResult doInBackground() {
                return authService.authenticate(identifier, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                try {
                    ValidationResult result = get();
                    if (result.isValid()) {
                        statusLabel.setText("Login successful! Opening dashboard...");
                        statusLabel.setForeground(UITheme.SUCCESS);

                        User user = SessionContext.getCurrentUser();
                        dispose(); // Close login window

                        SwingUtilities.invokeLater(() -> {
                            Role role = user.getRole();
                            switch (role) {
                                case ADMIN -> new AdminDashboardFrame().setVisible(true);
                                case RECEPTIONIST -> new ReceptionistDashboardFrame().setVisible(true);
                                case HOUSEKEEPING -> new HousekeeperDashboardFrame().setVisible(true);
                                case CUSTOMER -> new CustomerDashboardFrame().setVisible(true);
                            }
                        });
                    } else {
                        statusLabel.setText(result.getFormattedErrors());
                        statusLabel.setForeground(UITheme.DANGER);
                    }
                } catch (Exception ex) {
                    statusLabel.setText("Connection failure: " + ex.getMessage());
                    statusLabel.setForeground(UITheme.DANGER);
                }
            }
        }.execute();
    }

    private void openDatabaseConfigDialog() {
        JDialog d = new JDialog(this, "MySQL Database Settings", true);
        d.setSize(440, 380);
        d.setLocationRelativeTo(this);
        d.setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 6, 6, 6);

        JTextField hostField = new JTextField(AppConfig.getDbHost(), 15);
        JTextField portField = new JTextField(AppConfig.getDbPort(), 15);
        JTextField dbNameField = new JTextField(AppConfig.getDbName(), 15);
        JTextField userField = new JTextField(AppConfig.getDbUser(), 15);
        JPasswordField passField = new JPasswordField(AppConfig.getDbPassword(), 15);

        int r = 0;
        addConfigRow(p, g, r++, "DB Host:", hostField);
        addConfigRow(p, g, r++, "DB Port:", portField);
        addConfigRow(p, g, r++, "Database Name:", dbNameField);
        addConfigRow(p, g, r++, "DB Username:", userField);
        addConfigRow(p, g, r++, "DB Password:", passField);

        d.add(p, BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton testBtn = UITheme.createSecondaryButton("Test Connection");
        testBtn.addActionListener(e -> {
            AppConfig.saveToFile(hostField.getText().trim(), portField.getText().trim(),
                    dbNameField.getText().trim(), userField.getText().trim(), new String(passField.getPassword()));
            if (DatabaseConnection.testConnection()) {
                JOptionPane.showMessageDialog(d, "Connection Successful! MySQL database is reachable.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else if (DatabaseConnection.testServerConnection()) {
                JOptionPane.showMessageDialog(d, "Connected to MySQL Server! Database 'hotel_management' will be automatically created upon initialization.", "Server Connected", JOptionPane.INFORMATION_MESSAGE);
                DatabaseInitializer.initialize();
            } else {
                JOptionPane.showMessageDialog(d, "Connection failed. Please check host, port, username, and password.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton saveBtn = UITheme.createGoldButton("Save & Initialize");
        saveBtn.addActionListener(e -> {
            AppConfig.saveToFile(hostField.getText().trim(), portField.getText().trim(),
                    dbNameField.getText().trim(), userField.getText().trim(), new String(passField.getPassword()));
            DatabaseInitializer.initialize();
            d.dispose();
            statusLabel.setText("Database configuration saved.");
            statusLabel.setForeground(UITheme.SUCCESS);
        });

        foot.add(testBtn);
        foot.add(saveBtn);
        d.add(foot, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void addConfigRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0; gbc.gridy = row;
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BOLD);
        panel.add(l, gbc);

        gbc.gridx = 1;
        panel.add(comp, gbc);
    }
}
