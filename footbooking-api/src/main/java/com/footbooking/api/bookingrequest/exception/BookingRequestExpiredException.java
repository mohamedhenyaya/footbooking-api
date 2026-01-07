package com.footbooking.api.bookingrequest.exception;

public class BookingRequestExpiredException extends RuntimeException {
    public BookingRequestExpiredException(String message) {
        super(message);
    }
}
