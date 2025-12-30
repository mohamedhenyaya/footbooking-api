package com.footbooking.api.booking.dto;

import java.time.LocalDate;

public record BookingResponseDto(
        Long id,
        Long terrainId,
        String terrainName,
        String city,
        LocalDate date,
        int hour
) {
    public BookingResponseDto(Long id, Long terrainId, LocalDate date, int hour) {
        this(id, terrainId, null, null, date, hour);
    }
}