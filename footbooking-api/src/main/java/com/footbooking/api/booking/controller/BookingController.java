package com.footbooking.api.booking.controller;

import com.footbooking.api.booking.dto.BookingRequestDto;
import com.footbooking.api.booking.dto.BookingResponseDto;
import com.footbooking.api.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    @GetMapping("/me")
    public java.util.List<BookingResponseDto> myBookings() {
        return bookingService.getMyBookings();
    }

    @PostMapping
    public BookingResponseDto createBooking(@RequestBody @Valid BookingRequestDto request) {
        return bookingService.createBooking(request);
    }
}
