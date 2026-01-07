package com.footbooking.api.payment.dto;

import java.time.LocalDateTime;

public record PaymentProofDTO(
        Long id,
        Long bookingId,
        String screenshotUrl,
        String whatsappMessage,
        LocalDateTime submittedAt,
        LocalDateTime validatedAt,
        Long validatedBy,
        String validationStatus,
        String rejectionReason,
        BookingInfoDTO bookingInfo) {
}
