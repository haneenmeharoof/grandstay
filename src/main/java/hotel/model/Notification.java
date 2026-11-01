package hotel.model;

import java.time.LocalDateTime;

public class Notification {
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private String notificationType = "GENERAL";
    private boolean read = false;
    private String referenceId;
    private LocalDateTime createdAt;

    public Notification() {}

    public Notification(Long userId, String title, String message, String notificationType, String referenceId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType != null ? notificationType : "GENERAL";
        this.referenceId = referenceId;
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
