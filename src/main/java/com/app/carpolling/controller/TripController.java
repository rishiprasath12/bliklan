package com.app.carpolling.controller;

import com.app.carpolling.dto.*;
import com.app.carpolling.entity.Trip;
import com.app.carpolling.service.RouteService;
import com.app.carpolling.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TripController {
    
    private final TripService tripService;
    private final RouteService routeService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Trip>> createTrip(
        @Valid @RequestBody TripCreationRequest request
    ) {
        try {
            Trip trip = tripService.createTrip(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trip created successfully", trip));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<TripSearchResponse>>> searchTrips(
        @Valid @RequestBody TripSearchRequest request
    ) {
        try {
            List<TripSearchResponse> trips = tripService.searchTrips(request);
            return ResponseEntity.ok(
                ApiResponse.success("Trips retrieved successfully", trips)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{tripId}/seats")
    public ResponseEntity<ApiResponse<SeatAvailabilityResponse>> getSeatAvailability(
        @PathVariable Long tripId
    ) {
        try {
            SeatAvailabilityResponse response = tripService.getSeatAvailability(tripId);
            return ResponseEntity.ok(
                ApiResponse.success("Seat availability retrieved successfully", response)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/cities")
    public ResponseEntity<ApiResponse<List<String>>> getAllCities(
        @RequestParam(required = false) String search
    ) {
        try {
            List<String> cities;
            if (search != null && !search.trim().isEmpty()) {
                cities = routeService.searchCities(search);
            } else {
                cities = routeService.getAllCities();
            }
            return ResponseEntity.ok(
                ApiResponse.success("Cities retrieved successfully", cities)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/cities/boarding")
    public ResponseEntity<ApiResponse<List<String>>> getBoardingCities() {
        try {
            List<String> cities = routeService.getBoardingCities();
            return ResponseEntity.ok(
                ApiResponse.success("Boarding cities retrieved successfully", cities)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/cities/drop")
    public ResponseEntity<ApiResponse<List<String>>> getDropCities() {
        try {
            List<String> cities = routeService.getDropCities();
            return ResponseEntity.ok(
                ApiResponse.success("Drop cities retrieved successfully", cities)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/locations/boarding")
    public ResponseEntity<ApiResponse<List<String>>> getBoardingSubLocations(
        @RequestParam String city
    ) {
        try {
            List<String> locations = routeService.getBoardingSubLocationsByCity(city);
            return ResponseEntity.ok(
                ApiResponse.success("Boarding locations retrieved successfully", locations)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/locations/drop")
    public ResponseEntity<ApiResponse<List<String>>> getDropSubLocations(
        @RequestParam String city
    ) {
        try {
            List<String> locations = routeService.getDropSubLocationsByCity(city);
            return ResponseEntity.ok(
                ApiResponse.success("Drop locations retrieved successfully", locations)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/boarding-points")
    public ResponseEntity<ApiResponse<List<com.app.carpolling.entity.RoutePoint>>> getAllBoardingPoints(
        @RequestParam(required = false) String search
    ) {
        try {
            List<com.app.carpolling.entity.RoutePoint> points;
            if (search != null && !search.trim().isEmpty()) {
                points = routeService.searchBoardingPoints(search);
            } else {
                points = routeService.getAllBoardingPoints();
            }
            return ResponseEntity.ok(
                ApiResponse.success("Boarding points retrieved successfully", points)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/drop-points")
    public ResponseEntity<ApiResponse<List<com.app.carpolling.entity.RoutePoint>>> getAllDropPoints(
        @RequestParam(required = false) String search
    ) {
        try {
            List<com.app.carpolling.entity.RoutePoint> points;
            if (search != null && !search.trim().isEmpty()) {
                points = routeService.searchDropPoints(search);
            } else {
                points = routeService.getAllDropPoints();
            }
            return ResponseEntity.ok(
                ApiResponse.success("Drop points retrieved successfully", points)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/boarding-locations")
    public ResponseEntity<ApiResponse<List<LocationResponseDto>>> getBoardingLocationsByCity(@RequestParam String city) {
        List<LocationResponseDto> results = routeService.getBoardingLocationsByCity(city);
        return ResponseEntity.ok(ApiResponse.success("Boarding locations retrieved successfully", results));
    }
    
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponse<List<Trip>>> getTripsByDriverId(
        @PathVariable Long driverId
    ) {
        try {
            List<Trip> trips = tripService.getTripsByDriverId(driverId);
            return ResponseEntity.ok(
                ApiResponse.success("Trips retrieved successfully", trips)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{tripId}/route-points")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getTripRoutePoints(
        @PathVariable Long tripId,
        @RequestParam String boardingCity,
        @RequestParam String dropCity
    ) {
        try {
            java.util.Map<String, Object> routePoints = tripService.getTripRoutePoints(tripId, boardingCity, dropCity);
            return ResponseEntity.ok(
                ApiResponse.success("Route points retrieved successfully", routePoints)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}

