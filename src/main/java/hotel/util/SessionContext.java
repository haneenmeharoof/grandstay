package hotel.util;

import hotel.model.Role;
import hotel.model.User;

/**
 * Thread-safe application session context for managing logged-in user state.
 */
public final class SessionContext {

    private static volatile User currentUser = null;

    private SessionContext() {}

    public static synchronized void setCurrentUser(User user) {
        currentUser = user;
    }

    public static synchronized User getCurrentUser() {
        return currentUser;
    }

    public static synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    public static synchronized void clearSession() {
        currentUser = null;
    }

    public static synchronized Role getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public static synchronized boolean hasRole(Role role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public static synchronized boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    public static synchronized boolean isReceptionist() {
        return hasRole(Role.RECEPTIONIST) || isAdmin();
    }

    public static synchronized boolean isHousekeeping() {
        return hasRole(Role.HOUSEKEEPING) || isAdmin();
    }

    public static synchronized boolean isCustomer() {
        return hasRole(Role.CUSTOMER);
    }
}
