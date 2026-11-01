# GrandStay Hotel Management System

An enterprise-grade, modern desktop Hotel Management System built from scratch in **Java 17**, **Java Swing (FlatLaf)**, **JDBC**, **MySQL 8.x**, and **BCrypt**.

Developed for academic presentation, professional operations, and complete role-based property management.

---

## 1. Project Overview & Objectives
GrandStay Hotel Management System manages end-to-end hotel workflows:
- **Zero Double-Bookings**: Guaranteed non-overlapping reservation engine.
- **Double-Entry Financial Safety**: Strict `BigDecimal` arithmetic for room charges, taxes, service fees, discounts, partial payments, and refund balances.
- **Modern UI Design System**: Built with FlatLaf Swing featuring Deep Navy (`#0F172A`), Pure White/Slate cards, and Restrained Gold (`#D4AF37`) accents.
- **Role-Based Access Control (RBAC)**: Custom workspaces for Customer, Receptionist, Housekeeper, and Administrator.
- **Security & Integrity**: 12-round BCrypt salt hashing for credentials; sensitive data and connection parameters separated into `.env` / `db.properties`.

---

## 2. Implemented Features by Phase

### Phase 1 — Core Working System
- **Authentication & Registration**: Login with email/username and password, self-service customer registration, role-based navigation.
- **Customer Dashboard**: Live room search by dates, capacity, and amenities; real-time cost estimation; 1-click booking confirmation; reservation cancellation; itemized HTML folio preview.
- **Pricing & Billing**: Nightly rate calculations, 10% tax rate, $15.00 resort fee, itemized invoices, payment recording (Cash, Card, Transfer), and balance tracking.
- **Receptionist (Front Desk) Portal**: Today's arrivals and departures queues; 1-click fast Check-In and Check-Out; walk-in reservations; room reassignments; visual floor-by-floor Room Status Matrix.
- **Housekeeping Management**: Turnover task queue (*Pending* ➔ *In Progress* ➔ *Completed* ➔ *Inspected*); automatic room readiness synchronization (clean & inspected rooms automatically become available for check-in).
- **Maintenance Defect Tracking**: Defect tickets, priority ratings (Low to Critical), technician assignments, room lockout toggles, and availability restoration upon repair.
- **Inventory Control**: Supply catalog (bedsheets, bath towels, shampoo, soap bars, coffee pods), atomic stock in/out transactions, negative balance prevention, and low-stock threshold alerts.
- **Executive Administrator Portal**: KPI statistics cards (Total Rooms, Available, Occupied, Cleaning, Check-ins, Revenue); CRUD room categories and rooms; staff user management with account enable/suspend and password resets; business intelligence reports with 1-click CSV export.

### Phase 2 — Advanced Guest Experience
- **Digital Pre-Check-in**: Guests submit arrival times and ID document verification in advance; front desk review and approval queue.
- **In-Room Service Requests**: On-demand requests for extra towels, room cleaning, room service, or amenities with real-time status updates.
- **Customer Preferences & Suggestions**: Saved bedding, floor, and dietary preferences; tailored room suggestions based on guest preferences and live availability.
- **Ratings & Reviews**: 1-to-5 star ratings and written reviews after completed stays; administrative moderation workflow (Approve, Reject, Hide).
- **In-App Notification Center**: Instant notifications for booking confirmations, cancellations, payment receipts, pre-check-in approvals, and service order progress.
- **Local Guide & Trip Planner**: Curated city attractions, opening hours, estimated duration, and costs; custom daily itinerary planner.
- **Waitlist Management**: Real-time waitlist for sold-out room categories with automated availability notifications.
- **Security Audit Trail**: Chronological activity logging capturing user ID, role, action, target entity, and timestamp with CSV export.

---

## 3. Software Requirements
- **JDK**: Java 17 or higher (tested with Oracle OpenJDK 26 / 17)
- **Build System**: Apache Maven 3.9+
- **Database Server**: MySQL 8.0 or 8.x (running locally or remotely)
- **Operating System**: Windows 10 / 11, macOS, or Linux

---

## 4. Installation & Setup Instructions

### Step 1: Clone or Open Workspace
Open the project directory in VS Code or terminal:
```powershell
cd d:\jumna
```

