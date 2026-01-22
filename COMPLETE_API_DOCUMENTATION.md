# Complete API Documentation - Car Pooling Application

## Base URL
```
http://localhost:8080
```

## Response Format

All API responses follow this format:
```json
{
  "success": true/false,
  "message": "Response message",
  "data": { ... }
}
```

## Table of Contents
1. [User Management APIs](#1-user-management-apis)
2. [Driver Management APIs](#2-driver-management-apis)
3. [Trip Management APIs](#3-trip-management-apis)
4. [Booking Management APIs](#4-booking-management-apis)
5. [Payment Management APIs](#5-payment-management-apis)
6. [Admin Management APIs](#6-admin-management-apis)
7. [Error Responses](#error-responses)
8. [Workflow Examples](#workflow-examples)

---

## 1. User Management APIs

### 1.1 Register User

Register a new user (passenger or driver).

**Endpoint:** `POST /api/users/register`

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "9876543210",
  "password": "SecurePass@123",
  "role": "PASSENGER"
}
```

**Notes:**
- **Phone number is required** (10 digits)
- **Email is optional** (can be null or omitted)
- **Password requirements:** minimum 8 characters, at least one uppercase letter, one number, and one special character

**Role Values:** `PASSENGER`, `DRIVER`, `BOTH`

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "userId": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "9876543210",
    "role": "PASSENGER",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### 1.2 Login

Authenticate user using phone number.

**Endpoint:** `POST /api/users/login`

**Request Body:**
```json
{
  "phone": "9876543210",
  "password": "SecurePass@123"
}
```

**Notes:**
- **Login is phone-based only**
- Phone number must be 10 digits
- Email-based login is not supported
- JWT token is returned in response body and also set as HttpOnly cookie

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "9876543210",
    "role": "PASSENGER",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

### 1.3 Logout

Logout the current user and invalidate JWT token.

**Endpoint:** `POST /api/users/logout`

**Authentication Required:** Yes (JWT token in Authorization header or cookie)

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null
}
```

**Notes:**
- Token is added to blacklist
- JWT cookie is cleared
- Security context is cleared

### 1.4 Get User Details

Get details of the currently authenticated user.

**Endpoint:** `GET /api/users/getUserDetails`

**Authentication Required:** Yes (JWT token in Authorization header)

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "User details fetched successfully",
  "data": {
    "userId": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "9876543210",
    "role": "PASSENGER",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

---

## 2. Driver Management APIs

### 2.1 Register as Driver

Register driver profile for an existing user.

**Endpoint:** `POST /api/drivers/register`

**Request Body:**
```json
{
  "userId": 1,
  "licenseNumber": "DL1234567890",
  "licenseExpiryDate": "2025-12-31",
  "experienceYears": 5,
  "additionalInfo": "Professional driver with 5 years experience"
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Driver registered successfully",
  "data": {
    "id": 1,
    "user": { ... },
    "licenseNumber": "DL1234567890",
    "licenseExpiryDate": "2025-12-31",
    "experienceYears": 5,
    "isVerified": false,
    "rating": 0.0,
    "totalTrips": 0
  }
}
```

### 2.2 Get Driver by ID

Get driver details by driver ID.

**Endpoint:** `GET /api/drivers/{driverId}`

**Example:** `GET /api/drivers/1`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Driver retrieved successfully",
  "data": {
    "id": 1,
    "user": {
      "userId": 1,
      "name": "Jane Smith",
      "phone": "9876543211"
    },
    "licenseNumber": "DL1234567890",
    "experienceYears": 5,
    "rating": 4.5,
    "totalTrips": 25,
    "isVerified": true
  }
}
```

### 2.3 Register Vehicle

Add a vehicle to driver's profile.

**Endpoint:** `POST /api/drivers/vehicles`

**Request Body:**
```json
{
  "driverId": 1,
  "registrationNumber": "MH01AB1234",
  "brand": "Toyota",
  "model": "Innova Crysta",
  "color": "White",
  "manufacturingYear": 2020,
  "vehicleType": "SUV",
  "totalSeats": 7,
  "hasAC": true,
  "features": "GPS Navigation, Premium Sound System, USB Charging"
}
```

**Vehicle Types:** `SEDAN`, `SUV`, `HATCHBACK`, `MUV`, `LUXURY`

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Vehicle registered successfully",
  "data": {
    "id": 1,
    "registrationNumber": "MH01AB1234",
    "brand": "Toyota",
    "model": "Innova Crysta",
    "passengerSeats": 6,
    "isActive": true
  }
}
```

### 2.4 Get Driver Vehicles

Get all vehicles registered by a driver.

**Endpoint:** `GET /api/drivers/{driverId}/vehicles`

**Example:** `GET /api/drivers/1/vehicles`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Vehicles retrieved successfully",
  "data": [
    {
      "id": 1,
      "registrationNumber": "MH01AB1234",
      "brand": "Toyota",
      "model": "Innova Crysta",
      "totalSeats": 7,
      "passengerSeats": 6,
      "isActive": true
    }
  ]
}
```

### 2.5 Create Route

Create a route with multiple points.

**Endpoint:** `POST /api/drivers/routes`

**Request Body:**
```json
{
  "driverId": 1,
  "routeName": "Mumbai to Pune via Lonavala",
  "totalDistance": 150.5,
  "estimatedDuration": 180,
  "routePoints": [
    {
      "pointName": "Mumbai Central",
      "address": "Mumbai Central Railway Station, Mumbai",
      "city": "Mumbai",
      "state": "Maharashtra",
      "country": "India",
      "latitude": 18.9682,
      "longitude": 72.8209,
      "sequenceOrder": 1,
      "distanceFromStart": 0,
      "timeFromStart": 0,
      "isBoardingPoint": true,
      "isDropPoint": false
    },
    {
      "pointName": "Lonavala",
      "address": "Lonavala Bus Stand, Lonavala",
      "city": "Lonavala",
      "state": "Maharashtra",
      "country": "India",
      "latitude": 18.7537,
      "longitude": 73.4086,
      "sequenceOrder": 2,
      "distanceFromStart": 83,
      "timeFromStart": 90,
      "isBoardingPoint": true,
      "isDropPoint": true
    },
    {
      "pointName": "Pune Station",
      "address": "Pune Railway Station, Pune",
      "city": "Pune",
      "state": "Maharashtra",
      "country": "India",
      "latitude": 18.5204,
      "longitude": 73.8567,
      "sequenceOrder": 3,
      "distanceFromStart": 150,
      "timeFromStart": 180,
      "isBoardingPoint": false,
      "isDropPoint": true
    }
  ]
}
```

**Note:** 
- `distanceFromStart` is in kilometers
- `timeFromStart` is in minutes
- Points must be ordered by `sequenceOrder`

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Route created successfully",
  "data": {
    "id": 1,
    "routeName": "Mumbai to Pune via Lonavala",
    "totalDistance": 150.5,
    "estimatedDuration": 180,
    "routePoints": [ ... ]
  }
}
```

### 2.6 Get Driver Routes

Get all routes created by a driver.

**Endpoint:** `GET /api/drivers/{driverId}/routes`

**Example:** `GET /api/drivers/1/routes`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Routes retrieved successfully",
  "data": [
    {
      "id": 1,
      "routeName": "Mumbai to Pune via Lonavala",
      "totalDistance": 150.5,
      "estimatedDuration": 180,
      "routePoints": [ ... ]
    }
  ]
}
```

### 2.7 Get Route Price Combinations

Get all possible boarding and drop point combinations for a route with prices.

**Endpoint:** `GET /api/drivers/routes/{routeId}/price-combinations`

**Example:** `GET /api/drivers/routes/1/price-combinations`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Route price combinations retrieved successfully",
  "data": [
    {
      "boardingPointId": 1,
      "boardingPointName": "Mumbai Central",
      "dropPointId": 2,
      "dropPointName": "Lonavala",
      "distance": 83.0,
      "estimatedDuration": 90,
      "price": null
    },
    {
      "boardingPointId": 1,
      "boardingPointName": "Mumbai Central",
      "dropPointId": 3,
      "dropPointName": "Pune Station",
      "distance": 150.0,
      "estimatedDuration": 180,
      "price": 1500.0
    }
  ]
}
```

### 2.8 Set Route Prices

Set prices for specific boarding and drop point combinations on a route.

**Endpoint:** `POST /api/drivers/routes/prices`

**Request Body:**
```json
{
  "routeId": 1,
  "prices": [
    {
      "boardingPointId": 1,
      "dropPointId": 2,
      "price": 800.0
    },
    {
      "boardingPointId": 1,
      "dropPointId": 3,
      "price": 1500.0
    },
    {
      "boardingPointId": 2,
      "dropPointId": 3,
      "price": 700.0
    }
  ]
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Route prices set successfully",
  "data": "Prices updated"
}
```

---

## 3. Trip Management APIs

### 3.1 Create Trip

Schedule a trip on a route.

**Endpoint:** `POST /api/trips`

**Request Body:**
```json
{
  "routeId": 1,
  "vehicleId": 1,
  "driverId": 1,
  "departureTime": "2024-12-25T08:00:00",
  "basePricePerKm": 10.0,
  "specialInstructions": "Please arrive 10 minutes before departure"
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Trip created successfully",
  "data": {
    "id": 1,
    "departureTime": "2024-12-25T08:00:00",
    "estimatedArrivalTime": "2024-12-25T11:00:00",
    "availableSeats": 6,
    "status": "SCHEDULED"
  }
}
```

### 3.2 Search Trips

Search for available trips between two points on a date.

**Endpoint:** `POST /api/trips/search`

**Request Body:**
```json
{
  "boardingPoint": "Mumbai Central",
  "dropPoint": "Pune Station",
  "travelDate": "2024-12-25",
  "requiredSeats": 2
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Trips retrieved successfully",
  "data": [
    {
      "tripId": 1,
      "driverName": "Jane Smith",
      "driverPhone": "9876543211",
      "driverRating": 4.5,
      "vehicleBrand": "Toyota",
      "vehicleModel": "Innova Crysta",
      "vehicleColor": "White",
      "registrationNumber": "MH01AB1234",
      "vehicleType": "SUV",
      "hasAC": true,
      "departureTime": "2024-12-25T08:00:00",
      "arrivalTime": "2024-12-25T11:00:00",
      "availableSeats": 6,
      "pricePerKm": 10.0,
      "totalPrice": 1500.0,
      "distance": 150.0,
      "duration": 180,
      "routeName": "Mumbai to Pune via Lonavala"
    }
  ]
}
```

### 3.3 Get Seat Availability

Get seat layout and availability for a trip.

**Endpoint:** `GET /api/trips/{tripId}/seats`

**Example:** `GET /api/trips/1/seats`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Seat availability retrieved successfully",
  "data": {
    "tripId": 1,
    "totalSeats": 6,
    "availableSeats": 4,
    "seats": [
      {
        "seatNumber": "S1",
        "isAvailable": true,
        "isDriverSeat": false
      },
      {
        "seatNumber": "S2",
        "isAvailable": false,
        "isDriverSeat": false
      }
    ]
  }
}
```

### 3.4 Get All Cities

Get list of all cities available in the system with optional search.

**Endpoint:** `GET /api/trips/cities`

**Query Parameters:**
- `search` (optional): Search term to filter cities

**Examples:**
- `GET /api/trips/cities` - Get all cities
- `GET /api/trips/cities?search=Mum` - Search cities starting with "Mum"

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Cities retrieved successfully",
  "data": [
    "Mumbai",
    "Pune",
    "Lonavala",
    "Thane"
  ]
}
```

### 3.5 Get Boarding Cities

Get list of all cities that have boarding points.

**Endpoint:** `GET /api/trips/cities/boarding`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Boarding cities retrieved successfully",
  "data": [
    "Mumbai",
    "Lonavala",
    "Thane"
  ]
}
```

### 3.6 Get Drop Cities

Get list of all cities that have drop points.

**Endpoint:** `GET /api/trips/cities/drop`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Drop cities retrieved successfully",
  "data": [
    "Pune",
    "Lonavala",
    "Khandala"
  ]
}
```

### 3.7 Get Boarding Sub-Locations by City

Get all boarding sub-locations (specific points) within a city.

**Endpoint:** `GET /api/trips/locations/boarding`

**Query Parameters:**
- `city` (required): City name

**Example:** `GET /api/trips/locations/boarding?city=Mumbai`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Boarding locations retrieved successfully",
  "data": [
    "Mumbai Central",
    "Dadar",
    "Bandra"
  ]
}
```

### 3.8 Get Drop Sub-Locations by City

Get all drop sub-locations (specific points) within a city.

**Endpoint:** `GET /api/trips/locations/drop`

**Query Parameters:**
- `city` (required): City name

**Example:** `GET /api/trips/locations/drop?city=Pune`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Drop locations retrieved successfully",
  "data": [
    "Pune Station",
    "Shivajinagar",
    "Katraj"
  ]
}
```

### 3.9 Get All Boarding Points

Get list of all boarding points (full details) with optional search.

**Endpoint:** `GET /api/trips/boarding-points`

**Query Parameters:**
- `search` (optional): Search term to filter boarding points

**Examples:**
- `GET /api/trips/boarding-points` - Get all boarding points
- `GET /api/trips/boarding-points?search=Central` - Search boarding points

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Boarding points retrieved successfully",
  "data": [
    {
      "id": 1,
      "pointName": "Mumbai Central",
      "address": "Mumbai Central Railway Station, Mumbai",
      "city": "Mumbai",
      "latitude": 18.9682,
      "longitude": 72.8209
    }
  ]
}
```

### 3.10 Get All Drop Points

Get list of all drop points (full details) with optional search.

**Endpoint:** `GET /api/trips/drop-points`

**Query Parameters:**
- `search` (optional): Search term to filter drop points

**Examples:**
- `GET /api/trips/drop-points` - Get all drop points
- `GET /api/trips/drop-points?search=Station` - Search drop points

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Drop points retrieved successfully",
  "data": [
    {
      "id": 3,
      "pointName": "Pune Station",
      "address": "Pune Railway Station, Pune",
      "city": "Pune",
      "latitude": 18.5204,
      "longitude": 73.8567
    }
  ]
}
```

### 3.11 Get Boarding Locations by City (Hierarchical)

Get boarding locations grouped by city with full hierarchical information.

**Endpoint:** `GET /api/trips/boarding-locations`

**Query Parameters:**
- `city` (required): City name

**Example:** `GET /api/trips/boarding-locations?city=Mumbai`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Boarding locations retrieved successfully",
  "data": [
    {
      "city": "Mumbai",
      "subLocation": "Mumbai Central",
      "address": "Mumbai Central Railway Station, Mumbai"
    }
  ]
}
```

### 3.12 Get Trips by Driver ID

Get all trips created by a specific driver.

**Endpoint:** `GET /api/trips/driver/{driverId}`

**Example:** `GET /api/trips/driver/1`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Trips retrieved successfully",
  "data": [
    {
      "id": 1,
      "route": { ... },
      "vehicle": { ... },
      "departureTime": "2024-12-25T08:00:00",
      "estimatedArrivalTime": "2024-12-25T11:00:00",
      "availableSeats": 6,
      "status": "SCHEDULED"
    }
  ]
}
```

