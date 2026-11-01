package hotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TripItinerary {
    private Long id;
    private Long userId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
    private LocalDateTime createdAt;
    private List<ItineraryItem> items = new ArrayList<>();

    public TripItinerary() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<ItineraryItem> getItems() { return items; }
    public void setItems(List<ItineraryItem> items) { this.items = items != null ? items : new ArrayList<>(); }
}
