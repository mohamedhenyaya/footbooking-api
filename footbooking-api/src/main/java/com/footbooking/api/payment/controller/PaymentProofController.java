package com.footbooking.api.payment.controller;

import com.footbooking.api.payment.dto.PaymentProofDTO;
import com.footbooking.api.payment.dto.SubmitPaymentProofDTO;
import com.footbooking.api.payment.dto.ValidatePaymentDTO;
import com.footbooking.api.payment.service.PaymentProofService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-proofs")
@RequiredArgsConstructor
public class PaymentProofController {

    private final PaymentProofService paymentProofService;

    @PostMapping
    public ResponseEntity<PaymentProofDTO> submitPaymentProof(
            @Valid @RequestBody SubmitPaymentProofDTO request,
            @AuthenticationPrincipal UserDetails user) {
        PaymentProofDTO result = paymentProofService.submitPaymentProof(request, user.getUsername());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<PaymentProofDTO>> getPendingPaymentProofs() {
        List<PaymentProofDTO> result = paymentProofService.getPendingPaymentProofs();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/validate")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<PaymentProofDTO> validatePaymentProof(
            @PathVariable Long id,
            @Valid @RequestBody ValidatePaymentDTO request,
            @AuthenticationPrincipal UserDetails user) {
        PaymentProofDTO result = paymentProofService.validatePaymentProof(id, request, user.getUsername());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentProofDTO> getPaymentProofByBookingId(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal UserDetails user) {
        PaymentProofDTO result = paymentProofService.getPaymentProofByBookingId(bookingId, user.getUsername());
        return ResponseEntity.ok(result);
    }
}
