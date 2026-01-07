-- Car Pooling Application Database Schema
-- PostgreSQL Database

-- Create Database
CREATE DATABASE carpolling_db;

\c carpolling_db;

-- Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,                -- Email is optional now
    phone VARCHAR(20) NOT NULL UNIQUE,        -- Phone is the primary login method
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('PASSENGER', 'DRIVER', 'BOTH', 'ADMIN')),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Drivers Table
CREATE TABLE drivers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    license_expiry_date VARCHAR(50) NOT NULL,
    experience_years INTEGER NOT NULL,
    additional_info TEXT,
    is_verified BOOLEAN NOT NULL DEFAULT false,
    rating DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_trips INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Vehicles Table
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    registration_number VARCHAR(50) NOT NULL UNIQUE,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    color VARCHAR(50) NOT NULL,
    manufacturing_year INTEGER NOT NULL,
    vehicle_type VARCHAR(50) NOT NULL CHECK (vehicle_type IN ('SEDAN', 'SUV', 'HATCHBACK', 'MUV', 'LUXURY')),
    total_seats INTEGER NOT NULL,
    passenger_seats INTEGER NOT NULL,
    has_ac BOOLEAN NOT NULL DEFAULT true,
    features TEXT,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
);

-- Routes Table
CREATE TABLE routes (
    id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    route_name VARCHAR(255) NOT NULL,
    total_distance DOUBLE PRECISION NOT NULL,
    estimated_duration INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
);

-- Route Points Table (with Hierarchical Location Support)
CREATE TABLE route_points (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    city VARCHAR(255) NOT NULL,                  -- Main city (e.g., "Bangalore")
    sub_location VARCHAR(255) NOT NULL,          -- Sub-location (e.g., "Electronic City")
    point_name VARCHAR(255) NOT NULL,            -- Combined: "Bangalore - Electronic City"
    address VARCHAR(500) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    sequence_order INTEGER NOT NULL,
    distance_from_start INTEGER NOT NULL,        -- in meters
    time_from_start INTEGER NOT NULL,            -- in minutes
    is_boarding_point BOOLEAN NOT NULL DEFAULT true,
    is_drop_point BOOLEAN NOT NULL DEFAULT true,
    FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE
);

-- Indexes for route points (optimized for hierarchical search)
CREATE INDEX idx_route_point_city ON route_points(city);
CREATE INDEX idx_route_point_city_sublocation ON route_points(city, sub_location);
CREATE INDEX idx_route_point_boarding ON route_points(is_boarding_point) WHERE is_boarding_point = true;
CREATE INDEX idx_route_point_drop ON route_points(is_drop_point) WHERE is_drop_point = true;

-- Trips Table
CREATE TABLE trips (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    estimated_arrival_time TIMESTAMP NOT NULL,
    base_price_per_km DOUBLE PRECISION NOT NULL,
    available_seats INTEGER NOT NULL,
    booked_seats INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED' CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    special_instructions TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
    FOREIGN KEY (driver_id) REFERENCES drivers(id) ON DELETE CASCADE
);

-- Create indexes for trips table for efficient searching
CREATE INDEX idx_trip_date ON trips(departure_time);
CREATE INDEX idx_trip_status ON trips(status);

-- Trip Seats Table
CREATE TABLE trip_seats (
    id BIGSERIAL PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT true,
    is_driver_seat BOOLEAN NOT NULL DEFAULT false,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    UNIQUE (trip_id, seat_number)
);

-- Bookings Table
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_reference VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    trip_id BIGINT NOT NULL,
    boarding_point_id BIGINT NOT NULL,
    drop_point_id BIGINT NOT NULL,
    number_of_seats INTEGER NOT NULL,
    total_amount DOUBLE PRECISION NOT NULL,
    distance DOUBLE PRECISION NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'REFUNDED')),
    passenger_names TEXT,
    passenger_contacts TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    FOREIGN KEY (boarding_point_id) REFERENCES route_points(id) ON DELETE CASCADE,
    FOREIGN KEY (drop_point_id) REFERENCES route_points(id) ON DELETE CASCADE
);

