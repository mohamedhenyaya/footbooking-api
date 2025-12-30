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
        String status,
        BigDecimal totalPrice,
        String paymentStatus,
        LocalDateTime cancellationDeadline,
        LocalDateTime createdAt) {
}
