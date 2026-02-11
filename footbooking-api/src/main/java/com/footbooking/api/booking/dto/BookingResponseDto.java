package com.footbooking.api.booking.dto;

import com.footbooking.api.payment.dto.BankAccountDTO;
import java.time.LocalDate;
import java.util.List;

public record BookingResponseDto(
        Long id,
        Long terrainId,
        String terrainName,
        String city,
        LocalDate date,
        int hour,
        String moovMoneyNumber,
        List<BankAccountDTO> bankAccounts) {
    public BookingResponseDto(Long id, Long terrainId, LocalDate date, int hour, String moovMoneyNumber) {
        this(id, terrainId, null, null, date, hour, moovMoneyNumber, null);
    }

    // Constructeur intermédiaire
    public BookingResponseDto(Long id, Long terrainId, String terrainName, String city, LocalDate date, int hour, String moovMoneyNumber) {
        this(id, terrainId, terrainName, city, date, hour, moovMoneyNumber, null);
    }
}
