package hotel.model;

import java.time.LocalDateTime;

public class Room {
    private Long id;
    private String roomNumber;
    private Long roomTypeId;
    private RoomType roomType; // Join object
    private int floorNumber;
    private RoomStatus status;
    private String notes;
    private LocalDateTime createdAt;

    public Room() {
        this.status = RoomStatus.AVAILABLE;
    }

    public Room(String roomNumber, Long roomTypeId, int floorNumber, RoomStatus status, String notes) {
        this.roomNumber = roomNumber;
        this.roomTypeId = roomTypeId;
        this.floorNumber = floorNumber;
        this.status = status != null ? status : RoomStatus.AVAILABLE;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Long getRoomTypeId() { return roomTypeId; }
    public void setRoomTypeId(Long roomTypeId) { this.roomTypeId = roomTypeId; }

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public int getFloorNumber() { return floorNumber; }
    public void setFloorNumber(int floorNumber) { this.floorNumber = floorNumber; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + (roomType != null ? roomType.getName() : "Type #" + roomTypeId) + " - " + status.getDisplayName() + ")";
    }
}
