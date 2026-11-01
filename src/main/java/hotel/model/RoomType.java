package hotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RoomType {
    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private int maxCapacity;
    private String bedType;
    private String amenities;
    private boolean active = true;
    private LocalDateTime createdAt;

    public RoomType() {}

    public RoomType(String code, String name, String description, BigDecimal basePrice, int maxCapacity, String bedType, String amenities) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.maxCapacity = maxCapacity;
        this.bedType = bedType;
        this.amenities = amenities;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public String getBedType() { return bedType; }
    public void setBedType(String bedType) { this.bedType = bedType; }

    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " ($" + (basePrice != null ? basePrice.toPlainString() : "0.00") + "/night)";
    }
}
