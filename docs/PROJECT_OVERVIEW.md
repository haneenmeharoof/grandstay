# GrandStay Hotel Management System — Project Overview

## 1. Executive Summary
**GrandStay Hotel Management System** is a full-featured, enterprise-grade desktop hotel management application built in **Java 17** using **Java Swing**, **JDBC**, **MySQL 8.x**, and **BCrypt**. Designed for modern hotel operations and educational demonstration, it provides an intuitive, high-performance solution that spans guest reservations, front-desk check-in/check-out, billing, housekeeping turnover, defect maintenance, inventory tracking, and executive analytics.

---

## 2. Technology Stack & Architecture
- **Language**: Java 17 (compiled targeting release 17)
- **GUI Framework**: Java Swing with FlatLaf modern UI theme (Deep Navy, Pure White, Restrained Gold accents)
- **Database**: MySQL 8.x (InnoDB engine, utf8mb4 collation)
- **Persistence Layer**: Native JDBC with `java.sql.PreparedStatement` and database transaction rollbacks
- **Security**: 12-round BCrypt salt hashing (`org.mindrot.jbcrypt`)
- **Testing**: JUnit 5 Suite (`org.junit.jupiter`)
- **Build System**: Apache Maven 3.9+
- **Financial Precision**: 100% `BigDecimal` calculations (zero double/float rounding errors)

### Layered Architecture
```
┌─────────────────────────────────────────────────────────┐
│                  Presentation Layer                     │
│  (FlatLaf Swing, Cards, Dashboards, Tables, Dialogs)   │
└────────────────────────────┬────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────┐
│                    Service Layer                        │
│ (Business Rules, Cost Estimation, State Transitions)    │
└────────────────────────────┬────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────┐
│                  Data Access (DAOs)                     │
│   (PreparedStatements, Connection Management, SQL)      │
└────────────────────────────┬────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────┐
│                 MySQL 8.x Database                      │
│     (25 Normalized Tables, Foreign Keys, Indexes)       │
└─────────────────────────────────────────────────────────┘
```

---

## 3. Role-Based Access Control (RBAC)
The application defines four distinct user roles, each routed directly to their specialized workspace:

1. **Customer / Guest**:
   - Live room search with arrival/departure dates, guest count, and amenity filters.
   - Cost breakdown calculation (room charges + tax + resort fee).
   - Instant reservation booking and booking cancellation.
   - Folio and invoice viewer with HTML itemized layout.
   - Digital Pre-Check-in (arrival time, ID document verification).
   - In-room service orders (extra towels, room service, cleaning, maintenance).
   - Guest preferences (bedding, floor, dietary needs) and tailored room recommendations.
   - Ratings and written reviews after completed stays.
   - Priority waitlist subscription.
   - In-app notification center.

2. **Receptionist (Front Desk)**:
   - Today's arrivals and departures real-time queues.
   - Instant 1-click Check-In and Check-Out.
   - Reservation lookup by guest name, booking ref, or room number.
   - Walk-in guest registration and room assignment.
   - Folio billing, payment recording (Cash, Credit Card, Bank Transfer), and itemized invoice generation.
   - Pre-check-in approval and rejection queue.
   - Live Room Status Matrix (visual color-coded floor grid).

3. **Housekeeping Staff**:
   - Assigned turnover and daily cleaning task queue.
   - Status updates: *Pending* ➔ *In Progress* ➔ *Completed* ➔ *Inspected*.
   - Room readiness synchronization: rooms automatically mark *Available* upon inspection.
   - Linen and toiletries supply tracking.

4. **Hotel Administrator**:
   - Executive KPI Dashboard (Total Rooms, Available, Occupied, Dirty, Revenue, Check-ins).
   - Room and Room Category management (rates, capacity, bed configuration, amenities).
   - Staff account administration (create user, assign role, reset password, enable/suspend account).
   - Maintenance defect management (log ticket, assign technician, lockout room from booking).
   - Inventory management (catalog items, restock, consumption log, minimum threshold alerts).
   - Guest review moderation (approve public display, reject, hide).
   - Business intelligence reports (Occupancy %, Revenue, Balances) with CSV export.
   - Comprehensive Security Audit Trail with chronological user action logs.