### 3.13 Get Trip Route Points

Get specific boarding and drop points for a trip based on cities.

**Endpoint:** `GET /api/trips/{tripId}/route-points`

**Query Parameters:**
- `boardingCity` (required): Boarding city name
- `dropCity` (required): Drop city name

**Example:** `GET /api/trips/1/route-points?boardingCity=Mumbai&dropCity=Pune`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Route points retrieved successfully",
  "data": {
    "boardingPoints": [
      {
        "id": 1,
        "pointName": "Mumbai Central",
        "city": "Mumbai"
      }
    ],
    "dropPoints": [
      {
        "id": 3,
        "pointName": "Pune Station",
        "city": "Pune"
      }
    ]
  }
}
```

---

## 4. Booking Management APIs

### 4.1 Create Booking

Book seats on a trip.

**Endpoint:** `POST /api/bookings`

**Request Body:**
```json
{
  "userId": 1,
  "tripId": 1,
  "boardingPointId": 1,
  "dropPointId": 3,
  "seatNumbers": ["S1", "S2"],
  "passengerNames": "John Doe, Jane Doe",
  "passengerContacts": "9876543210, 9876543211"
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "message": "Booking created successfully",
  "data": {
    "id": 1,
    "bookingReference": "BK20241125093045",
    "numberOfSeats": 2,
    "seatNumbers": ["S1", "S2"],
    "totalAmount": 1500.0,
    "status": "PENDING"
  }
}
```

### 4.2 Get User Bookings

Get all bookings for a user.

**Endpoint:** `GET /api/bookings/user/{userId}`

**Example:** `GET /api/bookings/user/1`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Bookings retrieved successfully",
  "data": [
    {
      "bookingId": 1,
      "bookingReference": "BK20241125093045",
      "tripId": 1,
      "boardingPoint": "Mumbai Central",
      "dropPoint": "Pune Station",
      "departureTime": "2024-12-25T08:00:00",
      "seatNumbers": ["S1", "S2"],
      "totalAmount": 1500.0,
      "status": "CONFIRMED",
      "driverName": "Jane Smith",
      "driverPhone": "9876543211",
      "vehicleDetails": "Toyota Innova Crysta (White) - MH01AB1234",
      "bookedAt": "2024-11-25T09:30:45"
    }
  ]
}
```

