package com.app.carpolling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverLocationDto implements Serializable {
    
    private Long driverId;
    private Long tripId;
    private Double latitude;
    private Double longitude;
    private Double heading;        // Direction in degrees (0-360)
    private Double speed;          // Speed in km/h
    private Double accuracy;       // GPS accuracy in meters
    private LocalDateTime timestamp;
    private String status;         // MOVING, STOPPED, ARRIVED
}
