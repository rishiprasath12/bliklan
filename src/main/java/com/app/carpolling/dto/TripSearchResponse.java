package com.app.carpolling.dto;

import com.app.carpolling.entity.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripSearchResponse {
    private Long tripId;
    private String driverName;
    private String driverPhone;
    private Double driverRating;
    private String vehicleBrand;
    private String vehicleModel;
    private String vehicleColor;
    private String registrationNumber;
    private VehicleType vehicleType;
    private Boolean hasAC;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer availableSeats;
    private Double price; // Fixed price for this boarding-drop combination
    private Double distance;
    private Integer duration;
    private String routeName;
}