### 4.3 Get Booking by Reference

Get booking details by booking reference number.

**Endpoint:** `GET /api/bookings/reference/{bookingReference}`

**Example:** `GET /api/bookings/reference/BK20241125093045`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Booking retrieved successfully",
  "data": {
    "bookingId": 1,
    "bookingReference": "BK20241125093045",
    "tripId": 1,
    "boardingPoint": "Mumbai Central",
    "dropPoint": "Pune Station",
    "departureTime": "2024-12-25T08:00:00",
    "seatNumbers": ["S1", "S2"],
    "totalAmount": 1500.0,
    "status": "CONFIRMED"
  }
}
```

### 4.4 Cancel Booking

Cancel an existing booking.

**Endpoint:** `PUT /api/bookings/{bookingId}/cancel`

**Example:** `PUT /api/bookings/1/cancel`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Booking cancelled successfully",
  "data": {
    "id": 1,
    "bookingReference": "BK20241125093045",
    "status": "CANCELLED"
  }
}
```

---

## 5. Payment Management APIs

### 5.1 Create Payment Order (Razorpay)

Create a Razorpay order for a booking.

**Endpoint:** `POST /api/payments/create-order`

**Request Body:**
```json
{
  "bookingId": 1
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Razorpay order created successfully",
  "data": {
    "orderId": "order_MjK8xPQrZvL7Y3",
    "amount": 150000,
    "currency": "INR",
    "bookingId": 1,
    "bookingReference": "BK20241125093045",
    "razorpayKeyId": "rzp_test_xxxxxxxxxxxxx"
  }
}
```

