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
public class CityRouteDto {
    
    @NotBlank(message = "City name is required")
    private String city; // e.g., "Bangalore", "Chennai"
    
    @NotEmpty(message = "At least one stop point is required for the city")
    @Valid
    private List<StopPointDto> points; // List of stop points in this city
    
    @NotNull(message = "City sequence order is required")
    private Integer sequenceOrder; // Order in which cities appear in route (1, 2, 3...)
    
    @NotNull(message = "Boarding point flag is required")
    private Boolean isBoardingPoint; // true = passengers can board at any stop in this city
    
    @NotNull(message = "Drop point flag is required")
    private Boolean isDropPoint; // true = passengers can drop at any stop in this city
}

