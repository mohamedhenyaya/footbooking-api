package com.footbooking.api.booking.dto;

import java.time.LocalDate;

public record AdminBookingResponseDto(
        Long id,
        LocalDate date,
        int hour,
        String status,
        UserSummaryDto user,
        TerrainSummaryDto terrain) {
}
