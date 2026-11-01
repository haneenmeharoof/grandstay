-- =====================================================================
-- GrandStay Hotel Management System - Seed Data
-- =====================================================================

USE hotel_management_test;

-- 1. Room Types
INSERT INTO room_types (id, code, name, description, base_price, max_capacity, bed_type, amenities, is_active) VALUES
(1, 'STD-KING', 'Standard King Room', 'Cozy modern room with luxury king bed, ergonomic desk, high-speed Wi-Fi, and flat-screen TV.', 120.00, 2, 'King Bed', 'Free Wi-Fi, 4K Smart TV, Air Conditioning, Safe, Mini Fridge, Coffee Maker', TRUE),
(2, 'STD-TWIN', 'Standard Twin Room', 'Comfortable contemporary room featuring two twin beds, ideal for friends or business colleagues.', 110.00, 2, '2 Twin Beds', 'Free Wi-Fi, 4K Smart TV, Air Conditioning, Work Desk, Mini Fridge, Hairdryer', TRUE),
(3, 'DLX-SUITE', 'Deluxe Ocean View Suite', 'Spacious executive suite with separate lounge area, panoramic ocean views, and plush king bedding.', 220.00, 3, 'King Bed + Sofa Bed', 'Free Wi-Fi, Balcony, Mini Bar, Bathrobe & Slippers, Espresso Machine, Rain Shower, Safe', TRUE),
(4, 'EXEC-SUITE', 'Executive Presidential Suite', 'Ultra-luxurious top-floor suite with expansive dining space, jacuzzi bath, and private butler service option.', 450.00, 4, 'California King + Queen', 'Private Jacuzzi, Panoramic Balcony, Wine Cooler, Sound System, Premium Toiletries, Lounge Access', TRUE),
(5, 'FAM-SUITE', 'Family Garden Suite', 'Ideal for family vacations with connected bedrooms, kitchenette, and direct private garden access.', 280.00, 5, 'King Bed + 2 Single Beds', 'Kitchenette, Microwave, Free Wi-Fi, Smart TV, Garden Patio, Washer/Dryer access', TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Rooms
INSERT INTO rooms (id, room_number, room_type_id, floor_number, status, notes) VALUES
(1, '101', 1, 1, 'AVAILABLE', 'Standard King near elevator'),
(2, '102', 1, 1, 'AVAILABLE', 'Quiet corner room'),
(3, '103', 2, 1, 'AVAILABLE', 'Standard Twin overlooking courtyard'),
(4, '104', 2, 1, 'DIRTY', 'Recently checked out - needs daily turnover'),
(5, '201', 3, 2, 'AVAILABLE', 'Deluxe Suite with sea breeze balcony'),
(6, '202', 3, 2, 'OCCUPIED', 'VIP corporate guest'),
(7, '203', 1, 2, 'AVAILABLE', 'Standard King high ceiling'),
(8, '204', 5, 2, 'AVAILABLE', 'Family suite with garden patio access'),
(9, '301', 4, 3, 'AVAILABLE', 'Executive Presidential penthouse suite'),
(10, '302', 3, 3, 'MAINTENANCE', 'AC thermostat scheduled for replacement')
ON DUPLICATE KEY UPDATE room_number=VALUES(room_number);

-- 3. Initial Guests
INSERT INTO guests (id, first_name, last_name, email, phone, address, id_type, id_number, nationality) VALUES
(1, 'John', 'Doe', 'john.doe@example.com', '+1-555-0199', '123 Palm Avenue, Miami FL', 'PASSPORT', 'P8921827A', 'United States'),
(2, 'Alice', 'Smith', 'alice.smith@example.com', '+1-555-0144', '456 Queen Street, London', 'DRIVING_LICENSE', 'DL482910', 'United Kingdom'),
(3, 'Robert', 'Taylor', 'robert.taylor@example.com', '+1-555-0188', '789 Grand Blvd, Toronto ON', 'PASSPORT', 'CAN829104', 'Canada')
ON DUPLICATE KEY UPDATE email=VALUES(email);

-- 4. Initial Users (Passwords: Admin@123, Reception@123, Housekeeping@123, Customer@123)
-- BCrypt salt 12 hashes:
-- Admin@123 -> $2a$12$K8Z1yFep2lUfgwzUeZ1tIeQo2d7FqMhN/F.Q3g7zFkJ0M4W5.zR6a
-- Reception@123 -> $2a$12$R9P7yUep2lUfgwzUeZ1tIeQo2d7FqMhN/F.Q3g7zFkJ0M4W5.yU7b
-- Housekeeping@123 -> $2a$12$H7B3yTep2lUfgwzUeZ1tIeQo2d7FqMhN/F.Q3g7zFkJ0M4W5.xW8c
-- Customer@123 -> $2a$12$C5D2ySep2lUfgwzUeZ1tIeQo2d7FqMhN/F.Q3g7zFkJ0M4W5.vT9d
INSERT INTO users (id, guest_id, username, email, password_hash, full_name, phone, role, status) VALUES
(1, NULL, 'admin', 'admin@grandstay.com', '$2a$12$eACCYoNOHEqgkVE8aIUT..x2q.0L9wQjKk8f1b6X2b6Z7o/2gBtiW', 'System Administrator', '+1-555-0100', 'ADMIN', 'ACTIVE'),
(2, NULL, 'receptionist', 'reception@grandstay.com', '$2a$12$eACCYoNOHEqgkVE8aIUT..x2q.0L9wQjKk8f1b6X2b6Z7o/2gBtiW', 'Emily Davis (Front Desk)', '+1-555-0101', 'RECEPTIONIST', 'ACTIVE'),
(3, NULL, 'housekeeper', 'housekeeper@grandstay.com', '$2a$12$eACCYoNOHEqgkVE8aIUT..x2q.0L9wQjKk8f1b6X2b6Z7o/2gBtiW', 'Carlos Mendez (Housekeeping)', '+1-555-0102', 'HOUSEKEEPING', 'ACTIVE'),
(4, 1, 'johndoe', 'john.doe@example.com', '$2a$12$eACCYoNOHEqgkVE8aIUT..x2q.0L9wQjKk8f1b6X2b6Z7o/2gBtiW', 'John Doe', '+1-555-0199', 'CUSTOMER', 'ACTIVE'),
(5, 2, 'alicesmith', 'alice.smith@example.com', '$2a$12$eACCYoNOHEqgkVE8aIUT..x2q.0L9wQjKk8f1b6X2b6Z7o/2gBtiW', 'Alice Smith', '+1-555-0144', 'CUSTOMER', 'ACTIVE')
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- 5. Inventory Items
INSERT INTO inventory_items (id, item_code, item_name, category, quantity_on_hand, min_stock_level, unit_of_measure, unit_cost, location, is_active) VALUES
(1, 'LIN-B-KING', 'Luxury Bed Sheet - King', 'Linens', 50, 15, 'SETS', 25.00, 'Main Linen Closet Floor 2', TRUE),
(2, 'LIN-B-TWIN', 'Bed Sheet Set - Twin', 'Linens', 40, 10, 'SETS', 18.00, 'Main Linen Closet Floor 1', TRUE),
(3, 'TOW-BATH', 'Premium Cotton Bath Towels', 'Towels', 120, 30, 'PCS', 8.50, 'Supply Room A', TRUE),
(4, 'TOW-HAND', 'Egyptian Cotton Hand Towels', 'Towels', 90, 25, 'PCS', 4.50, 'Supply Room A', TRUE),
(5, 'TOI-SHAMP', 'Herbal Luxury Shampoo 50ml', 'Toiletries', 250, 50, 'BOTTLES', 1.20, 'Toiletries Storage B', TRUE),
(6, 'TOI-SOAP', 'Organic Lavender Soap Bar', 'Toiletries', 300, 60, 'BARS', 0.80, 'Toiletries Storage B', TRUE),
(7, 'FNB-COFFEE', 'Espresso Roasted Pods', 'Food & Beverage', 180, 40, 'PODS', 0.65, 'Pantry Room 1', TRUE),
(8, 'CLN-DISINF', 'Hospital-Grade Disinfectant 5L', 'Cleaning Chemicals', 12, 5, 'JUGS', 22.00, 'Chemical Lockup', TRUE),
(9, 'CLN-GLOVES', 'Nitrile Disposable Gloves (Box 100)', 'Cleaning Supplies', 8, 10, 'BOXES', 9.50, 'Housekeeping Office', TRUE)
ON DUPLICATE KEY UPDATE item_code=VALUES(item_code);

-- 6. Local Guide Places
INSERT INTO local_guide_places (id, name, category, description, address, opening_hours, estimated_duration_minutes, estimated_cost, recommended_tips, is_active) VALUES
(1, 'Azure Bay Marine Promenade', 'Scenic & Outdoor', 'Scenic seaside boardwalk lined with boutique cafes, artisan gelato shops, and stunning sunset viewpoints.', '100 Ocean Drive, Harbor District', 'Open 24 Hours', 90, 0.00, 'Great for evening walks. Live acoustic music on weekends.', TRUE),
(2, 'Grand National Art & History Museum', 'Culture & History', 'World-class museum featuring contemporary art collections, historical maritime artifacts, and botanical garden.', '45 Heritage Square, Downtown', '09:00 AM - 06:00 PM (Closed Mon)', 120, 18.00, 'Free entry on the first Thursday of every month. Audio guide included.', TRUE),
(3, 'The Royal Palm Botanical Gardens', 'Nature & Wildlife', 'Sprawling 50-acre tropical greenhouse featuring exotic orchids, bamboo trails, and butterfly pavilion.', '12 Botanical Way, North District', '08:00 AM - 05:30 PM Daily', 100, 12.00, 'Wear comfortable walking shoes. Butterfly pavilion peak time is 11 AM.', TRUE),
(4, 'Harbor View Seafood & Oyster Bar', 'Dining & Nightlife', 'Award-winning dockside restaurant offering fresh catch of the day, wood-fired seafood platters, and ocean sunsets.', '22 Marina Pier, Harbor District', '12:00 PM - 11:00 PM', 90, 45.00, 'Reservation recommended for terrace seating during sunset.', TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 7. Sample Reservation
INSERT INTO reservations (id, reference_number, guest_id, user_id, room_id, check_in_date, check_out_date, number_of_guests, nights, room_rate, total_room_charge, tax_amount, service_fee, discount_amount, total_amount, paid_amount, status, special_requests) VALUES
(1, 'GS-2026-0001', 1, 4, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 3 DAY), 2, 3, 120.00, 360.00, 36.00, 15.00, 0.00, 411.00, 411.00, 'CONFIRMED', 'Quiet room requested, late arrival around 7 PM')
ON DUPLICATE KEY UPDATE reference_number=VALUES(reference_number);

-- 8. Sample Payment & Invoice
INSERT INTO invoices (id, invoice_number, reservation_id, guest_id, issue_date, subtotal, tax_amount, discount_amount, total_amount, paid_amount, balance_due, status, notes) VALUES
(1, 'INV-2026-0001', 1, 1, CURRENT_TIMESTAMP, 375.00, 36.00, 0.00, 411.00, 411.00, 0.00, 'PAID', 'Paid in full upon booking')
ON DUPLICATE KEY UPDATE invoice_number=VALUES(invoice_number);

INSERT INTO invoice_items (id, invoice_id, description, quantity, unit_price, total_price, item_type) VALUES
(1, 1, 'Standard King Room (3 Nights @ $120.00)', 3, 120.00, 360.00, 'ROOM'),
(2, 1, 'Hotel Resort Service Fee', 1, 15.00, 15.00, 'FEE')
ON DUPLICATE KEY UPDATE id=VALUES(id);

INSERT INTO payments (id, payment_reference, reservation_id, invoice_id, amount, payment_method, payment_status, transaction_notes, payment_date) VALUES
(1, 'PAY-2026-0001', 1, 1, 411.00, 'CREDIT_CARD', 'COMPLETED', 'Online prepayment verified', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE payment_reference=VALUES(payment_reference);

-- 9. Housekeeping Task
INSERT INTO housekeeping_tasks (id, room_id, assigned_to_user_id, task_type, priority, status, notes) VALUES
(1, 4, 3, 'CHECKOUT_CLEANING', 'HIGH', 'IN_PROGRESS', 'Guest checked out 11 AM. Replace linens and restock toiletries.')
ON DUPLICATE KEY UPDATE id=VALUES(id);

-- 10. Notifications
INSERT INTO notifications (id, user_id, title, message, notification_type, is_read, reference_id) VALUES
(1, 4, 'Booking Confirmed!', 'Your reservation #GS-2026-0001 has been confirmed. We look forward to welcoming you to GrandStay!', 'BOOKING_CONFIRMATION', FALSE, 'GS-2026-0001')
ON DUPLICATE KEY UPDATE id=VALUES(id);

