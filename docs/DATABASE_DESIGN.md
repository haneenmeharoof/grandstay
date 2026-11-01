# GrandStay Hotel Management System — Database Design

## 1. Database Specifications
- **Database Name**: `hotel_management`
- **Engine**: MySQL InnoDB
- **Default Character Set**: `utf8mb4`
- **Default Collation**: `utf8mb4_unicode_ci`

---

## 2. Entity Relationship Overview
The schema consists of 25 relational tables enforcing referential integrity via foreign key constraints with indexed lookup columns:

1. `guests` — Registered guest contact details, identification documents, and historical notes.
2. `users` — Unified authentication table with BCrypt hashed passwords, role ENUMs, and status.
3. `room_types` — Category definitions (e.g. Standard King, Ocean View Suite) with base nightly pricing and capacity.
4. `rooms` — Individual hotel rooms, floor numbers, and operational status (`AVAILABLE`, `OCCUPIED`, `DIRTY`, `CLEANING`, `INSPECTED`, `MAINTENANCE`, `OUT_OF_SERVICE`).
5. `reservations` — Core booking entity storing check-in/out dates, guest count, nights, calculated charges, paid amounts, and statuses.
6. `reservation_guests` — Primary and accompanying guest names for each reservation.
7. `invoices` — Official billing folios with subtotal, taxes, fees, and remaining balance due.
8. `invoice_items` — Line items attached to an invoice (room nights, service fees, meals, amenities).
9. `payments` — Completed transactions recorded against reservations and folios.
10. `refunds` — Refund tracking records detailing amount, authorization, and reason.
11. `housekeeping_tasks` — Cleaning orders linked to rooms with priority, status, and inspection sign-off.
12. `maintenance_tickets` — Repair requests, technician assignments, and room lockout flags.
13. `inventory_items` — Hotel supplies (linens, toiletries, coffee pods) with min stock thresholds.
14. `inventory_transactions` — Full inventory audit log for stock additions, room restocks, and deductions.
15. `service_orders` — In-room guest requests (towels, room service, housekeeping, amenities).
16. `feedback` — 1-to-5 star ratings and written reviews with moderation statuses (`PENDING`, `APPROVED`, `REJECTED`, `HIDDEN`).
17. `waitlist` — Queued customer requests for booked-out categories with automated availability alerts.
18. `notifications` — In-app guest and staff messaging history.
19. `customer_favorites` — Bookmarked room categories for guest quick-booking.
20. `customer_preferences` — Saved guest preferences (bed type, floor preference, quiet room, dietary needs).
21. `pre_checkins` — Digital pre-arrival check-ins with submitted ID documents and arrival time.
22. `local_guide_places` — Curated local attractions, opening hours, estimated cost, and tips.
23. `trip_itineraries` — Guest trip planner containers.
24. `itinerary_items` — Planned daily activities linked to guide places.
25. `audit_logs` — Immutable compliance trail capturing user ID, role, action, target entity, and timestamp.

---

## 3. Financial Integrity & Concurrency Controls
- **Monetary Types**: All monetary columns (`base_price`, `room_rate`, `total_amount`, `paid_amount`, `balance_due`, `unit_cost`) use `DECIMAL(10,2)`.
- **Double-Booking Prevention**: When creating a reservation, the system checks for any conflicting active reservation with dates overlapping `[checkIn, checkOut)`. Overlap condition:
  ```sql
  WHERE res.room_id = :roomId
    AND res.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
    AND res.check_in_date < :newCheckOut
    AND res.check_out_date > :newCheckIn
  ```
- **Transaction Safety**: Multi-step operations (booking confirmation + invoice generation, or payment recording + balance update) run inside explicit JDBC transactions (`conn.setAutoCommit(false)` with `conn.rollback()` on error).
- **Negative Stock Prevention**: Inventory deductions use `SELECT ... FOR UPDATE` with Java and database safeguards to reject any transaction that would push stock levels below zero.
