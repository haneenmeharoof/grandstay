# GrandStay repair notes — 2026-10-30

## Changes made

1. **Reception billing workspace**
   - Added a reservation/guest table directly inside Billing & Payments.
   - Record Payment and View / Print Folio now work from the selected row in that table; users no longer need to switch to Reservations & Walk-in to select a guest.
   - Added a refresh action and reload after payment/check-in/check-out.

2. **Today's check-in KPI**
   - The administrator dashboard count now includes reservations scheduled to check in today that have already transitioned to `CHECKED_IN`, not only pending/confirmed arrivals. Cancelled and no-show reservations remain excluded.
   - The receptionist arrivals table remains a pending-arrivals queue, so a guest disappears from that queue after successful check-in as intended.

3. **Room status safety**
   - The administrator can no longer mark a room `AVAILABLE` or `INSPECTED` while a reservation for that room is still `CHECKED_IN`.
   - The administrator dashboard refreshes its overview and room list when its window regains focus.

4. **Atomic check-in/check-out persistence**
   - Reservation status changes now use the same JDBC transaction connection as room status changes.
   - Checkout housekeeping turnover task creation now uses that same transaction as well. A failed operation therefore rolls back the reservation, room, and housekeeping changes together rather than leaving partial state.

## Validation status

- Source-level Java parsing was checked with the installed JDK; no Java syntax errors were reported.
- A full Maven build and live MySQL/Swing workflow test could not be run in this build environment because Maven and the project dependency cache are unavailable. Run `mvn clean package` on the development machine before replacing the current project. This ZIP is a repaired source package, not a claim that live integration testing has passed.

## Follow-up repair pass (2026-10-31)

- Front desk arrivals now list all reservations with `check_in_date = CURDATE()` except `CANCELLED` and `NO_SHOW`, including reservations already checked in.
- Front desk departures now list all reservations with `check_out_date = CURDATE()` except `CANCELLED` and `NO_SHOW`, including reservations already checked out.
- Removed the status qualifiers from the two front desk section headings.
- The front desk actions now give a clear message when the selected arrival is already checked in or the selected departure is already checked out. Existing service-layer state validation remains authoritative.
- Review submission still requires an actual `CHECKED_OUT` reservation; reaching the scheduled checkout date does not silently check a guest out. Added account ownership validation and removed fallback customer IDs from the review UI.

Verification note: source changes were inspected, but Maven is not installed in this repair environment and dependencies are not cached. Run `mvn clean package` from the directory containing `pom.xml`, then test with the local MySQL test database. No live database data was modified by this repair pass.
