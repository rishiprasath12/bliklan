package com.app.carpolling.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteCreationRequest {
    
    @NotNull(message = "Driver ID is required")
    private Long driverId;
    
    @NotBlank(message = "Route name is required")
    private String routeName;
    
    @NotNull(message = "Total distance is required (in meters)")
    private Double totalDistance;
    
    @NotNull(message = "Estimated duration is required (in minutes)")
    private Integer estimatedDuration;
    
    @NotEmpty(message = "At least one city with stops is required")
    @Valid
    private List<CityRouteDto> cities; // List of cities with their stop points
}









