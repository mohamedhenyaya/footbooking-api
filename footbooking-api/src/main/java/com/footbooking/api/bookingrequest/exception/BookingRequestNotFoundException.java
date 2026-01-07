package com.footbooking.api.bookingrequest.exception;

public class BookingRequestNotFoundException extends RuntimeException {
    public BookingRequestNotFoundException(String message) {
        super(message);
    }
}
