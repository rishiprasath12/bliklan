# Carpolling API Summary

This document provides a comprehensive overview of all available APIs in the Carpolling application.

**Base URL**: `http://localhost:8080`

**Authentication**: Most endpoints require JWT token in the Authorization header as `Bearer <token>` or via HTTP-only cookie.

---

## Table of Contents
1. [User APIs](#user-apis)
2. [Driver APIs](#driver-apis)
3. [Trip APIs](#trip-apis)
4. [Booking APIs](#booking-apis)
5. [Payment APIs](#payment-apis)
6. [Admin APIs](#admin-apis)

---

## User APIs

### 1. Register User
- **Endpoint**: `POST /api/users/register`
- **Description**: Register a new user account
- **Authentication**: Not required
- **Request Body**:
```json
{
  "name": "string",
  "phoneNumber": "string",
  "email": "string",
  "password": "string"
}
```
- **Response**: `201 CREATED`
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "jwt-token",
    "user": { /* user details */ }
  }
}
```

### 2. Login
- **Endpoint**: `POST /api/users/login`
- **Description**: Authenticate user and receive JWT token
- **Authentication**: Not required
- **Request Body**:
```json
{
  "phoneNumber": "string",
  "password": "string"
}
```
- **Response**: `200 OK` + Sets JWT cookie
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "jwt-token",
    "user": { /* user details */ }
  }
}
```

### 3. Logout
- **Endpoint**: `POST /api/users/logout`
- **Description**: Logout user and invalidate JWT token
- **Authentication**: Required
- **Response**: `200 OK` + Clears JWT cookie
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null
}
```

### 4. Get Current User Details
- **Endpoint**: `GET /api/users/getUserDetails`
- **Description**: Get authenticated user's details
- **Authentication**: Required
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "User details fetched successfully",
  "data": {
    "id": 1,
    "name": "string",
    "email": "string",
    "phoneNumber": "string",
    "role": "USER"
  }
}
```

---

## Driver APIs

### 1. Register Driver
- **Endpoint**: `POST /api/drivers/register`
- **Description**: Register a new driver
- **Authentication**: Required
- **Request Body**:
```json
{
  "userId": 1,
  "licenseNumber": "string",
  "licenseExpiryDate": "YYYY-MM-DD",
  "aadharNumber": "string",
  "address": "string"
}
```
- **Response**: `201 CREATED`
```json
{
  "success": true,
  "message": "Driver registered successfully",
  "data": { /* driver details */ }
}
```

### 2. Get Driver by ID
- **Endpoint**: `GET /api/drivers/{driverId}`
- **Description**: Retrieve driver details by ID
- **Authentication**: Required
- **Response**: `200 OK`

### 3. Register Vehicle
- **Endpoint**: `POST /api/drivers/vehicles`
- **Description**: Register a new vehicle for a driver
- **Authentication**: Required
- **Request Body**:
```json
{
  "driverId": 1,
  "vehicleNumber": "string",
  "vehicleType": "SEDAN/SUV/HATCHBACK/TEMPO_TRAVELLER/BUS",
  "vehicleModel": "string",
  "vehicleColor": "string",
  "totalSeats": 4
}
```
- **Response**: `201 CREATED`

### 4. Get Driver Vehicles
- **Endpoint**: `GET /api/drivers/{driverId}/vehicles`
- **Description**: Get all vehicles owned by a driver
- **Authentication**: Required
- **Response**: `200 OK`

### 5. Create Route
- **Endpoint**: `POST /api/drivers/routes`
- **Description**: Create a new route with boarding and drop points
- **Authentication**: Required
- **Request Body**:
```json
{
  "driverId": 1,
  "routeName": "string",
  "routePoints": [
    {
      "city": "string",
      "subLocation": "string",
      "latitude": 0.0,
      "longitude": 0.0,
      "stopOrder": 1,
      "isBoardingPoint": true,
      "isDropPoint": false
    }
  ]
}
```
- **Response**: `201 CREATED`

### 6. Get Driver Routes
- **Endpoint**: `GET /api/drivers/{driverId}/routes`
- **Description**: Get all routes created by a driver
- **Authentication**: Required
- **Response**: `200 OK`

### 7. Get Route Price Combinations
- **Endpoint**: `GET /api/drivers/routes/{routeId}/price-combinations`
- **Description**: Get all possible boarding-drop point combinations for pricing
- **Authentication**: Required
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Route price combinations retrieved successfully",
  "data": [
    {
      "boardingPointId": 1,
      "boardingCity": "string",
      "boardingSubLocation": "string",
      "dropPointId": 2,
      "dropCity": "string",
      "dropSubLocation": "string",
      "currentPrice": 100.00
    }
  ]
}
```

### 8. Set Route Prices
- **Endpoint**: `POST /api/drivers/routes/prices`
- **Description**: Set prices for boarding-drop point combinations
- **Authentication**: Required
- **Request Body**:
```json
{
  "routeId": 1,
  "prices": [
    {
      "boardingPointId": 1,
      "dropPointId": 2,
      "price": 100.00
    }
  ]
}
```
- **Response**: `200 OK`

---

## Trip APIs

### 1. Create Trip
- **Endpoint**: `POST /api/trips`
- **Description**: Create a new trip for a specific route and vehicle
- **Authentication**: Required (Driver)
- **Request Body**:
```json
{
  "routeId": 1,
  "vehicleId": 1,
  "departureTime": "2026-01-25T10:00:00",
  "driverId": 1
}
```
- **Response**: `201 CREATED`

### 2. Search Trips
- **Endpoint**: `POST /api/trips/search`
- **Description**: Search available trips based on boarding/drop locations and date
- **Authentication**: Not required
- **Request Body**:
```json
{
  "boardingCity": "string",
  "boardingSubLocation": "string (optional)",
  "dropCity": "string",
  "dropSubLocation": "string (optional)",
  "travelDate": "YYYY-MM-DD"
}
```
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Trips retrieved successfully",
  "data": [
    {
      "tripId": 1,
      "departureTime": "2026-01-25T10:00:00",
      "availableSeats": 3,
      "totalSeats": 4,
      "price": 100.00,
      "driverName": "string",
      "vehicleNumber": "string",
      "vehicleType": "SEDAN",
      "boardingPoint": { /* point details */ },
      "dropPoint": { /* point details */ }
    }
  ]
}
```

### 3. Get Seat Availability
- **Endpoint**: `GET /api/trips/{tripId}/seats`
- **Description**: Get seat availability for a specific trip
- **Authentication**: Required
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Seat availability retrieved successfully",
  "data": {
    "tripId": 1,
    "totalSeats": 4,
    "availableSeats": 3,
    "seats": [
      {
        "seatNumber": 1,
        "isAvailable": true
      }
    ]
  }
}
```

### 4. Get All Cities
- **Endpoint**: `GET /api/trips/cities?search={searchTerm}`
- **Description**: Get all available cities (with optional search)
- **Authentication**: Not required
- **Query Parameters**: 
  - `search` (optional): Search term to filter cities
- **Response**: `200 OK`

### 5. Get Boarding Cities
- **Endpoint**: `GET /api/trips/cities/boarding`
- **Description**: Get all cities with boarding points
- **Authentication**: Not required
- **Response**: `200 OK`

### 6. Get Drop Cities
- **Endpoint**: `GET /api/trips/cities/drop`
- **Description**: Get all cities with drop points
- **Authentication**: Not required
- **Response**: `200 OK`

### 7. Get Boarding Sub-locations
- **Endpoint**: `GET /api/trips/locations/boarding?city={cityName}`
- **Description**: Get all boarding sub-locations within a city
- **Authentication**: Not required
- **Query Parameters**: 
  - `city` (required): City name
- **Response**: `200 OK`

### 8. Get Drop Sub-locations
- **Endpoint**: `GET /api/trips/locations/drop?city={cityName}`
- **Description**: Get all drop sub-locations within a city
- **Authentication**: Not required
- **Query Parameters**: 
  - `city` (required): City name
- **Response**: `200 OK`

### 9. Get All Boarding Points
- **Endpoint**: `GET /api/trips/boarding-points?search={searchTerm}`
- **Description**: Get all boarding points with optional search
- **Authentication**: Not required
- **Response**: `200 OK`

### 10. Get All Drop Points
- **Endpoint**: `GET /api/trips/drop-points?search={searchTerm}`
- **Description**: Get all drop points with optional search
- **Authentication**: Not required
- **Response**: `200 OK`

### 11. Get Boarding Locations by City
- **Endpoint**: `GET /api/trips/boarding-locations?city={cityName}`
- **Description**: Get detailed boarding locations for a city
- **Authentication**: Not required
- **Response**: `200 OK`

### 12. Get Trips by Driver
- **Endpoint**: `GET /api/trips/driver/{driverId}`
- **Description**: Get all trips created by a specific driver
- **Authentication**: Required
- **Response**: `200 OK`

### 13. Get Trip Route Points
- **Endpoint**: `GET /api/trips/{tripId}/route-points?boardingCity={city}&dropCity={city}`
- **Description**: Get route points between boarding and drop cities for a trip
- **Authentication**: Required
- **Query Parameters**: 
  - `boardingCity` (required)
  - `dropCity` (required)
- **Response**: `200 OK`

---

## Booking APIs

### 1. Create Booking
- **Endpoint**: `POST /api/bookings`
- **Description**: Create a new booking for a trip
- **Authentication**: Required
- **Request Body**:
```json
{
  "userId": 1,
  "tripId": 1,
  "boardingPointId": 1,
  "dropPointId": 2,
  "numberOfSeats": 2,
  "passengerName": "string",
  "passengerPhone": "string",
  "passengerEmail": "string"
}
```
- **Response**: `201 CREATED`
```json
{
  "success": true,
  "message": "Booking created successfully",
  "data": {
    "id": 1,
    "bookingReference": "BOOK123456",
    "status": "PENDING",
    "totalAmount": 200.00,
    /* other booking details */
  }
}
```

### 2. Get User Bookings
- **Endpoint**: `GET /api/bookings/user/{userId}`
- **Description**: Get all bookings made by a user
- **Authentication**: Required
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Bookings retrieved successfully",
  "data": [
    {
      "bookingId": 1,
      "bookingReference": "BOOK123456",
      "status": "CONFIRMED",
      "tripDetails": { /* trip info */ },
      "paymentStatus": "PAID"
    }
  ]
}
```

### 3. Get Booking by Reference
- **Endpoint**: `GET /api/bookings/reference/{bookingReference}`
- **Description**: Get booking details by booking reference number
- **Authentication**: Required
- **Response**: `200 OK`

### 4. Cancel Booking
- **Endpoint**: `PUT /api/bookings/{bookingId}/cancel`
- **Description**: Cancel a booking
- **Authentication**: Required
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Booking cancelled successfully",
  "data": { /* updated booking details */ }
}
```

---

## Payment APIs

### 1. Create Payment Order
- **Endpoint**: `POST /api/payments/create-order`
- **Description**: Create a Razorpay payment order for a booking
- **Authentication**: Required
- **Request Body**:
```json
{
  "bookingId": 1
}
```
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Razorpay order created successfully",
  "data": {
    "orderId": "order_xxxxx",
    "amount": 20000,
    "currency": "INR",
    "keyId": "rzp_test_xxxxx"
  }
}
```

