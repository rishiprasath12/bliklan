package com.app.carpolling.controller;

import com.app.carpolling.dto.DriverLocationDto;
import com.app.carpolling.entity.User;
import com.app.carpolling.repository.DriverRepository;
import com.app.carpolling.repository.TripRepository;
import com.app.carpolling.repository.UserRepository;
import com.app.carpolling.service.DriverLocationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class DriverLocationController {

    private static final Logger logger = LoggerFactory.getLogger(DriverLocationController.class);

    private final DriverLocationService driverLocationService;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final TripRepository tripRepository;

    public DriverLocationController(DriverLocationService driverLocationService,
                                   UserRepository userRepository,
                                   DriverRepository driverRepository,
                                   TripRepository tripRepository) {
        this.driverLocationService = driverLocationService;
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.tripRepository = tripRepository;
    }

    /**
     * WebSocket endpoint: Driver sends location updates.
     * Destination: /app/driver/location
     * Driver must own the trip.
     */
    @MessageMapping("/driver/location")
    public void handleDriverLocation(@Payload @Valid DriverLocationDto dto, Principal principal) {
        if (principal == null || principal.getName() == null) {
            logger.warn("Driver location rejected: not authenticated");
            throw new IllegalArgumentException("Authentication required");
        }
        String phoneNumber = principal.getName();
        User user = userRepository.findByPhone(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var driver = driverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User is not a driver"));

        if (tripRepository.findByIdAndDriver_Id(dto.getTripId(), driver.getId()).isEmpty()) {
            logger.warn("Driver {} attempted to update location for trip {} they do not own", driver.getId(), dto.getTripId());
            throw new IllegalArgumentException("You do not have permission to update location for this trip");
        }

        driverLocationService.updateAndBroadcastLocation(dto);
    }
}
