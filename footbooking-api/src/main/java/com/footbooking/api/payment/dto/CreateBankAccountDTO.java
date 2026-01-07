package com.footbooking.api.payment.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBankAccountDTO(
        @NotBlank(message = "Account holder name is required") String accountHolderName,

        @NotBlank(message = "Bank name is required") String bankName,

        @NotBlank(message = "Account number is required") String accountNumber,

        @NotBlank(message = "RIB is required") String rib,

        String additionalInfo) {
}
