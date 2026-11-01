package hotel.model;

import java.time.LocalDateTime;

public class User {
    private Long id;
    private Long guestId;
    private String username;
    private String email;
    private String passwordHash;
    private String fullName;
    private String phone;
    private Role role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {
        this.role = Role.CUSTOMER;
        this.status = UserStatus.ACTIVE;
    }

    public User(String username, String email, String passwordHash, String fullName, String phone, Role role) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role != null ? role : Role.CUSTOMER;
        this.status = UserStatus.ACTIVE;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    @Override
    public String toString() {
        return fullName + " (" + username + " - " + (role != null ? role.getDisplayName() : "") + ")";
    }
}
