package com.app.carpolling.controller;

import com.app.carpolling.dto.*;
import com.app.carpolling.entity.Driver;
import com.app.carpolling.entity.Route;
import com.app.carpolling.entity.Vehicle;
import com.app.carpolling.service.DriverService;
import com.app.carpolling.service.RouteService;
import com.app.carpolling.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DriverController {
    
    private final DriverService driverService;
    private final VehicleService vehicleService;
    private final RouteService routeService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Driver>> registerDriver(
        @Valid @RequestBody DriverRegistrationRequest request
    ) {
        try {
            Driver driver = driverService.registerDriver(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver registered successfully", driver));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{driverId}")
    public ResponseEntity<ApiResponse<Driver>> getDriverById(@PathVariable Long driverId) {
        try {
            Driver driver = driverService.getDriverById(driverId);
            return ResponseEntity.ok(ApiResponse.success("Driver retrieved successfully", driver));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/vehicles")
    public ResponseEntity<ApiResponse<Vehicle>> registerVehicle(
        @Valid @RequestBody VehicleRegistrationRequest request
    ) {
        try {
            Vehicle vehicle = vehicleService.registerVehicle(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle registered successfully", vehicle));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{driverId}/vehicles")
    public ResponseEntity<ApiResponse<List<Vehicle>>> getDriverVehicles(
        @PathVariable Long driverId
    ) {
        try {
            List<Vehicle> vehicles = vehicleService.getVehiclesByDriverId(driverId);
            return ResponseEntity.ok(ApiResponse.success("Vehicles retrieved successfully", vehicles));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/routes")
    public ResponseEntity<ApiResponse<Route>> createRoute(
        @Valid @RequestBody RouteCreationRequest request
    ) {
        try {
            Route route = routeService.createRoute(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Route created successfully", route));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/{driverId}/routes")
    public ResponseEntity<ApiResponse<List<Route>>> getDriverRoutes(
        @PathVariable Long driverId
    ) {
        try {
            List<Route> routes = routeService.getRoutesByDriverId(driverId);
            return ResponseEntity.ok(ApiResponse.success("Routes retrieved successfully", routes));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/routes/{routeId}/price-combinations")
    public ResponseEntity<ApiResponse<List<RoutePriceCombinationDto>>> getRoutePriceCombinations(
        @PathVariable Long routeId
    ) {
        try {
            List<RoutePriceCombinationDto> combinations = routeService.getRoutePriceCombinations(routeId);
            return ResponseEntity.ok(
                ApiResponse.success("Route price combinations retrieved successfully", combinations)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/routes/prices")
    public ResponseEntity<ApiResponse<String>> setRoutePrices(
        @Valid @RequestBody SetRoutePricesRequest request
    ) {
        try {
            routeService.setRoutePrices(request);
            return ResponseEntity.ok(
                ApiResponse.success("Route prices set successfully", "Prices updated")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}









