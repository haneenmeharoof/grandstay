package hotel.service;

import hotel.dao.AuditLogDAO;
import hotel.dao.GuestDAO;
import hotel.dao.UserDAO;
import hotel.model.AuditLog;
import hotel.model.Guest;
import hotel.model.Role;
import hotel.model.User;
import hotel.model.UserStatus;
import hotel.util.PasswordUtil;
import hotel.util.SessionContext;
import hotel.validation.InputValidator;
import hotel.validation.ValidationResult;
import java.sql.SQLException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDAO userDAO = new UserDAO();
    private final GuestDAO guestDAO = new GuestDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    public ValidationResult authenticate(String identifier, String plainPassword) {
        if (identifier == null || identifier.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
            return ValidationResult.error("Please enter both username/email and password.");
        }

        try {
            Optional<User> userOpt = userDAO.findByUsernameOrEmail(identifier.trim());
            if (userOpt.isEmpty()) {
                return ValidationResult.error("Invalid credentials. Please check your username and password.");
            }

            User user = userOpt.get();
            if (user.getStatus() != UserStatus.ACTIVE) {
                return ValidationResult.error("Account is currently " + user.getStatus().name().toLowerCase() + ". Please contact hotel administration.");
            }

            if (!PasswordUtil.checkPassword(plainPassword, user.getPasswordHash())) {
                return ValidationResult.error("Invalid credentials. Please check your username and password.");
            }

            // Set logged in user session
            SessionContext.setCurrentUser(user);
            auditLogDAO.log(new AuditLog(user.getId(), user.getUsername(), user.getRole().name(), "LOGIN", "USER", String.valueOf(user.getId()), "Successful login"));
            logger.info("User '{}' ({}) logged in successfully.", user.getUsername(), user.getRole());
            return ValidationResult.success();
        } catch (SQLException e) {
            logger.error("Database error during authentication: {}", e.getMessage(), e);
            return ValidationResult.error("Database connection error: " + e.getMessage());
        }
    }

    public ValidationResult registerCustomer(String username, String email, String password,
                                             String firstName, String lastName, String phone, String address) {
        String fullName = (firstName != null ? firstName.trim() : "") + " " + (lastName != null ? lastName.trim() : "");
        ValidationResult val = InputValidator.validateUserRegistration(username, email, password, fullName);
        if (!val.isValid()) {
            return val;
        }

        try {
            // Check uniqueness
            if (userDAO.findByUsernameOrEmail(username.trim()).isPresent()) {
                return ValidationResult.error("Username is already taken. Please choose another.");
            }
            if (userDAO.findByUsernameOrEmail(email.trim()).isPresent()) {
                return ValidationResult.error("An account with this email already exists. Please log in.");
            }

            // Create guest profile
            Guest guest = new Guest(firstName.trim(), lastName.trim(), email.trim(), phone != null ? phone.trim() : "");
            guest.setAddress(address != null ? address.trim() : "");
            Long guestId = guestDAO.insert(guest);

            // Create customer user
            String hashedPassword = PasswordUtil.hashPassword(password);
            User user = new User(username.trim(), email.trim(), hashedPassword, fullName, phone, Role.CUSTOMER);
            user.setGuestId(guestId);
            Long userId = userDAO.insert(user);

            auditLogDAO.log(new AuditLog(userId, username, Role.CUSTOMER.name(), "REGISTER", "USER", String.valueOf(userId), "Self customer registration"));
            logger.info("New customer registered: '{}' (ID: {})", username, userId);
            return ValidationResult.success();
        } catch (SQLException e) {
            logger.error("Error during customer registration: {}", e.getMessage(), e);
            return ValidationResult.error("Registration failed due to a database error: " + e.getMessage());
        }
    }

    public void logout() {
        User u = SessionContext.getCurrentUser();
        if (u != null) {
            auditLogDAO.log(new AuditLog(u.getId(), u.getUsername(), u.getRole().name(), "LOGOUT", "USER", String.valueOf(u.getId()), "User logged out"));
            logger.info("User '{}' logged out.", u.getUsername());
        }
        SessionContext.clearSession();
    }
}
