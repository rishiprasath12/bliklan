package com.app.carpolling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePriceCombinationDto {
    
    private String boardingCity;
    private String dropCity;
    private Double price; // null if not set yet
    private Integer estimatedDistance; // in meters
    private Integer estimatedDuration; // in minutes
}