### Step 2: Configure MySQL Database
1. Ensure your MySQL 8.x server is running (e.g. `MYSQL80` Windows service).
2. Open MySQL Workbench or run PowerShell:
```powershell
# Using the provided PowerShell database setup script:
.\setup_db.ps1 -User "root" -Password "YOUR_PASSWORD"
```
Or execute SQL scripts manually via MySQL CLI:
```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

### Step 3: Configure Database Credentials
Create or edit `.env` (or `db.properties` in project root):
```properties
DB_HOST=localhost
DB_PORT=3306
DB_NAME=hotel_management
DB_USER=root
DB_PASSWORD=root
```
*(Alternatively, you can configure and test your MySQL credentials directly from the Login screen using the "⚙ Database Connection Settings" button!)*

---

## 5. Compiling and Running the Application

### Compile the Application:
```powershell
mvn -o compile
```

### Run All Unit Tests:
```powershell
.\test.ps1
```
*Result: 15 / 15 unit tests executed and passed.*

### Package the Executable JAR:
```powershell
& 'C:\Program Files\Java\jdk-26.0.2\bin\jar.exe' --create --file target/GrandStayHotelManagement.jar --main-class hotel.Main -C target/classes .
```

### Launch the Application:
```powershell
.\run.ps1
```
Or run directly with Java:
```powershell
$m2 = "$env:USERPROFILE\.m2\repository"
$jars = @(
    "target\GrandStayHotelManagement.jar",
    "$m2\com\mysql\mysql-connector-j\8.3.0\mysql-connector-j-8.3.0.jar",
    "$m2\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar",
    "$m2\com\formdev\flatlaf\3.4.1\flatlaf-3.4.1.jar",
    "$m2\com\formdev\flatlaf-extras\3.4.1\flatlaf-extras-3.4.1.jar",
    "$m2\org\slf4j\slf4j-api\2.0.12\slf4j-api-2.0.12.jar"
)
$cp = $jars -join ";"
java -cp $cp hotel.Main
```

---

## 6. Demonstration Accounts
Pre-configured demo accounts in `database/seed.sql` (passwords hashed with 12-round BCrypt):

| Role | Username | Password | Quick-Fill Button |
| :--- | :--- | :--- | :--- |
| **System Administrator** | `admin` | `Admin@123` | Click "Admin" |
| **Front Desk Receptionist** | `receptionist` | `Reception@123` | Click "Reception" |
| **Housekeeping Staff** | `housekeeper` | `Housekeeping@123` | Click "Housekeeper" |
| **Registered Customer** | `johndoe` | `Customer@123` | Click "Customer" |

---

## 7. Project Structure
```
GrandStayHotelManagementSystem/
├── pom.xml                               # Maven project configuration
├── README.md                             # Project setup & run documentation
├── .gitignore                            # Standard git ignore rules
├── .env.example                          # Environment template
├── run.ps1                               # PowerShell application launcher
├── test.ps1                              # PowerShell unit test runner
├── setup_db.ps1                          # MySQL automated database setup script
├── database/
│   ├── schema.sql                        # 25 normalized tables with constraints & indexes
│   ├── seed.sql                          # Seed data with BCrypt-hashed demo accounts
│   └── reset_database.sql                # Drop and recreate database cleanly
├── docs/
│   ├── PROJECT_OVERVIEW.md               # Technical overview & architecture
│   ├── DATABASE_DESIGN.md                # Entity relationship documentation
│   ├── USER_GUIDE.md                     # Step-by-step role guides
│   ├── TESTING.md                        # Unit testing strategy & results
│   └── IMPLEMENTATION_STATUS.md          # Verification status across phases
├── src/
│   ├── main/java/hotel/
│   │   ├── Main.java                     # Application entry point
│   │   ├── config/AppConfig.java         # .env and db.properties loader
│   │   ├── database/                     # JDBC connection & auto-initializer
│   │   ├── model/                        # 20+ Domain models and status enums
│   │   ├── dao/                          # JDBC Data Access Objects with transactions
│   │   ├── service/                      # Business services (Reservation, Billing, etc.)
│   │   ├── validation/                   # Input & capacity validators
│   │   ├── util/                         # PasswordUtil, DateUtil, CurrencyUtil, CsvExporter
│   │   └── ui/
│   │       ├── common/                   # UITheme, HeaderPanel, StatCard
│   │       ├── auth/                     # LoginFrame, RegisterDialog
│   │       ├── customer/                 # CustomerDashboardFrame
│   │       └── staff/                    # Receptionist, Housekeeper, Admin Dashboards
│   └── test/java/hotel/                  # JUnit 5 test suites & TestRunner
```

---

## 8. Known Limitations & Notes
- **Email & SMS**: Notifications are delivered in-app via the Notification Center (`notifications` table). To send external emails or SMS in a production environment, plug in an SMTP or Twilio provider.
- **Card Payment Gateway**: Payment transactions record payment references and settlement details into the ledger. For live card processing, connect Stripe or PayPal SDKs to the `BillingService`.
