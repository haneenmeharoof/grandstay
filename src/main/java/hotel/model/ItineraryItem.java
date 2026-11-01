package hotel.model;

import java.time.LocalDate;

public class ItineraryItem {
    private Long id;
    private Long itineraryId;
    private Long placeId;
    private String placeName;
    private String activityName;
    private LocalDate plannedDate;
    private String plannedTime;
    private int sequenceOrder = 1;
    private String notes;

    public ItineraryItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getItineraryId() { return itineraryId; }
    public void setItineraryId(Long itineraryId) { this.itineraryId = itineraryId; }

    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }

    public String getPlaceName() { return placeName; }
    public void setPlaceName(String placeName) { this.placeName = placeName; }

    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }

    public LocalDate getPlannedDate() { return plannedDate; }
    public void setPlannedDate(LocalDate plannedDate) { this.plannedDate = plannedDate; }

    public String getPlannedTime() { return plannedTime; }
    public void setPlannedTime(String plannedTime) { this.plannedTime = plannedTime; }

    public int getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(int sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
