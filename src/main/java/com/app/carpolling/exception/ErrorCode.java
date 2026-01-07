package com.app.carpolling.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    
    // 400 - Bad Request (Validation and Business Logic Errors)
    PASSWORD_TOO_SHORT(400, "Password must be at least 8 characters long"),
    PASSWORD_MISSING_UPPERCASE(400, "Password must contain at least one uppercase letter"),
    PASSWORD_MISSING_NUMBER(400, "Password must contain at least one number"),
    PASSWORD_MISSING_SPECIAL_CHAR(400, "Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;':\"\\,.<>/?)"),
    TRIP_NOT_AVAILABLE(400, "Trip is not available for booking"),
    NOT_ENOUGH_SEATS(400, "Not enough seats available"),
    BOOKING_ALREADY_CANCELLED(400, "Booking is already cancelled"),
    BOOKING_NOT_PENDING(400, "Booking is not in pending state"),
    INVALID_REQUEST(400, "Invalid request"),
    INVALID_ROUTE(400, "Invalid route configuration"),
    
    // 401 - Unauthorized (Authentication Errors)
    INVALID_CREDENTIALS(401, "Invalid credentials provided"),
    UNAUTHORIZED_ACCESS(401, "Unauthorized access"),
    
    // 403 - Forbidden (Authorization Errors)
    USER_ACCOUNT_INACTIVE(403, "User account is inactive"),
    VEHICLE_NOT_BELONGS_TO_DRIVER(403, "Vehicle does not belong to this driver"),
    FORBIDDEN_ACCESS(403, "Access forbidden"),
    
    // 404 - Not Found (Resource Not Found Errors)
    USER_NOT_FOUND(404, "User not found"),
    DRIVER_NOT_FOUND(404, "Driver not found"),
    VEHICLE_NOT_FOUND(404, "Vehicle not found"),
    TRIP_NOT_FOUND(404, "Trip not found"),
    ROUTE_NOT_FOUND(404, "Route not found"),
    ROUTE_POINT_NOT_FOUND(404, "Route point not found"),
    BOARDING_POINT_NOT_FOUND(404, "Boarding point not found"),
    DROP_POINT_NOT_FOUND(404, "Drop point not found"),
    BOOKING_NOT_FOUND(404, "Booking not found"),
    SEAT_NOT_FOUND(404, "Seat not found"),
    PAYMENT_NOT_FOUND(404, "Payment not found"),
    PRICE_NOT_FOUND(404, "Price not configured for this route combination"),
    RESOURCE_NOT_FOUND(404, "Requested resource not found"),
    
    // 409 - Conflict (Resource Already Exists or State Conflict)
    PHONE_ALREADY_REGISTERED(409, "Phone number is already registered"),
    EMAIL_ALREADY_REGISTERED(409, "Email address is already registered"),
    DRIVER_PROFILE_ALREADY_EXISTS(409, "Driver profile already exists for this user"),
    LICENSE_NUMBER_ALREADY_REGISTERED(409, "License number is already registered"),
    VEHICLE_REGISTRATION_EXISTS(409, "Vehicle registration number already exists"),
    SEAT_ALREADY_BOOKED(409, "Seat is already booked"),
    PAYMENT_ALREADY_EXISTS(409, "Payment already exists for this booking"),
    
    // 422 - Unprocessable Entity (Business Logic Failures)
    PAYMENT_FAILED(422, "Payment processing failed"),
    
    // 429 - Too Many Requests (Rate Limiting)
    TOO_MANY_REQUESTS(429, "Too many requests, please try again later"),
    
    // 500 - Internal Server Error (System Errors)
    INTERNAL_SERVER_ERROR(500, "Internal server error occurred"),
    DATABASE_ERROR(500, "Database operation failed"),
    EXTERNAL_SERVICE_ERROR(500, "External service unavailable");
    
    private final int code;
    private final String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