**Notes:**
- Amount is in paise (e.g., 150000 paise = 1500 INR)
- Use this order ID for Razorpay payment integration on frontend
- razorpayKeyId is provided for frontend integration

### 5.2 Payment Callback (Razorpay Verification)

Verify Razorpay payment after successful payment on frontend.

**Endpoint:** `POST /api/payments/payment-callback`

**Request Body:**
```json
{
  "razorpayOrderId": "order_MjK8xPQrZvL7Y3",
  "razorpayPaymentId": "pay_MjK8xPQrZvL7Y3",
  "razorpaySignature": "9bcad0b09b9e1e65f3c3b1f6e6f1e1e1e1e1e1e1e1e1e1e1e1e1e1e1e1e1",
  "bookingId": 1
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Payment verified successfully",
  "data": {
    "id": 1,
    "booking": { ... },
    "transactionId": "pay_MjK8xPQrZvL7Y3",
    "amount": 1500.0,
    "status": "SUCCESS",
    "paidAt": "2024-11-25T09:32:12"
  }
}
```

**Failure Response (402 Payment Required):**
```json
{
  "success": false,
  "message": "Payment verification failed: Signature mismatch",
  "data": null
}
```

**Notes:**
- On successful verification, booking status is automatically updated to `CONFIRMED`
- Payment signature is verified using Razorpay secret key

