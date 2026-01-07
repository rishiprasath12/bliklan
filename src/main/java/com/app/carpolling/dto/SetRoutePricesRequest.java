package com.app.carpolling.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetRoutePricesRequest {
    
    @NotNull(message = "Route ID is required")
    private Long routeId;
    
    @NotEmpty(message = "At least one price entry is required")
    @Valid
    private List<RoutePriceDto> prices; // Price matrix for all boarding-drop combinations
}

