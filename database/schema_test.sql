-- =====================================================================
-- GrandStay Hotel Management System - Database Schema
-- Compatible with MySQL 8.x
-- Character Set: utf8mb4, Engine: InnoDB
-- =====================================================================

CREATE DATABASE IF NOT EXISTS hotel_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hotel_management_test;

-- Drop foreign keys safe order
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Guests
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS itinerary_items;
DROP TABLE IF EXISTS trip_itineraries;
DROP TABLE IF EXISTS local_guide_places;
DROP TABLE IF EXISTS pre_checkins;
DROP TABLE IF EXISTS customer_preferences;
DROP TABLE IF EXISTS customer_favorites;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS waitlist;
DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS service_orders;
DROP TABLE IF EXISTS inventory_transactions;
DROP TABLE IF EXISTS inventory_items;
DROP TABLE IF EXISTS maintenance_tickets;
DROP TABLE IF EXISTS housekeeping_tasks;
DROP TABLE IF EXISTS refunds;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS invoice_items;
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS reservation_guests;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS room_types;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS guests;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- TABLE: guests
-- ---------------------------------------------------------------------
CREATE TABLE guests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    address VARCHAR(255) NULL,
    id_type VARCHAR(50) NULL,
    id_number VARCHAR(50) NULL,
    nationality VARCHAR(50) NULL,
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_guests_email (email),
    INDEX idx_guests_phone (phone),
    INDEX idx_guests_name (last_name, first_name)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: users
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    guest_id BIGINT NULL,
    username VARCHAR(80) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NULL,
    role ENUM('CUSTOMER', 'RECEPTIONIST', 'ADMIN', 'HOUSEKEEPING') NOT NULL DEFAULT 'CUSTOMER',
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE SET NULL,
    INDEX idx_users_role (role),
    INDEX idx_users_status (status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: room_types
-- ---------------------------------------------------------------------
CREATE TABLE room_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    max_capacity INT NOT NULL DEFAULT 2,
    bed_type VARCHAR(50) NOT NULL,
    amenities TEXT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: rooms
-- ---------------------------------------------------------------------
CREATE TABLE rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(20) NOT NULL UNIQUE,
    room_type_id BIGINT NOT NULL,
    floor_number INT NOT NULL,
    status ENUM('AVAILABLE', 'RESERVED', 'OCCUPIED', 'DIRTY', 'CLEANING', 'INSPECTED', 'MAINTENANCE', 'OUT_OF_SERVICE') NOT NULL DEFAULT 'AVAILABLE',
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE RESTRICT,
    INDEX idx_rooms_status (status),
    INDEX idx_rooms_floor (floor_number)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: reservations
-- ---------------------------------------------------------------------
CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference_number VARCHAR(30) NOT NULL UNIQUE,
    guest_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    room_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    number_of_guests INT NOT NULL DEFAULT 1,
    nights INT NOT NULL,
    room_rate DECIMAL(10,2) NOT NULL,
    total_room_charge DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    service_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status ENUM('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED', 'NO_SHOW') NOT NULL DEFAULT 'CONFIRMED',
    cancellation_reason TEXT NULL,
    cancelled_at TIMESTAMP NULL,
    special_requests TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT,
    INDEX idx_res_dates (check_in_date, check_out_date),
    INDEX idx_res_status (status),
    INDEX idx_res_room (room_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: reservation_guests
-- ---------------------------------------------------------------------
CREATE TABLE reservation_guests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NULL,
    phone VARCHAR(30) NULL,
    id_number VARCHAR(50) NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: invoices
-- ---------------------------------------------------------------------
CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(30) NOT NULL UNIQUE,
    reservation_id BIGINT NOT NULL,
    guest_id BIGINT NOT NULL,
    issue_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    paid_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    balance_due DECIMAL(10,2) NOT NULL,
    status ENUM('ISSUED', 'PARTIALLY_PAID', 'PAID', 'CANCELLED') NOT NULL DEFAULT 'ISSUED',
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE RESTRICT,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE RESTRICT,
    INDEX idx_inv_status (status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: invoice_items
-- ---------------------------------------------------------------------
CREATE TABLE invoice_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    item_type VARCHAR(50) NOT NULL DEFAULT 'ROOM',
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: payments
-- ---------------------------------------------------------------------
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_reference VARCHAR(30) NOT NULL UNIQUE,
    reservation_id BIGINT NOT NULL,
    invoice_id BIGINT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'BANK_TRANSFER', 'ONLINE') NOT NULL,
    payment_status ENUM('COMPLETED', 'PENDING', 'FAILED', 'REFUNDED', 'PARTIALLY_REFUNDED') NOT NULL DEFAULT 'COMPLETED',
    transaction_notes TEXT NULL,
    processed_by_user_id BIGINT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE RESTRICT,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE SET NULL,
    FOREIGN KEY (processed_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_pay_res (reservation_id),
    INDEX idx_pay_date (payment_date)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: refunds
-- ---------------------------------------------------------------------
CREATE TABLE refunds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    refund_reference VARCHAR(30) NOT NULL UNIQUE,
    payment_id BIGINT NULL,
    reservation_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    reason TEXT NOT NULL,
    status ENUM('PENDING', 'PROCESSED', 'REJECTED') NOT NULL DEFAULT 'PROCESSED',
    processed_by_user_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE SET NULL,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE RESTRICT,
    FOREIGN KEY (processed_by_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: housekeeping_tasks
-- ---------------------------------------------------------------------
CREATE TABLE housekeeping_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    assigned_to_user_id BIGINT NULL,
    task_type VARCHAR(50) NOT NULL DEFAULT 'DAILY_CLEANING',
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'URGENT') NOT NULL DEFAULT 'MEDIUM',
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'INSPECTED') NOT NULL DEFAULT 'PENDING',
    notes TEXT NULL,
    inspection_notes TEXT NULL,
    inspected_by_user_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    inspected_at TIMESTAMP NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT,
    FOREIGN KEY (assigned_to_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (inspected_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_hk_status (status),
    INDEX idx_hk_room (room_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: maintenance_tickets
-- ---------------------------------------------------------------------
CREATE TABLE maintenance_tickets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ticket_number VARCHAR(30) NOT NULL UNIQUE,
    room_id BIGINT NOT NULL,
    reported_by_user_id BIGINT NULL,
    assigned_to_user_id BIGINT NULL,
    issue_description TEXT NOT NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'MEDIUM',
    status ENUM('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    resolution_notes TEXT NULL,
    room_made_unavailable BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT,
    FOREIGN KEY (reported_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (assigned_to_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_maint_status (status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: inventory_items
-- ---------------------------------------------------------------------
CREATE TABLE inventory_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_code VARCHAR(30) NOT NULL UNIQUE,
    item_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    quantity_on_hand INT NOT NULL DEFAULT 0,
    min_stock_level INT NOT NULL DEFAULT 5,
    unit_of_measure VARCHAR(20) NOT NULL DEFAULT 'PCS',
    unit_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    location VARCHAR(100) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_inv_code (item_code),
    INDEX idx_inv_category (category)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: inventory_transactions
-- ---------------------------------------------------------------------
CREATE TABLE inventory_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    transaction_type ENUM('STOCK_IN', 'STOCK_OUT', 'ADJUSTMENT', 'ROOM_RESTOCK') NOT NULL,
    quantity INT NOT NULL,
    balance_after INT NOT NULL,
    reference_notes TEXT NULL,
    performed_by_user_id BIGINT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES inventory_items(id) ON DELETE RESTRICT,
    FOREIGN KEY (performed_by_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: service_orders
-- ---------------------------------------------------------------------
CREATE TABLE service_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(30) NOT NULL UNIQUE,
    reservation_id BIGINT NOT NULL,
    guest_id BIGINT NOT NULL,
    category ENUM('ROOM_SERVICE', 'EXTRA_TOWELS', 'ROOM_CLEANING', 'MAINTENANCE_ASSISTANCE', 'EXTRA_AMENITIES', 'GENERAL_ASSISTANCE') NOT NULL,
    description TEXT NOT NULL,
    special_instructions TEXT NULL,
    status ENUM('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    assigned_to_user_id BIGINT NULL,
    charge_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE RESTRICT,
    FOREIGN KEY (assigned_to_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_svc_status (status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: feedback
-- ---------------------------------------------------------------------
CREATE TABLE feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL UNIQUE,
    guest_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review_title VARCHAR(150) NOT NULL,
    review_text TEXT NOT NULL,
    moderation_status ENUM('PENDING', 'APPROVED', 'REJECTED', 'HIDDEN') NOT NULL DEFAULT 'PENDING',
    moderated_by_user_id BIGINT NULL,
    moderated_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (moderated_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_fb_mod (moderation_status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: waitlist
-- ---------------------------------------------------------------------
CREATE TABLE waitlist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    guest_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    desired_check_in DATE NOT NULL,
    desired_check_out DATE NOT NULL,
    number_of_guests INT NOT NULL DEFAULT 1,
    status ENUM('ACTIVE', 'NOTIFIED', 'CONVERTED', 'CANCELLED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notified_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE RESTRICT,
    FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE RESTRICT,
    INDEX idx_wl_status (status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: notifications
-- ---------------------------------------------------------------------
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    reference_id VARCHAR(50) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notif_user (user_id, is_read)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: customer_favorites
-- ---------------------------------------------------------------------
CREATE TABLE customer_favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    room_type_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_user_room_type (user_id, room_type_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: customer_preferences
-- ---------------------------------------------------------------------
CREATE TABLE customer_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    preferred_floor VARCHAR(20) NULL,
    preferred_bed_type VARCHAR(30) NULL,
    quiet_room BOOLEAN NOT NULL DEFAULT FALSE,
    high_floor BOOLEAN NOT NULL DEFAULT FALSE,
    near_elevator BOOLEAN NOT NULL DEFAULT FALSE,
    dietary_needs TEXT NULL,
    notes TEXT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: pre_checkins
-- ---------------------------------------------------------------------
CREATE TABLE pre_checkins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expected_arrival_time VARCHAR(20) NOT NULL,
    id_document_type VARCHAR(50) NOT NULL,
    id_document_number VARCHAR(50) NOT NULL,
    special_requests TEXT NULL,
    emergency_contact_name VARCHAR(100) NULL,
    emergency_contact_phone VARCHAR(50) NULL,
    status ENUM('SUBMITTED', 'APPROVED', 'REJECTED', 'NEEDS_UPDATE') NOT NULL DEFAULT 'SUBMITTED',
    staff_notes TEXT NULL,
    reviewed_by_user_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP NULL,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_precheckin_status (status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: local_guide_places
-- ---------------------------------------------------------------------
CREATE TABLE local_guide_places (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    address VARCHAR(255) NOT NULL,
    opening_hours VARCHAR(100) NULL,
    estimated_duration_minutes INT NOT NULL DEFAULT 60,
    estimated_cost DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    recommended_tips TEXT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: trip_itineraries
-- ---------------------------------------------------------------------
CREATE TABLE trip_itineraries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: itinerary_items
-- ---------------------------------------------------------------------
CREATE TABLE itinerary_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    itinerary_id BIGINT NOT NULL,
    place_id BIGINT NULL,
    activity_name VARCHAR(150) NOT NULL,
    planned_date DATE NOT NULL,
    planned_time VARCHAR(20) NULL,
    sequence_order INT NOT NULL DEFAULT 1,
    notes TEXT NULL,
    FOREIGN KEY (itinerary_id) REFERENCES trip_itineraries(id) ON DELETE CASCADE,
    FOREIGN KEY (place_id) REFERENCES local_guide_places(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- TABLE: audit_logs
-- ---------------------------------------------------------------------
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    username VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NULL,
    details TEXT NULL,
    ip_address VARCHAR(50) NULL DEFAULT '127.0.0.1',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_time (timestamp),
    INDEX idx_audit_user (user_id)
) ENGINE=InnoDB;

