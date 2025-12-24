package com.footbooking.api.booking.dto;

import java.time.LocalDate;

public record BookingResponseDto(
        Long id,
        Long terrainId,
        LocalDate date,
        int hour
) {}
