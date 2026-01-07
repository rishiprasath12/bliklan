package com.app.carpolling.service;

import com.app.carpolling.dto.BookingRequest;
import com.app.carpolling.dto.BookingResponse;
import com.app.carpolling.entity.*;
import com.app.carpolling.exception.BaseException;
import com.app.carpolling.exception.ErrorCode;
import com.app.carpolling.repository.BookingRepository;
import com.app.carpolling.repository.RoutePointRepository;
import com.app.carpolling.repository.RoutePriceRepository;
import com.app.carpolling.repository.TripSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final TripSeatRepository tripSeatRepository;
    private final RoutePointRepository routePointRepository;
    private final RoutePriceRepository routePriceRepository;
    private final UserService userService;
    private final TripService tripService;
    
    @Transactional
    public Booking createBooking(BookingRequest request) {
        // Get dependencies
        User user = userService.getUserById(request.getUserId());
        Trip trip = tripService.getTripById(request.getTripId());
        
        RoutePoint boardingPoint = routePointRepository.findById(request.getBoardingPointId())
            .orElseThrow(() -> new BaseException(ErrorCode.BOARDING_POINT_NOT_FOUND));
        
        RoutePoint dropPoint = routePointRepository.findById(request.getDropPointId())
            .orElseThrow(() -> new BaseException(ErrorCode.DROP_POINT_NOT_FOUND));
        
        // Validate trip is scheduled
        if (trip.getStatus() != TripStatus.SCHEDULED) {
            throw new BaseException(ErrorCode.TRIP_NOT_AVAILABLE);
        }
        
        // Validate seats availability
        if (trip.getAvailableSeats() < request.getSeatNumbers().size()) {
            throw new BaseException(ErrorCode.NOT_ENOUGH_SEATS);
        }
        
        // Validate and reserve seats
        for (String seatNumber : request.getSeatNumbers()) {
            TripSeat seat = tripSeatRepository.findByTripIdAndSeatNumber(trip.getId(), seatNumber)
                .orElseThrow(() -> new BaseException(ErrorCode.SEAT_NOT_FOUND, "Seat " + seatNumber + " not found"));
            
            if (!seat.getIsAvailable()) {
                throw new BaseException(ErrorCode.SEAT_ALREADY_BOOKED, "Seat " + seatNumber + " is already booked");
            }
            
            // Reserve seat
            seat.setIsAvailable(false);
            tripSeatRepository.save(seat);
        }
        
        // Get fixed price from route price matrix
        RoutePrice routePrice = routePriceRepository.findByRouteAndPoints(
            trip.getRoute().getId(),
            boardingPoint.getId(),
            dropPoint.getId()
        ).orElseThrow(() -> new BaseException(ErrorCode.PRICE_NOT_FOUND, 
            "Price not configured for this boarding-drop combination"));
        
        // Calculate distance (for reference)
        double distance = (dropPoint.getDistanceFromStart() - boardingPoint.getDistanceFromStart()) / 1000.0;
        
        // Calculate total amount
        double pricePerSeat = routePrice.getPrice();
        double totalAmount = pricePerSeat * request.getSeatNumbers().size();
        
        // Update trip available seats
        trip.setAvailableSeats(trip.getAvailableSeats() - request.getSeatNumbers().size());
        trip.setBookedSeats(trip.getBookedSeats() + request.getSeatNumbers().size());
        
        // Create booking
        Booking booking = new Booking();
        booking.setBookingReference(generateBookingReference());
        booking.setUser(user);
        booking.setTrip(trip);
        booking.setBoardingPoint(boardingPoint);
        booking.setDropPoint(dropPoint);
        booking.setNumberOfSeats(request.getSeatNumbers().size());
        booking.setSeatNumbers(request.getSeatNumbers());
        booking.setTotalAmount(totalAmount);
        booking.setDistance(distance);
        booking.setStatus(BookingStatus.PENDING);
        booking.setPassengerNames(request.getPassengerNames());
        booking.setPassengerContacts(request.getPassengerContacts());
        
        return bookingRepository.save(booking);
    }
    
    private String generateBookingReference() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "BK" + timestamp;
    }
    
    @Transactional
    public Booking confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BaseException(ErrorCode.BOOKING_NOT_FOUND));
        
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }
    
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BaseException(ErrorCode.BOOKING_NOT_FOUND));
        
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BaseException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }
        
        // Release seats
        for (String seatNumber : booking.getSeatNumbers()) {
            TripSeat seat = tripSeatRepository.findByTripIdAndSeatNumber(
                booking.getTrip().getId(), seatNumber
            ).orElseThrow(() -> new BaseException(ErrorCode.SEAT_NOT_FOUND));
            
            seat.setIsAvailable(true);
            tripSeatRepository.save(seat);
        }
        
        // Update trip available seats
        Trip trip = booking.getTrip();
        trip.setAvailableSeats(trip.getAvailableSeats() + booking.getNumberOfSeats());
        trip.setBookedSeats(trip.getBookedSeats() - booking.getNumberOfSeats());
        
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }
    
    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return bookings.stream()
            .map(this::convertToBookingResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
            .orElseThrow(() -> new BaseException(ErrorCode.BOOKING_NOT_FOUND));
        return convertToBookingResponse(booking);
    }
    
    private BookingResponse convertToBookingResponse(Booking booking) {
        String vehicleDetails = String.format("%s %s (%s) - %s",
            booking.getTrip().getVehicle().getBrand(),
            booking.getTrip().getVehicle().getModel(),
            booking.getTrip().getVehicle().getColor(),
            booking.getTrip().getVehicle().getRegistrationNumber()
        );
        
        return new BookingResponse(
            booking.getId(),
            booking.getBookingReference(),
            booking.getTrip().getId(),
            booking.getBoardingPoint().getPointName(),
            booking.getDropPoint().getPointName(),
            booking.getTrip().getDepartureTime(),
            booking.getSeatNumbers(),
            booking.getTotalAmount(),
            booking.getStatus(),
            booking.getTrip().getDriver().getUser().getName(),
            booking.getTrip().getDriver().getUser().getPhone(),
            vehicleDetails,
            booking.getCreatedAt()
        );
    }
    
    @Transactional(readOnly = true)
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
            .orElseThrow(() -> new BaseException(ErrorCode.BOOKING_NOT_FOUND));
    }
}









