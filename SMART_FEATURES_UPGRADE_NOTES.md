# GrandStay Smart Features Upgrade Notes

## Included changes

### Smart Room Allocation
- Date-based room search now permits a room currently marked OCCUPIED only for a future arrival date.
- Existing reservation-overlap exclusion remains in force for PENDING, CONFIRMED, and CHECKED_IN reservations using the half-open stay interval rule (existing check-in < requested check-out AND existing check-out > requested check-in).
- MAINTENANCE, OUT_OF_SERVICE, DIRTY, CLEANING and other non-ready physical states remain excluded. Same-day/current arrivals do not receive currently occupied rooms.

### Hotel Business Intelligence
Added report choices to the existing Reports & Analytics screen:
- 14-day booked-room forecast
- Monthly completed-payment revenue trend
- Room-category booking / room-night / room-charge performance
- Booking-source analysis
- Cancellation statistics
- Housekeeping completion and inspection performance

All reports use existing schema tables and columns. No database migration or destructive SQL is required.

### Smart Guest Arrival Management
The existing pre-check-in workflow was retained; this upgrade does not replace the existing queue approval/check-in implementation.

## Data interpretation notes
- Booking source is inferred from `reservations.user_id`: NULL is treated as front-desk/walk-in; non-NULL is treated as customer-account/online. This is an inference from the current schema, not a dedicated channel field.
- Revenue trend reports completed payment transactions, not reservation face value.
- Occupancy forecast counts active reserved room-nights for each date, not a prediction model trained on historical demand.

## Verification
- All Java main-source files compiled successfully with `javac --release 17` against the project's existing dependency bundle.
- Maven was not available in the packaging environment, so `mvn clean package` could not be run here.
- Live MySQL integration tests were not run in this environment. Test the new reports against a backup/test database before production use.
- No schema changes were made.