### 5.3 Get Payment by Booking ID

Get payment details for a booking.

**Endpoint:** `GET /api/payments/booking/{bookingId}`

**Example:** `GET /api/payments/booking/1`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Payment retrieved successfully",
  "data": {
    "id": 1,
    "transactionId": "pay_MjK8xPQrZvL7Y3",
    "amount": 1500.0,
    "status": "SUCCESS",
    "paidAt": "2024-11-25T09:32:12"
  }
}
```

### 5.4 Get Payment by Transaction ID

Get payment details by transaction ID (Razorpay payment ID).

**Endpoint:** `GET /api/payments/transaction/{transactionId}`

**Example:** `GET /api/payments/transaction/pay_MjK8xPQrZvL7Y3`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Payment retrieved successfully",
  "data": {
    "id": 1,
    "transactionId": "pay_MjK8xPQrZvL7Y3",
    "bookingId": 1,
    "amount": 1500.0,
    "status": "SUCCESS",
    "paidAt": "2024-11-25T09:32:12"
  }
}
```

### 5.5 Get Payment by Payment ID

Get payment details by payment ID.

**Endpoint:** `GET /api/payments/{paymentId}`

**Example:** `GET /api/payments/1`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Payment retrieved successfully",
  "data": {
    "id": 1,
    "booking": { ... },
    "transactionId": "pay_MjK8xPQrZvL7Y3",
    "amount": 1500.0,
    "status": "SUCCESS",
    "paidAt": "2024-11-25T09:32:12"
  }
}
```

---

## 6. Admin Management APIs

### 6.1 Dashboard Statistics

Get overall platform statistics for admin dashboard.

**Endpoint:** `GET /api/admin/dashboard/stats`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Dashboard stats retrieved successfully",
  "data": {
    "totalUsers": 1250,
    "totalDrivers": 180,
    "totalVehicles": 200,
    "totalTrips": 450,
    "totalBookings": 2300,
    "totalRevenue": 1850000.0,
    "activeTrips": 25,
    "pendingDriverVerifications": 8,
    "todayBookings": 45,
    "todayRevenue": 35000.0
  }
}
```

