package hotel.ui.common;

import hotel.config.AppConfig;
import hotel.model.User;
import hotel.service.AuthService;
import hotel.service.CustomerFeatureService;
import hotel.ui.auth.LoginFrame;
import hotel.util.SessionContext;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class HeaderPanel extends JPanel {

    private final AuthService authService = new AuthService();
    private final CustomerFeatureService customerFeatureService = new CustomerFeatureService();
    private JLabel notifBadge;

    public HeaderPanel(String currentSectionTitle) {
        setLayout(new BorderLayout());
        setBackground(UITheme.NAVY_DARK);
        setPreferredSize(new Dimension(0, 60));
        setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));

        // Left Branding
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPanel.setOpaque(false);

        JLabel brandIcon = new JLabel("✦");
        brandIcon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandIcon.setForeground(UITheme.GOLD);

        JLabel brandLabel = new JLabel("GRANDSTAY");
        brandLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        brandLabel.setForeground(Color.WHITE);

        JLabel sep = new JLabel("|");
        sep.setFont(UITheme.FONT_REGULAR);
        sep.setForeground(UITheme.TEXT_MUTED);

        JLabel sectionLabel = new JLabel(currentSectionTitle != null ? currentSectionTitle : "Hotel Management System");
        sectionLabel.setFont(UITheme.FONT_REGULAR);
        sectionLabel.setForeground(new Color(0xCB, 0xD5, 0xE1));

        leftPanel.add(brandIcon);
        leftPanel.add(brandLabel);
        leftPanel.add(sep);
        leftPanel.add(sectionLabel);

        // Right User Information & Actions
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightPanel.setOpaque(false);

        User user = SessionContext.getCurrentUser();
        String displayName = user != null ? user.getFullName() : "Guest User";
        String roleText = user != null ? user.getRole().getDisplayName() : "Visitor";

        // Unread notifications indicator
        int unreadCount = 0;
        if (user != null) {
            try {
                unreadCount = customerFeatureService.getUnreadNotificationCount(user.getId());
            } catch (Exception ignored) {}
        }

        notifBadge = new JLabel("🔔 " + (unreadCount > 0 ? "(" + unreadCount + ")" : ""));
        notifBadge.setFont(UITheme.FONT_BOLD);
        notifBadge.setForeground(unreadCount > 0 ? UITheme.GOLD : Color.LIGHT_GRAY);
        notifBadge.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notifBadge.setToolTipText("In-app notifications");
        notifBadge.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showNotificationsDialog();
            }
        });
        rightPanel.add(notifBadge);

        JLabel userLabel = new JLabel(displayName);
        userLabel.setFont(UITheme.FONT_BOLD);
        userLabel.setForeground(Color.WHITE);

        JLabel roleBadge = new JLabel(" " + roleText + " ");
        roleBadge.setFont(UITheme.FONT_SMALL);
        roleBadge.setForeground(UITheme.NAVY_DARK);
        roleBadge.setBackground(UITheme.GOLD);
        roleBadge.setOpaque(true);
        roleBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(UITheme.FONT_SMALL);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(UITheme.NAVY_MEDIUM);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> handleLogout());

        rightPanel.add(userLabel);
        rightPanel.add(roleBadge);
        rightPanel.add(Box.createHorizontalStrut(6));
        rightPanel.add(logoutBtn);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    private void showNotificationsDialog() {
        User user = SessionContext.getCurrentUser();
        if (user == null) return;
        try {
            var list = customerFeatureService.getNotifications(user.getId());
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "You have no notifications.", "Notifications", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (var n : list) {
                sb.append("• [").append(n.getTitle()).append("] ").append(n.getMessage()).append("\n\n");
            }
            customerFeatureService.markAllNotificationsRead(user.getId());
            notifBadge.setText("🔔");
            JOptionPane.showMessageDialog(this, sb.toString(), "Your Notifications", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching notifications: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out of your session?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            Window currentWindow = SwingUtilities.getWindowAncestor(this);
            if (currentWindow != null) {
                currentWindow.dispose();
            }
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
