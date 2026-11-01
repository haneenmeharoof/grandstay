# GrandStay repair delivery notes

## Changes in this delivery
- Added `RESERVED` to the Java room-status model and both fresh-install SQL schemas while retaining all prior states (`INSPECTED`, `OUT_OF_SERVICE`, etc.).
- Added `database/migration_add_reserved_room_status.sql` for existing databases. Back up the database and run this migration once before using the updated application.
- Updated the receptionist Live Room Status Matrix to display `Reserved for Upcoming Booking` when a room otherwise physically available/inspected has a pending or confirmed reservation with a future checkout date. The physical room status is not overwritten; this avoids falsely marking a vacant room as occupied.
- Added emergency-contact name and phone validation to digital pre-check-in submission.
- Changed the expected-arrival default from a fixed noon value to one hour from the current local time.
- Preserved the date-based arrivals/departures DAO queries and existing overlapping-reservation checks.

## Important limitations / verification
- The current execution environment does not have Maven installed, so a fresh `mvn clean package` could not be run here. The uploaded project had previously built successfully on the user's Windows machine, but these modifications still need a build there.
- The Pre-Check-In Queue's existing Approve action still approves the submitted information; it does not itself complete physical check-in. Actual check-in remains a separate receptionist operation. This delivery does not claim to have completed the larger queue redesign or full cross-module end-to-end testing.
- The matrix's RESERVED label is a derived operational indicator. It deliberately does not change the underlying room physical-status column. Reservation overlap checks remain the allocation authority.
- Existing database: run the migration SQL before launching the updated build. Do not run reset_database.sql.
