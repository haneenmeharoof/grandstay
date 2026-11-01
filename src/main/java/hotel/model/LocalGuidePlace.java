package hotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LocalGuidePlace {
    private Long id;
    private String name;
    private String category;
    private String description;
    private String address;
    private String openingHours;
    private int estimatedDurationMinutes = 60;
    private BigDecimal estimatedCost = BigDecimal.ZERO;
    private String recommendedTips;
    private boolean active = true;
    private LocalDateTime createdAt;

    public LocalGuidePlace() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }

    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public BigDecimal getEstimatedCost() { return estimatedCost; }
    public void setEstimatedCost(BigDecimal estimatedCost) { this.estimatedCost = estimatedCost != null ? estimatedCost : BigDecimal.ZERO; }

    public String getRecommendedTips() { return recommendedTips; }
    public void setRecommendedTips(String recommendedTips) { this.recommendedTips = recommendedTips; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " (" + category + ")";
    }
}