-- Create indexes for bookings table
CREATE INDEX idx_booking_user ON bookings(user_id);
CREATE INDEX idx_booking_trip ON bookings(trip_id);
CREATE INDEX idx_booking_status ON bookings(status);

-- Booking Seats Table (for storing booked seat numbers)
CREATE TABLE booking_seats (
    booking_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- Payments Table
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    booking_id BIGINT NOT NULL UNIQUE,
    amount DOUBLE PRECISION NOT NULL,
    payment_method VARCHAR(50) NOT NULL CHECK (payment_method IN ('CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'NET_BANKING', 'WALLET')),
    status VARCHAR(50) NOT NULL DEFAULT 'INITIATED' CHECK (status IN ('INITIATED', 'SUCCESS', 'FAILED', 'REFUNDED', 'PENDING')),
    payment_gateway_response TEXT,
    paid_at TIMESTAMP,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- Create indexes for payments table
CREATE INDEX idx_payment_booking ON payments(booking_id);
CREATE INDEX idx_payment_status ON payments(status);

-- Sample Data Insertion (Optional)

-- Insert Sample User (Passenger)
INSERT INTO users (name, phone, password, role) 
VALUES ('John Doe', '9876543210', '$2a$10$encrypted_password_here', 'PASSENGER');

-- Insert Sample User (Driver)
INSERT INTO users (name, phone, password, role) 
VALUES ('Jane Smith', '9876543211', '$2a$10$encrypted_password_here', 'DRIVER');

-- Database Optimization Notes:
-- 1. All foreign keys have ON DELETE CASCADE for referential integrity
-- 2. Indexes created on frequently queried columns (dates, status, user_id, trip_id)
-- 3. Unique constraints on critical fields (email, phone, license_number, registration_number)
-- 4. Check constraints for enum-like fields to ensure data integrity
-- 5. Timestamps for audit trail (created_at, updated_at)
-- 6. Proper data types for optimization (BIGSERIAL for IDs, DOUBLE PRECISION for prices)

-- Views for Common Queries (Optional but recommended)

-- View: Available Trips Summary
CREATE VIEW available_trips_summary AS
SELECT 
    t.id as trip_id,
    r.route_name,
    u.name as driver_name,
    u.phone as driver_phone,
    d.rating as driver_rating,
    v.brand || ' ' || v.model as vehicle_name,
    v.vehicle_type,
    v.registration_number,
    t.departure_time,
    t.estimated_arrival_time,
    t.available_seats,
    t.status
FROM trips t
JOIN routes r ON t.route_id = r.id
JOIN drivers d ON t.driver_id = d.id
JOIN users u ON d.user_id = u.id
JOIN vehicles v ON t.vehicle_id = v.id
WHERE t.status = 'SCHEDULED' AND t.available_seats > 0;

-- View: User Booking History
CREATE VIEW user_booking_history AS
SELECT 
    b.id as booking_id,
    b.booking_reference,
    u.name as passenger_name,
    bp.point_name as boarding_point,
    dp.point_name as drop_point,
    t.departure_time,
    b.number_of_seats,
    b.total_amount,
    b.status as booking_status,
    p.status as payment_status,
    driveruser.name as driver_name,
    v.brand || ' ' || v.model as vehicle_name
FROM bookings b
JOIN users u ON b.user_id = u.id
JOIN trips t ON b.trip_id = t.id
JOIN route_points bp ON b.boarding_point_id = bp.id
JOIN route_points dp ON b.drop_point_id = dp.id
JOIN drivers d ON t.driver_id = d.id
JOIN users driveruser ON d.user_id = driveruser.id
JOIN vehicles v ON t.vehicle_id = v.id
LEFT JOIN payments p ON b.id = p.booking_id;

-- Function to update driver rating (Optional)
CREATE OR REPLACE FUNCTION update_driver_rating()
RETURNS TRIGGER AS $$
BEGIN
    -- This is a placeholder for rating calculation logic
    -- In real implementation, calculate average rating from reviews
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_trips_updated_at BEFORE UPDATE ON trips
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_bookings_updated_at BEFORE UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