### 6.2 Get All Users

Get list of all users in the system.

**Endpoint:** `GET /api/admin/users`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Users retrieved successfully",
  "data": [
    {
      "userId": 1,
      "name": "John Doe",
      "email": "john@example.com",
      "phone": "9876543210",
      "role": "PASSENGER",
      "isActive": true,
      "createdAt": "2024-01-15T10:30:00"
    }
  ]
}
```

### 6.3 Activate User

Activate a deactivated user account.

**Endpoint:** `PUT /api/admin/users/{userId}/activate`

**Example:** `PUT /api/admin/users/1/activate`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "User activated successfully",
  "data": {
    "userId": 1,
    "name": "John Doe",
    "isActive": true
  }
}
```

### 6.4 Deactivate User

Deactivate a user account.

**Endpoint:** `PUT /api/admin/users/{userId}/deactivate`

**Example:** `PUT /api/admin/users/1/deactivate`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "User deactivated successfully",
  "data": {
    "userId": 1,
    "name": "John Doe",
    "isActive": false
  }
}
```

### 6.5 Get All Drivers

Get list of all drivers in the system.

**Endpoint:** `GET /api/admin/drivers`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Drivers retrieved successfully",
  "data": [
    {
      "id": 1,
      "user": {
        "userId": 2,
        "name": "Jane Smith",
        "phone": "9876543211"
      },
      "licenseNumber": "DL1234567890",
      "experienceYears": 5,
      "rating": 4.5,
      "totalTrips": 25,
      "isVerified": true
    }
  ]
}
```