### 2. Payment Callback
- **Endpoint**: `POST /api/payments/payment-callback`
- **Description**: Verify Razorpay payment signature and update payment status
- **Authentication**: Required
- **Request Body**:
```json
{
  "razorpayOrderId": "order_xxxxx",
  "razorpayPaymentId": "pay_xxxxx",
  "razorpaySignature": "signature_xxxxx",
  "bookingId": 1
}
```
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Payment verified successfully",
  "data": {
    "id": 1,
    "status": "SUCCESS",
    "transactionId": "pay_xxxxx",
    "amount": 200.00
  }
}
```

### 3. Get Payment by Booking ID
- **Endpoint**: `GET /api/payments/booking/{bookingId}`
- **Description**: Get payment details for a specific booking
- **Authentication**: Required
- **Response**: `200 OK`

### 4. Get Payment by Transaction ID
- **Endpoint**: `GET /api/payments/transaction/{transactionId}`
- **Description**: Get payment details by Razorpay transaction ID
- **Authentication**: Required
- **Response**: `200 OK`

### 5. Get Payment by ID
- **Endpoint**: `GET /api/payments/{paymentId}`
- **Description**: Get payment details by payment ID
- **Authentication**: Required
- **Response**: `200 OK`

---

## Admin APIs

**Note**: All admin APIs require ADMIN role authentication.

### 1. Get Dashboard Stats
- **Endpoint**: `GET /api/admin/dashboard/stats`
- **Description**: Get overall system statistics for admin dashboard
- **Authentication**: Required (Admin)
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Dashboard stats retrieved successfully",
  "data": {
    "totalUsers": 100,
    "totalDrivers": 20,
    "totalTrips": 50,
    "totalBookings": 200,
    "totalRevenue": 50000.00,
    "pendingVerifications": 5
  }
}
```

