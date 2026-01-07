package com.footbooking.api.bookingrequest.dto;

import com.footbooking.api.payment.dto.BankAccountDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingRequestResponseDTO(
        Long id,
        Long userId,
        String userName,
        Long terrainId,
        String terrainName,
        String city,
        LocalDate date,
        Integer hour,
        String status,
        String paymentScreenshotUrl,
        String whatsappMessage,
        LocalDateTime submittedAt,
        LocalDateTime deadline,
        Long approvedBy,
        LocalDateTime approvedAt,
        String rejectionReason,
        Long bookingId,
        BankAccountDTO bankAccount,
        LocalDateTime createdAt) {
}
