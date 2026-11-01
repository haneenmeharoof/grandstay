# GrandStay Hotel Management System — User Guide

## 1. Getting Started
Launch the application by running `.\run.ps1` (or `java -cp ... hotel.Main`). The initial screen presented is the **GrandStay Sign-In Window**.

### Demonstration Accounts
The system provides built-in quick-fill buttons for rapid college presentation:

| Role | Username | Password | Purpose |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `Admin@123` | Master control, staff accounts, rates, analytics |
| **Receptionist** | `receptionist` | `Reception@123` | Front desk, arrivals, departures, billing |
| **Housekeeper** | `housekeeper` | `Housekeeping@123` | Cleaning tasks, turnover sign-off, supplies |
| **Customer** | `johndoe` | `Customer@123` | Room booking, reservations, in-room services |

---

## 2. Customer Portal Walkthrough
1. **Search & Book Rooms**:
   - Enter your intended check-in and check-out dates (format `YYYY-MM-DD`).
   - Select the number of guests.
   - Click **Search Rooms** to see live available rooms.
   - Click on any room to preview the estimated cost breakdown.
   - Click **Book Selected Room**, provide optional special requests, and confirm.
2. **My Reservations**:
   - View active and past bookings.
   - Click **View Folio / Invoice** to see the itemized HTML invoice.
   - Click **Digital Pre-Check-in** to notify the front desk of your expected arrival time and upload ID details.
   - Click **Leave Review** after your stay to submit star ratings and comments.
   - Click **Cancel Booking** if your plans change.
3. **In-Room Services**:
   - Order extra towels, room cleaning, room service, or amenities during your stay.
   - Track progress from *Pending* to *Completed*.
4. **Local Guide & Trip Planner**:
   - Browse curated local attractions, parks, and restaurants with costs and hours.
   - Build a daily itinerary for your vacation.
5. **Waitlist**:
   - If a preferred suite is sold out, register your dates on the waitlist to receive an in-app notification when a room becomes available.

---

## 3. Front Desk (Receptionist) Walkthrough
1. **Today's Arrivals**:
   - See all guests scheduled to arrive today.
   - Click **Complete Guest Check-In** to assign the room and mark it as *Occupied*.
2. **Today's Departures**:
   - See departing guests.
   - Ensure the folio balance is settled ($0.00), then click **Process Guest Check-Out**.
   - The room automatically transitions to *Dirty* and dispatches a turnover task to Housekeeping.
3. **Walk-In Guests**:
   - In the **Reservations & Walk-in** tab, click **New Walk-in Reservation**.
   - Enter guest details, select an available room, and confirm.
4. **Billing & Folio**:
   - Select any reservation and click **Record Payment**.
   - Choose the payment method (Cash, Credit Card, Bank Transfer), enter the amount, and save.
   - The folio balance updates in real time.
5. **Pre-Check-in Queue**:
   - Review incoming pre-check-in submissions and approve or request updates.

---

## 4. Housekeeping Portal Walkthrough
1. **Cleaning Tasks**:
   - View assigned turnover and daily cleaning tasks.
   - When beginning a room turnover, select the task and click **Mark In-Progress** (sets room status to *Cleaning*).
   - Once cleaning is finished, click **Mark Completed**.
   - When supervisor inspection is complete, click **Inspected & Ready for Guests** (sets room status to *Available*).
2. **Supplies & Inventory**:
   - View current stock of bedsheets, towels, and toiletries.
   - Click **Record Used Supplies** to log linens and amenities consumed during turnovers.

---

## 5. Administrator Portal Walkthrough
1. **Executive Dashboard**:
   - Monitor real-time property KPIs: Total Rooms, Available, Occupied, Dirty, Check-ins, and Total Revenue.
2. **Rooms & Categories**:
   - Add new rooms, set floor numbers, update operational statuses, and configure room types with nightly rates and amenities.
3. **Staff & User Accounts**:
   - Create new staff accounts, assign roles, reset forgotten passwords, and enable or suspend access.
4. **Maintenance & Defect Tracking**:
   - Log maintenance tickets, assign technicians, and toggle the room lockout flag (prevents bookings while under repair).
   - Once resolved, availability is automatically restored.
5. **Supplies Catalog**:
   - Manage inventory items, restock supplies, and monitor low-stock warnings.
6. **Review Moderation**:
   - Review customer feedback. Approve positive reviews for public display or reject inappropriate submissions.
7. **Business Reports & CSV Export**:
   - Generate Occupancy Rate, Revenue, and Outstanding Balance reports with 1-click CSV export.
8. **Security Audit Trail**:
   - Inspect immutable security logs detailing actor username, role, action, target entity, and timestamp.