### 6.6 Get Unverified Drivers

Get list of drivers pending verification.

**Endpoint:** `GET /api/admin/drivers/unverified`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Unverified drivers retrieved successfully",
  "data": [
    {
      "id": 5,
      "user": {
        "userId": 10,
        "name": "New Driver",
        "phone": "9876543222"
      },
      "licenseNumber": "DL9876543210",
      "experienceYears": 3,
      "isVerified": false
    }
  ]
}
```

### 6.7 Verify Driver

Verify a driver's documents and profile.

**Endpoint:** `PUT /api/admin/drivers/{driverId}/verify`

**Example:** `PUT /api/admin/drivers/5/verify`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Driver verified successfully",
  "data": {
    "id": 5,
    "user": { ... },
    "isVerified": true
  }
}
```

### 6.8 Unverify Driver

Revoke driver verification.

**Endpoint:** `PUT /api/admin/drivers/{driverId}/unverify`

**Example:** `PUT /api/admin/drivers/5/unverify`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Driver verification revoked successfully",
  "data": {
    "id": 5,
    "user": { ... },
    "isVerified": false
  }
}
```

### 6.9 Get All Vehicles

Get list of all vehicles in the system.

**Endpoint:** `GET /api/admin/vehicles`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Vehicles retrieved successfully",
  "data": [
    {
      "id": 1,
      "registrationNumber": "MH01AB1234",
      "brand": "Toyota",
      "model": "Innova Crysta",
      "vehicleType": "SUV",
      "totalSeats": 7,
      "passengerSeats": 6,
      "isActive": true,
      "driver": { ... }
    }
  ]
}
```

### 6.10 Get All Trips

Get list of all trips in the system.

**Endpoint:** `GET /api/admin/trips`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Trips retrieved successfully",
  "data": [
    {
      "id": 1,
      "route": { ... },
      "vehicle": { ... },
      "driver": { ... },
      "departureTime": "2024-12-25T08:00:00",
      "estimatedArrivalTime": "2024-12-25T11:00:00",
      "availableSeats": 6,
      "status": "SCHEDULED"
    }
  ]
}
```

### 6.11 Get All Bookings

Get list of all bookings in the system.

**Endpoint:** `GET /api/admin/bookings`

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Bookings retrieved successfully",
  "data": [
    {
      "id": 1,
      "bookingReference": "BK20241125093045",
      "user": { ... },
      "trip": { ... },
      "numberOfSeats": 2,
      "seatNumbers": ["S1", "S2"],
      "totalAmount": 1500.0,
      "status": "CONFIRMED",
      "bookedAt": "2024-11-25T09:30:45"
    }
  ]
}
```

---

## Error Responses

### Validation Error (400 Bad Request)
```json
{
  "success": false,
  "message": "Validation failed",
  "data": null
}
```

### Unauthorized (401 Unauthorized)
```json
{
  "success": false,
  "message": "Invalid credentials",
  "data": null
}
```

### Not Found (404 Not Found)
```json
{
  "success": false,
  "message": "Resource not found",
  "data": null
}
```

### Server Error (500 Internal Server Error)
```json
{
  "success": false,
  "message": "An error occurred: [error details]",
  "data": null
}
```

---

## Workflow Examples

### Complete Passenger Booking Flow

