package com.footbooking.api.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubmitPaymentProofDTO(
        @NotNull(message = "Booking ID is required") Long bookingId,

        @NotBlank(message = "Screenshot URL is required") String screenshotUrl,

        String whatsappMessage) {
}
