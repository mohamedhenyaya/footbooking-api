package com.footbooking.api.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingHistoryDTO(
        Long id,
        Long terrainId,
        String terrainName,
        String city,
        LocalDate date,
        int hour,
        BigDecimal totalPrice,
        LocalDateTime cancellationDeadline,
        LocalDateTime createdAt) {
}