### 2. Get All Users
- **Endpoint**: `GET /api/admin/users`
- **Description**: Get list of all registered users
- **Authentication**: Required (Admin)
- **Response**: `200 OK`

### 3. Activate User
- **Endpoint**: `PUT /api/admin/users/{userId}/activate`
- **Description**: Activate a deactivated user account
- **Authentication**: Required (Admin)
- **Response**: `200 OK`

### 4. Deactivate User
- **Endpoint**: `PUT /api/admin/users/{userId}/deactivate`
- **Description**: Deactivate a user account
- **Authentication**: Required (Admin)
- **Response**: `200 OK`

### 5. Get All Drivers
- **Endpoint**: `GET /api/admin/drivers`
- **Description**: Get list of all registered drivers
- **Authentication**: Required (Admin)
- **Response**: `200 OK`

### 6. Get Unverified Drivers
- **Endpoint**: `GET /api/admin/drivers/unverified`
- **Description**: Get list of drivers pending verification
- **Authentication**: Required (Admin)
- **Response**: `200 OK`

### 7. Verify Driver
- **Endpoint**: `PUT /api/admin/drivers/{driverId}/verify`
- **Description**: Verify a driver's credentials
- **Authentication**: Required (Admin)
- **Response**: `200 OK`

### 8. Unverify Driver
- **Endpoint**: `PUT /api/admin/drivers/{driverId}/unverify`
- **Description**: Revoke driver verification
- **Authentication**: Required (Admin)
- **Response**: `200 OK`

