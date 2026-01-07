package com.footbooking.api.payment.exception;

public class BankAccountNotFoundException extends RuntimeException {
    public BankAccountNotFoundException(Long terrainId) {
        super("Bank account not found for terrain ID: " + terrainId);
    }
}
