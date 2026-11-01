-- =====================================================================
-- GrandStay Hotel Management System - Reset Database Script
-- Drops and recreates the hotel_management database cleanly
-- =====================================================================

DROP DATABASE IF EXISTS hotel_management;
CREATE DATABASE hotel_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hotel_management;

-- Next step: Execute database/schema.sql and database/seed.sql
