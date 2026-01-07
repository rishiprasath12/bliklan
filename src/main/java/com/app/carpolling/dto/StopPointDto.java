package com.app.carpolling.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopPointDto {
    
    @NotBlank(message = "Sub location is required")
    private String subLocation; // e.g., "Electronic City", "Silk Board"
    
    @NotBlank(message = "Address is required")
    private String address; // Full address
    
    @NotNull(message = "Latitude is required")
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    private Double longitude;
    
    @NotNull(message = "Distance from start is required (in meters)")
    private Integer distanceFromStart; // in meters
    
    @NotNull(message = "Time from start is required (in minutes)")
    private Integer timeFromStart; // in minutes
}

