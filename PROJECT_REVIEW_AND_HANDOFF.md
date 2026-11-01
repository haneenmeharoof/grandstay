# GrandStay Hotel Management System — Review Handoff

## Changes made in this review copy

1. **Customer service-order privacy**
   - Added a guest-scoped service-order query.
   - Customer service history now loads only orders for the signed-in guest rather than every service order.
   - Removed the customer UI's fallback reservation ID and guest ID (`1L`). A customer must select one of their own reservations.
   - Added server-side ownership validation before a service request is inserted.
   - Replaced the silent exception swallow in service-history loading with visible error reporting.

2. **Trip Planner completion**
   - Kept the separate Trip Planner tab.
   - Added UI actions to add an activity to a selected itinerary, view its activities, and delete an itinerary.
   - Activity dates are validated by the existing service against the itinerary date range.
   - Deleting an itinerary uses the existing database `ON DELETE CASCADE` relationship to remove its activities.

## Verification performed

- Compiled all 80 Java source files with `javac --release 17`, using the project's existing dependency bundle on the classpath. Compilation completed successfully.
- Inspected the Trip Planner DAO against the supplied schema: `trip_itinerary_items.itinerary_id`/`trip_itineraries.id` cascade relationship and `place_id`/`local_guide_places.id` set-null relationship were compatible with the DAO.

## Not verified in this environment

- A fresh Maven build: Maven is not installed in this review environment.
- Live MySQL connection, database writes, and Swing end-to-end behavior: no access to the user's local MySQL server or credentials.
- Full regression testing of every staff/admin workflow and every database feature.

## Before presenting

1. On the original Windows machine, set `DB_PASSWORD` and run `mvn clean package`.
2. Launch the newly built JAR (do not use an older JAR copied from the original `target` directory).
3. Log in as a customer; create an itinerary, add an activity within its dates, view activities, delete the itinerary, and confirm it disappears after reopening the Trip Planner.
4. Select a reservation under My Reservations, submit a service request, and confirm only that guest's service requests appear in In-Room Services.
5. Run the supplied test script and any manual checks described in `docs/TESTING.md`.

The original uploaded project was not modified. This ZIP is a separate review copy.