### 9. Get All Vehicles
- **Endpoint**: `GET /api/admin/vehicles`
- **Description**: Get list of all registered vehicles
- **Authentication**: Required (Admin)
- **Response**: `200 OK`

### 10. Get All Trips
- **Endpoint**: `GET /api/admin/trips`
- **Description**: Get list of all trips in the system
- **Authentication**: Required (Admin)
- **Response**: `200 OK`

### 11. Get All Bookings
- **Endpoint**: `GET /api/admin/bookings`
- **Description**: Get list of all bookings in the system
- **Authentication**: Required (Admin)
- **Response**: `200 OK`

---

## Common Response Format

All API responses follow a standard format:

### Success Response
```json
{
  "success": true,
  "message": "Operation successful message",
  "data": { /* response data */ }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error message describing what went wrong",
  "data": null
}
```

---

## HTTP Status Codes

- `200 OK` - Request successful
- `201 CREATED` - Resource created successfully
- `400 BAD_REQUEST` - Invalid request or validation error
- `401 UNAUTHORIZED` - Authentication required or failed
- `403 FORBIDDEN` - Insufficient permissions
- `404 NOT_FOUND` - Resource not found
- `500 INTERNAL_SERVER_ERROR` - Server error

---

## Authentication

Most endpoints require JWT authentication. Include the token in the request header:

```
Authorization: Bearer <your-jwt-token>
```

Or the token will be automatically sent via HTTP-only cookie after login.

---

## Vehicle Types
- `SEDAN`
- `SUV`
- `HATCHBACK`
- `TEMPO_TRAVELLER`
- `BUS`

## Booking Status
- `PENDING` - Booking created, payment pending
- `CONFIRMED` - Payment successful
- `CANCELLED` - Booking cancelled
- `COMPLETED` - Trip completed

## Payment Status
- `PENDING` - Payment not initiated
- `SUCCESS` - Payment successful
- `FAILED` - Payment failed

## Payment Methods
- `RAZORPAY` - Online payment via Razorpay
- `CASH` - Cash payment (if supported)

## User Roles
- `USER` - Regular user
- `DRIVER` - Driver (can also book as user)
- `ADMIN` - Administrator

---

## Notes

1. **CORS**: All endpoints support CORS with `origins = "*"`
2. **Validation**: Request bodies are validated using Jakarta Validation annotations
3. **Date Format**: Use ISO 8601 format for dates (`YYYY-MM-DD` or `YYYY-MM-DDTHH:mm:ss`)
4. **Pagination**: Currently not implemented, all list endpoints return full results
5. **Rate Limiting**: Not currently implemented

---

## Additional Resources

- **Swagger UI**: Available at `http://localhost:8080/swagger-ui.html`
- **API Documentation**: Available at `http://localhost:8080/v3/api-docs`
- **Postman Collection**: See `postman_collection.json` in project root
- **Database Schema**: See `database-schema.sql` in project root

---

**Last Updated**: January 22, 2026
