package com.footbooking.api.booking.dto;

import java.time.LocalDateTime;

public record RawBookingSlotDto(
        int hour,
        String status,
        LocalDateTime createdAt) {
}
