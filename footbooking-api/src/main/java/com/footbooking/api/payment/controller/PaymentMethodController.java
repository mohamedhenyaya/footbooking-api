package com.footbooking.api.payment.controller;

import com.footbooking.api.payment.dto.PaymentMethodDTO;
import com.footbooking.api.payment.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me/payment-methods")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    public List<PaymentMethodDTO> getPaymentMethods(@AuthenticationPrincipal UserDetails user) {
        return paymentMethodService.getUserPaymentMethods(user.getUsername());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePaymentMethod(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        paymentMethodService.deletePaymentMethod(id, user.getUsername());
        return ResponseEntity.ok(Map.of("message", "Payment method deleted successfully"));
    }
}
