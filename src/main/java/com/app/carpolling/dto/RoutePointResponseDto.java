package com.app.carpolling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePointResponseDto {
    
    private Long id;
    private Long routeId;
    private String city;
    private String subLocation;
    private String pointName; // "Bangalore - Electronic City"
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer sequenceOrder;
    private Integer distanceFromStart; // in meters
    private Integer timeFromStart; // in minutes
    private Boolean isBoardingPoint;
    private Boolean isDropPoint;
}