1. **Register/Login**
   ```
   POST /api/users/register
   or
   POST /api/users/login
   ```

2. **Get Boarding Cities**
   ```
   GET /api/trips/cities/boarding
   ```

3. **Get Drop Cities**
   ```
   GET /api/trips/cities/drop
   ```

4. **Get Boarding Locations in Selected City**
   ```
   GET /api/trips/locations/boarding?city=Mumbai
   ```

5. **Get Drop Locations in Selected City**
   ```
   GET /api/trips/locations/drop?city=Pune
   ```

6. **Search Trips**
   ```
   POST /api/trips/search
   ```

7. **View Seat Availability**
   ```
   GET /api/trips/{tripId}/seats
   ```

8. **Create Booking**
   ```
   POST /api/bookings
   ```

9. **Create Payment Order**
   ```
   POST /api/payments/create-order
   ```

10. **Complete Payment on Frontend (Razorpay)**
    - Use Razorpay SDK with order ID from step 9

11. **Verify Payment**
    ```
    POST /api/payments/payment-callback
    ```

12. **View Booking Details**
    ```
    GET /api/bookings/reference/{bookingReference}
    ```

### Complete Driver Onboarding Flow

1. **Register as User**
   ```
   POST /api/users/register (role: DRIVER or BOTH)
   ```

2. **Login**
   ```
   POST /api/users/login
   ```

3. **Register as Driver**
   ```
   POST /api/drivers/register
   ```

4. **Register Vehicle**
   ```
   POST /api/drivers/vehicles
   ```

5. **Create Route with Multiple Points**
   ```
   POST /api/drivers/routes
   ```

6. **View Route Price Combinations**
   ```
   GET /api/drivers/routes/{routeId}/price-combinations
   ```

7. **Set Route Prices**
   ```
   POST /api/drivers/routes/prices
   ```

8. **Create Trip**
   ```
   POST /api/trips
   ```

9. **View Your Trips**
   ```
   GET /api/trips/driver/{driverId}
   ```

### Admin Workflow

1. **View Dashboard Statistics**
   ```
   GET /api/admin/dashboard/stats
   ```

2. **View Pending Driver Verifications**
   ```
   GET /api/admin/drivers/unverified
   ```

3. **Verify Driver**
   ```
   PUT /api/admin/drivers/{driverId}/verify
   ```

4. **View All Bookings**
   ```
   GET /api/admin/bookings
   ```

5. **Manage Users**
   ```
   GET /api/admin/users
   PUT /api/admin/users/{userId}/activate
   PUT /api/admin/users/{userId}/deactivate
   ```

---

## Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Alternatively, the JWT token can be sent as an HttpOnly cookie (automatically set during login).

## Notes

- All timestamps are in ISO 8601 format
- Prices are in the base currency (e.g., INR)
- Distances are in kilometers
- Durations are in minutes
- All endpoints support CORS for cross-origin requests
- Razorpay integration is used for payments
- Admin endpoints may require additional role-based authorization (not yet implemented)

## Status Enums

### Booking Status
- `PENDING` - Booking created, payment pending
- `CONFIRMED` - Payment successful, booking confirmed
- `CANCELLED` - Booking cancelled by user
- `COMPLETED` - Trip completed

### Trip Status
- `SCHEDULED` - Trip is scheduled
- `IN_PROGRESS` - Trip is ongoing
- `COMPLETED` - Trip completed
- `CANCELLED` - Trip cancelled

### Payment Status
- `PENDING` - Payment order created
- `SUCCESS` - Payment successful and verified
- `FAILED` - Payment failed
- `REFUNDED` - Payment refunded

### User Role
- `PASSENGER` - Can only book trips
- `DRIVER` - Can only create trips
- `BOTH` - Can both book and create trips
- `ADMIN` - Administrative access

### Vehicle Type
- `SEDAN` - Sedan car
- `SUV` - SUV car
- `HATCHBACK` - Hatchback car
- `MUV` - Multi Utility Vehicle
- `LUXURY` - Luxury car
