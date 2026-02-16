package com.app.carpolling.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocationResponseDto {

    private Double latitude;
    private Double longitude;
    private Long timestamp; // Server timestamp when location was last updated
}
