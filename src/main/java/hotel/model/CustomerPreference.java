package hotel.model;

import java.time.LocalDateTime;

public class CustomerPreference {
    private Long id;
    private Long userId;
    private String preferredFloor;
    private String preferredBedType;
    private boolean quietRoom;
    private boolean highFloor;
    private boolean nearElevator;
    private String dietaryNeeds;
    private String notes;
    private LocalDateTime updatedAt;

    public CustomerPreference() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPreferredFloor() { return preferredFloor; }
    public void setPreferredFloor(String preferredFloor) { this.preferredFloor = preferredFloor; }

    public String getPreferredBedType() { return preferredBedType; }
    public void setPreferredBedType(String preferredBedType) { this.preferredBedType = preferredBedType; }

    public boolean isQuietRoom() { return quietRoom; }
    public void setQuietRoom(boolean quietRoom) { this.quietRoom = quietRoom; }

    public boolean isHighFloor() { return highFloor; }
    public void setHighFloor(boolean highFloor) { this.highFloor = highFloor; }

    public boolean isNearElevator() { return nearElevator; }
    public void setNearElevator(boolean nearElevator) { this.nearElevator = nearElevator; }

    public String getDietaryNeeds() { return dietaryNeeds; }
    public void setDietaryNeeds(String dietaryNeeds) { this.dietaryNeeds = dietaryNeeds; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
