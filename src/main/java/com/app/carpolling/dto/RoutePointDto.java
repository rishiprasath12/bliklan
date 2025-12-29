package com.app.carpolling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePointDto {
    
    @NotBlank(message = "City is required")
    private String city; // e.g., "Bangalore"
    
    @NotBlank(message = "Sub location is required")
    private String subLocation; // e.g., "Electronic City"
    
    @NotBlank(message = "Address is required")
    private String address; // Full address
    
    @NotNull(message = "Latitude is required")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    private Double longitude;
    
    @NotNull(message = "Sequence order is required")
    private Integer sequenceOrder;
    
    @NotNull(message = "Distance from start is required")
    private Integer distanceFromStart;
    
    @NotNull(message = "Time from start is required")
    private Integer timeFromStart;
    
    private Boolean isBoardingPoint = false;
    
    private Boolean isDropPoint = false;
}

