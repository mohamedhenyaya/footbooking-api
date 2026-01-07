package com.footbooking.api.payment.dto;

import java.time.LocalDate;

public record BookingInfoDTO(
        Long bookingId,
        String terrainName,
        LocalDate bookingDate,
        Integer bookingHour,
        String userName,
        String userEmail) {
}
