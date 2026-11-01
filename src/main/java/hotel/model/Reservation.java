package hotel.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {
    private Long id;
    private String referenceNumber;
    private Long guestId;
    private Guest guest;
    private Long userId;
    private Long roomId;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numberOfGuests;
    private int nights;
    private BigDecimal roomRate = BigDecimal.ZERO;
    private BigDecimal totalRoomCharge = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal serviceFee = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal paidAmount = BigDecimal.ZERO;
    private ReservationStatus status = ReservationStatus.CONFIRMED;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private String specialRequests;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Reservation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }

    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public int getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(int numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    public int getNights() { return nights; }
    public void setNights(int nights) { this.nights = nights; }

    public BigDecimal getRoomRate() { return roomRate; }
    public void setRoomRate(BigDecimal roomRate) { this.roomRate = roomRate != null ? roomRate : BigDecimal.ZERO; }

    public BigDecimal getTotalRoomCharge() { return totalRoomCharge; }
    public void setTotalRoomCharge(BigDecimal totalRoomCharge) { this.totalRoomCharge = totalRoomCharge != null ? totalRoomCharge : BigDecimal.ZERO; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount != null ? taxAmount : BigDecimal.ZERO; }

    public BigDecimal getServiceFee() { return serviceFee; }
    public void setServiceFee(BigDecimal serviceFee) { this.serviceFee = serviceFee != null ? serviceFee : BigDecimal.ZERO; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount != null ? paidAmount : BigDecimal.ZERO; }

    public BigDecimal getBalanceDue() {
        BigDecimal total = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        BigDecimal paid = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        BigDecimal balance = total.subtract(paid);
        return balance.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : balance;
    }

    public boolean isFullyPaid() {
        return getBalanceDue().compareTo(BigDecimal.ZERO) <= 0;
    }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return referenceNumber + " (" + (guest != null ? guest.getFullName() : "Guest #" + guestId) + " - " + status.getDisplayName() + ")";
    }
}
