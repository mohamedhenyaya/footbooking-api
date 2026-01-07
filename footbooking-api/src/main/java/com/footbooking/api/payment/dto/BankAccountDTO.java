package com.footbooking.api.payment.dto;

public record BankAccountDTO(
        Long id,
        String accountHolderName,
        String bankName,
        String accountNumber,
        String rib,
        String additionalInfo) {
}
