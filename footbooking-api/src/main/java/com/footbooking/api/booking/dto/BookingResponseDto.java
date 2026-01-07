package com.footbooking.api.booking.dto;

import com.footbooking.api.payment.dto.BankAccountDTO;
import java.time.LocalDate;

public record BookingResponseDto(
        Long id,
        Long terrainId,
        String terrainName,
        String city,
        LocalDate date,
        int hour,
        BankAccountDTO bankAccount) {
    public BookingResponseDto(Long id, Long terrainId, LocalDate date, int hour) {
        this(id, terrainId, null, null, date, hour, null);
    }

    public BookingResponseDto(Long id, Long terrainId, String terrainName, String city, LocalDate date, int hour) {
        this(id, terrainId, terrainName, city, date, hour, null);
    }
}
