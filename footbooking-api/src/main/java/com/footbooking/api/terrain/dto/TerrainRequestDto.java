package com.footbooking.api.terrain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record TerrainRequestDto(
        @NotBlank(message = "Name is required") String name,

        @NotBlank(message = "City is required") String city,

        @NotNull(message = "Price is required") @Positive(message = "Price must be positive") BigDecimal pricePerHour,

        boolean indoor,

        String description,

        List<String> amenities,

        String surface,

        @Positive(message = "Capacity must be positive") Integer capacity) {
}
