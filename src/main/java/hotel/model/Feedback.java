package hotel.model;

import java.time.LocalDateTime;

public class Feedback {
    public enum ModerationStatus {
        PENDING("Pending Moderation"),
        APPROVED("Approved / Visible"),
        REJECTED("Rejected"),
        HIDDEN("Hidden");

        private final String displayName;

        ModerationStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private Long id;
    private Long reservationId;
    private String reservationRef;
    private Long guestId;
    private String guestName;
    private Long userId;
    private int rating = 5;
    private String reviewTitle;
    private String reviewText;
    private ModerationStatus moderationStatus = ModerationStatus.PENDING;
    private Long moderatedByUserId;
    private LocalDateTime moderatedAt;
    private LocalDateTime createdAt;

    public Feedback() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public String getReservationRef() { return reservationRef; }
    public void setReservationRef(String reservationRef) { this.reservationRef = reservationRef; }

    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = Math.max(1, Math.min(5, rating)); }

    public String getReviewTitle() { return reviewTitle; }
    public void setReviewTitle(String reviewTitle) { this.reviewTitle = reviewTitle; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public ModerationStatus getModerationStatus() { return moderationStatus; }
    public void setModerationStatus(ModerationStatus moderationStatus) { this.moderationStatus = moderationStatus; }

    public Long getModeratedByUserId() { return moderatedByUserId; }
    public void setModeratedByUserId(Long moderatedByUserId) { this.moderatedByUserId = moderatedByUserId; }

    public LocalDateTime getModeratedAt() { return moderatedAt; }
    public void setModeratedAt(LocalDateTime moderatedAt) { this.moderatedAt = moderatedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
