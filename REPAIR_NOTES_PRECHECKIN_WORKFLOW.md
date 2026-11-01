# Pre-Check-In Workflow Repair

## Behaviour
- Today's arrivals continue to include today's confirmed reservations that have not submitted digital pre-check-in.
- A reservation with a digital pre-check-in still in `SUBMITTED` state is hidden from Today's Arrivals while it remains in the pending queue.
- Receptionist approval now requires confirmation that the guest is physically present and that the reservation arrival date is today.
- Approval invokes the existing reservation check-in service. On success, the reservation becomes `CHECKED_IN` and its room becomes `OCCUPIED`; the pre-check-in submission is marked `APPROVED` and removed from the pending queue.
- The approved pre-check-in record remains stored in `pre_checkins` for history.
- Rejected submissions continue to be handled by the existing review flow.

## Files changed
- `src/main/java/hotel/dao/PreCheckinDAO.java`: added lookup by pre-check-in ID.
- `src/main/java/hotel/dao/ReservationDAO.java`: excludes only `SUBMITTED` digital pre-check-ins from today's arrivals.
- `src/main/java/hotel/ui/staff/ReceptionistDashboardFrame.java`: approval performs actual check-in and refreshes the relevant screens.

## Verification
All Java source files compiled successfully with Java 17 `javac`, using the project's existing dependency-inclusive JAR as the classpath. A live MySQL/UI integration test was not possible in this environment; test the approval flow against a backed-up test database before production use.
