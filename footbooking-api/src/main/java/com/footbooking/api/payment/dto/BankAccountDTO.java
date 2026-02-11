package com.footbooking.api.payment.dto;

public record BankAccountDTO(
        Long id,
        Long terrainId,
        String bankName,
        String accountNumber,
        String additionalInfo) {
}
