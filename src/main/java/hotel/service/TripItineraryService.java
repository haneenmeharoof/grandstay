package hotel.service;

import hotel.dao.TripItineraryDAO;
import hotel.model.TripItinerary;
import hotel.model.ItineraryItem;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TripItineraryService {

    private final TripItineraryDAO itineraryDAO;

    public TripItineraryService() {
        this.itineraryDAO = new TripItineraryDAO();
    }

    public Long createItinerary(
            Long userId,
            String title,
            LocalDate startDate,
            LocalDate endDate,
            String notes) throws SQLException {

        if (userId == null) {
            throw new IllegalArgumentException("User login is required.");
        }

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Enter an itinerary title.");
        }

        if (startDate == null || endDate == null
                || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Invalid itinerary dates.");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past.");
        }

        TripItinerary itinerary = new TripItinerary();
        itinerary.setUserId(userId);
        itinerary.setTitle(title.trim());
        itinerary.setStartDate(startDate);
        itinerary.setEndDate(endDate);
        itinerary.setNotes(notes == null ? "" : notes.trim());

        return itineraryDAO.create(itinerary);
    }

    public void addActivity(
            Long itineraryId,
            Long userId,
            ItineraryItem item) throws SQLException {

        if (itineraryId == null || userId == null || item == null) {
            throw new IllegalArgumentException("Invalid activity details.");
        }

        TripItinerary itinerary = itineraryDAO
                .findById(itineraryId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Itinerary not found or access denied."));

        if (item.getActivityName() == null
                || item.getActivityName().trim().isEmpty()) {
            throw new IllegalArgumentException("Enter an activity name.");
        }

        if (item.getPlannedDate() == null
                || item.getPlannedDate().isBefore(itinerary.getStartDate())
                || item.getPlannedDate().isAfter(itinerary.getEndDate())) {
            throw new IllegalArgumentException(
                    "Activity date must be within the itinerary dates.");
        }

        item.setItineraryId(itineraryId);

        itineraryDAO.addItem(item);
    }

    public List<TripItinerary> getMyItineraries(Long userId)
            throws SQLException {

        if (userId == null) {
            throw new IllegalArgumentException("User login is required.");
        }

        List<TripItinerary> itineraries =
                itineraryDAO.findByUserId(userId);

        for (TripItinerary itinerary : itineraries) {
            itinerary.setItems(
                    itineraryDAO.findItems(itinerary.getId()));
        }

        return itineraries;
    }

    public boolean deleteItinerary(Long itineraryId, Long userId)
            throws SQLException {

        if (itineraryId == null || userId == null) {
            throw new IllegalArgumentException("Invalid itinerary.");
        }

        return itineraryDAO.delete(itineraryId, userId);
    }
}
