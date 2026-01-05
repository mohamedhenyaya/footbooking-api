package com.footbooking.api.admin.controller;

import com.footbooking.api.booking.dto.AdminBookingResponseDto;
import com.footbooking.api.booking.dto.BookingRequestDto;
import com.footbooking.api.booking.dto.BookingResponseDto;
import com.footbooking.api.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BookingService bookingService;

    @GetMapping("/bookings")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<AdminBookingResponseDto>> getIncomingBookings(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(bookingService.getIncomingBookings(date, status));
    }

    @PostMapping("/bookings")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<BookingResponseDto> createAdminBooking(@RequestBody BookingRequestDto request) {
        return ResponseEntity.ok(bookingService.createAdminBooking(request));
    }
}
