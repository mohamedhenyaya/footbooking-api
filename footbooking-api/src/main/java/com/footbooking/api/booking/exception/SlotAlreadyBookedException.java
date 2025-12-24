package com.footbooking.api.booking.exception;

public class SlotAlreadyBookedException extends RuntimeException {

    public SlotAlreadyBookedException() {
        super("Créneau déjà réservé");
    }
}
