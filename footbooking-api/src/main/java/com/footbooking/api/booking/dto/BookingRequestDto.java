package com.footbooking.api.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingRequestDto(
        @NotNull Long terrainId,
        @NotNull LocalDate date,
        @Min(0) @Max(23) int hour
) {}
