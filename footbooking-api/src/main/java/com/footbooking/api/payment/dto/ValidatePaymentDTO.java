package com.footbooking.api.payment.dto;

import jakarta.validation.constraints.NotNull;

public record ValidatePaymentDTO(
        @NotNull(message = "Approval status is required") Boolean approved,

        String rejectionReason) {
}
