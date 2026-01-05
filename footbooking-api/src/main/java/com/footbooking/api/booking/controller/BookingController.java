package com.footbooking.api.booking.controller;

import com.footbooking.api.booking.dto.BookingRequestDto;
import com.footbooking.api.booking.dto.BookingResponseDto;
import com.footbooking.api.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return org.springframework.http.ResponseEntity
                .ok(java.util.Map.of("message", "Booking cancelled successfully"));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<?> acceptBooking(@PathVariable Long id) {
        bookingService.adminAcceptBooking(id);
        return ResponseEntity.ok(java.util.Map.of("message", "Booking accepted successfully"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> adminCancelBooking(@PathVariable Long id) {
        bookingService.adminCancelBooking(id);
        return ResponseEntity.ok(java.util.Map.of("message", "Booking cancelled by admin successfully"));
    }
}
