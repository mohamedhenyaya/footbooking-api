package com.footbooking.api.payment.dto;

public record PaymentMethodDTO(
        Long id,
        String cardType,
        String lastFourDigits,
        Integer expiryMonth,
        Integer expiryYear,
        boolean isDefault) {
}
