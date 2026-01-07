package com.app.carpolling.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePriceDto {
    
    @NotNull(message = "Boarding city is required")
    private String boardingCity; // City name (e.g., "Bangalore")
    
    @NotNull(message = "Drop city is required")
    private String dropCity; // City name (e.g., "Chennai")
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price; // Fixed price for this boarding-drop combination
}

