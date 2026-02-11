package com.footbooking.api.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBankAccountDTO(
        @NotNull(message = "Terrain ID is required") Long terrainId,

        @NotBlank(message = "Bank name is required") String bankName,

        @NotBlank(message = "Account number is required") String accountNumber,

        String additionalInfo) {
}
