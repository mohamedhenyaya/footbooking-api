package com.footbooking.api.bookingrequest.dto;

import java.time.LocalDate;

public record CreateBookingRequestDTO(
        Long terrainId,
        LocalDate date,
        Integer hour) {
}
