package com.footbooking.api.payment.exception;

public class PaymentProofNotFoundException extends RuntimeException {
    public PaymentProofNotFoundException(Long id) {
        super("Payment proof not found with ID: " + id);
    }

    public PaymentProofNotFoundException(String message) {
        super(message);
    }
}
