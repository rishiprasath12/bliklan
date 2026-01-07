package com.app.carpolling.dto;

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
public class BookingRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Trip ID is required")
    private Long tripId;
    
    @NotBlank(message = "Boarding city is required")
    private String boardingCity; // e.g., "Bangalore"
    
    @NotBlank(message = "Boarding sub-location is required")
    private String boardingSubLocation; // e.g., "Silk Board"
    
    @NotBlank(message = "Drop city is required")
    private String dropCity; // e.g., "Chennai"
    
    @NotBlank(message = "Drop sub-location is required")
    private String dropSubLocation; // e.g., "T Nagar"
    
    @NotEmpty(message = "Seat numbers are required")
    private List<String> seatNumbers;
    
    private String passengerNames;
    
    private String passengerContacts;
}









