package hotel.ui.auth;

import hotel.service.AuthService;
import hotel.ui.common.UITheme;
import hotel.validation.ValidationResult;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class RegisterDialog extends JDialog {

    private final AuthService authService = new AuthService();

    private final JTextField usernameField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JTextField firstNameField = new JTextField(18);
    private final JTextField lastNameField = new JTextField(18);
    private final JTextField phoneField = new JTextField(18);
    private final JTextField addressField = new JTextField(18);

    public RegisterDialog(Frame parent) {
        super(parent, "Customer Registration - GrandStay Hotel", true);
        setSize(480, 500);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 16));
        header.setBackground(UITheme.NAVY_DARK);
        JLabel title = new JLabel("Join GrandStay Guest Club");
        title.setFont(UITheme.FONT_SUBTITLE);
        title.setForeground(UITheme.GOLD);
        header.add(title);
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.BG_LIGHT);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        addFormRow(form, gbc, row++, "First Name *:", firstNameField);
        addFormRow(form, gbc, row++, "Last Name *:", lastNameField);
        addFormRow(form, gbc, row++, "Email Address *:", emailField);
        addFormRow(form, gbc, row++, "Phone Number:", phoneField);
        addFormRow(form, gbc, row++, "Address / City:", addressField);
        addFormRow(form, gbc, row++, "Choose Username *:", usernameField);
        addFormRow(form, gbc, row++, "Choose Password *:", passwordField);

        add(form, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        footer.setBackground(UITheme.BG_LIGHT);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton cancelBtn = UITheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton registerBtn = UITheme.createPrimaryButton("Create Account");
        registerBtn.addActionListener(e -> handleRegistration());

        footer.add(cancelBtn);
        footer.add(registerBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(l, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 30));
        panel.add(field, gbc);
    }

    private void handleRegistration() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String phone = phoneField.getText();
        String address = addressField.getText();

        ValidationResult result = authService.registerCustomer(
                username, email, password, firstName, lastName, phone, address
        );

        if (result.isValid()) {
            JOptionPane.showMessageDialog(this,
                    "Account registered successfully! You can now log in with your credentials.",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    result.getFormattedErrors(),
                    "Registration Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}
