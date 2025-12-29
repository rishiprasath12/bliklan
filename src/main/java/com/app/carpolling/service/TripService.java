package com.app.carpolling.service;

import com.app.carpolling.dto.SeatAvailabilityResponse;
import com.app.carpolling.dto.TripCreationRequest;
import com.app.carpolling.dto.TripSearchRequest;
import com.app.carpolling.dto.TripSearchResponse;
import com.app.carpolling.entity.*;
import com.app.carpolling.exception.BaseException;
import com.app.carpolling.exception.ErrorCode;
import com.app.carpolling.repository.RoutePointRepository;
import com.app.carpolling.repository.TripRepository;
import com.app.carpolling.repository.TripSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripService {
    
    private final TripRepository tripRepository;
    private final TripSeatRepository tripSeatRepository;
    private final RoutePointRepository routePointRepository;
    private final RouteService routeService;
    private final VehicleService vehicleService;
    private final DriverService driverService;
    
    @Transactional
    public Trip createTrip(TripCreationRequest request) {
        // Get dependencies
        Route route = routeService.getRouteById(request.getRouteId());
        Vehicle vehicle = vehicleService.getVehicleById(request.getVehicleId());
        Driver driver = driverService.getDriverById(request.getDriverId());
        
        // Validate driver owns the vehicle
        if (!vehicle.getDriver().getId().equals(driver.getId())) {
            throw new BaseException(ErrorCode.VEHICLE_NOT_BELONGS_TO_DRIVER);
        }
        
        // Calculate estimated arrival time
        LocalDateTime estimatedArrival = request.getDepartureTime()
            .plusMinutes(route.getEstimatedDuration());
        
        // Create trip
        Trip trip = new Trip();
        trip.setRoute(route);
        trip.setVehicle(vehicle);
        trip.setDriver(driver);
        trip.setDepartureTime(request.getDepartureTime());
        trip.setEstimatedArrivalTime(estimatedArrival);
        trip.setBasePricePerKm(request.getBasePricePerKm());
        trip.setAvailableSeats(vehicle.getPassengerSeats());
        trip.setBookedSeats(0);
        trip.setStatus(TripStatus.SCHEDULED);
        trip.setSpecialInstructions(request.getSpecialInstructions());
        
        Trip savedTrip = tripRepository.save(trip);
        
        // Create seats for the trip
        createSeatsForTrip(savedTrip, vehicle);
        
        return savedTrip;
    }
    
    private void createSeatsForTrip(Trip trip, Vehicle vehicle) {
        List<TripSeat> seats = new ArrayList<>();
        
        // Create driver seat
        TripSeat driverSeat = new TripSeat();
        driverSeat.setTrip(trip);
        driverSeat.setSeatNumber("D1");
        driverSeat.setIsAvailable(false);
        driverSeat.setIsDriverSeat(true);
        seats.add(driverSeat);
        
        // Create passenger seats
        for (int i = 1; i <= vehicle.getPassengerSeats(); i++) {
            TripSeat seat = new TripSeat();
            seat.setTrip(trip);
            seat.setSeatNumber("S" + i);
            seat.setIsAvailable(true);
            seat.setIsDriverSeat(false);
            seats.add(seat);
        }
        
        tripSeatRepository.saveAll(seats);
    }
    
    @Transactional(readOnly = true)
    public List<TripSearchResponse> searchTrips(TripSearchRequest request) {
        // Find routes that have both boarding and drop points
        List<Route> routes = routeService.findRoutesByBoardingAndDropPoint(
            request.getBoardingPoint(),
            request.getDropPoint()
        );
        
        if (routes.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> routeIds = routes.stream()
            .map(Route::getId)
            .collect(Collectors.toList());
        
        // Find available trips for these routes on the specified date
        LocalDateTime startOfDay = request.getTravelDate().atStartOfDay();
        LocalDateTime endOfDay = request.getTravelDate().atTime(LocalTime.MAX);
        
        List<Trip> trips = tripRepository.findAvailableTrips(routeIds, startOfDay, endOfDay);
        
        // Convert to response DTOs
        List<TripSearchResponse> responses = new ArrayList<>();
        for (Trip trip : trips) {
            if (trip.getAvailableSeats() >= request.getRequiredSeats()) {
                TripSearchResponse response = buildTripSearchResponse(
                    trip,
                    request.getBoardingPoint(),
                    request.getDropPoint()
                );
                responses.add(response);
            }
        }
        
        return responses;
    }
    
    private TripSearchResponse buildTripSearchResponse(Trip trip, String boardingPoint, String dropPoint) {
        // Get route points
        List<RoutePoint> routePoints = routePointRepository
            .findByRouteIdOrderBySequenceOrderAsc(trip.getRoute().getId());
        
        RoutePoint boarding = routePoints.stream()
            .filter(rp -> rp.getPointName().equals(boardingPoint))
            .findFirst()
            .orElseThrow(() -> new BaseException(ErrorCode.BOARDING_POINT_NOT_FOUND));
        
        RoutePoint drop = routePoints.stream()
            .filter(rp -> rp.getPointName().equals(dropPoint))
            .findFirst()
            .orElseThrow(() -> new BaseException(ErrorCode.DROP_POINT_NOT_FOUND));
        
        double distance = (drop.getDistanceFromStart() - boarding.getDistanceFromStart()) / 1000.0;
        int duration = drop.getTimeFromStart() - boarding.getTimeFromStart();
        double totalPrice = distance * trip.getBasePricePerKm();
        
        LocalDateTime arrivalTime = trip.getDepartureTime().plusMinutes(duration);
        
        TripSearchResponse response = new TripSearchResponse();
        response.setTripId(trip.getId());
        response.setDriverName(trip.getDriver().getUser().getName());
        response.setDriverPhone(trip.getDriver().getUser().getPhone());
        response.setDriverRating(trip.getDriver().getRating());
        response.setVehicleBrand(trip.getVehicle().getBrand());
        response.setVehicleModel(trip.getVehicle().getModel());
        response.setVehicleColor(trip.getVehicle().getColor());
        response.setRegistrationNumber(trip.getVehicle().getRegistrationNumber());
        response.setVehicleType(trip.getVehicle().getVehicleType());
        response.setHasAC(trip.getVehicle().getHasAC());
        response.setDepartureTime(trip.getDepartureTime());
        response.setArrivalTime(arrivalTime);
        response.setAvailableSeats(trip.getAvailableSeats());
        response.setPricePerKm(trip.getBasePricePerKm());
        response.setTotalPrice(totalPrice);
        response.setDistance(distance);
        response.setDuration(duration);
        response.setRouteName(trip.getRoute().getRouteName());
        
        return response;
    }
    
    @Transactional(readOnly = true)
    public SeatAvailabilityResponse getSeatAvailability(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
            .orElseThrow(() -> new BaseException(ErrorCode.TRIP_NOT_FOUND));
        
        List<TripSeat> seats = tripSeatRepository.findByTripId(tripId);
        
        List<SeatAvailabilityResponse.SeatInfo> seatInfos = seats.stream()
            .filter(seat -> !seat.getIsDriverSeat()) // Exclude driver seat
            .map(seat -> new SeatAvailabilityResponse.SeatInfo(
                seat.getSeatNumber(),
                seat.getIsAvailable(),
                seat.getIsDriverSeat()
            ))
            .collect(Collectors.toList());
        
        return new SeatAvailabilityResponse(
            tripId,
            trip.getVehicle().getTotalSeats() - 1, // Exclude driver seat
            trip.getAvailableSeats(),
            seatInfos
        );
    }
    
    @Transactional(readOnly = true)
    public Trip getTripById(Long tripId) {
        return tripRepository.findById(tripId)
            .orElseThrow(() -> new BaseException(ErrorCode.TRIP_NOT_FOUND));
    }
}









