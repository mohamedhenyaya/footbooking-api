package com.footbooking.api.bookingrequest.controller;

import com.footbooking.api.bookingrequest.dto.*;
import com.footbooking.api.bookingrequest.service.BookingRequestService;
import com.footbooking.api.auth.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking-requests")
@RequiredArgsConstructor
public class BookingRequestController {

    private final BookingRequestService bookingRequestService;
    private final com.footbooking.api.auth.repository.UserRepository userRepository;

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "User not found"));
    }

    @PostMapping
    public ResponseEntity<BookingRequestResponseDTO> createBookingRequest(
            @RequestBody CreateBookingRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(bookingRequestService.createBookingRequest(dto, user.getId()));
    }

    @PostMapping("/{id}/submit-payment")
    public ResponseEntity<BookingRequestResponseDTO> submitPayment(
            @PathVariable Long id,
            @RequestBody SubmitPaymentRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(bookingRequestService.submitPayment(id, dto, user.getId()));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<BookingRequestResponseDTO> approveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(bookingRequestService.approveRequest(id, user.getId()));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<BookingRequestResponseDTO> rejectRequest(
            @PathVariable Long id,
            @RequestBody RejectBookingRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(bookingRequestService.rejectRequest(id, dto, user.getId()));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<BookingRequestResponseDTO>> getPendingRequests(
            @RequestParam(required = false) Long terrainId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(bookingRequestService.getPendingRequests(user.getId(), terrainId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<BookingRequestResponseDTO>> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(bookingRequestService.getMyRequests(user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelOrRejectRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        BookingRequestResponseDTO responseObj = bookingRequestService.cancelOrRejectRequest(id, user.getId());
        return ResponseEntity.ok(responseObj);
    }
}
