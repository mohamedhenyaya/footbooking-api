package com.footbooking.api.auth.controller;

import com.footbooking.api.auth.dto.*;
import com.footbooking.api.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public AuthMeResponse me(@AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            throw new RuntimeException("User not authenticated");
        }
        return authService.getMe(user.getUsername());
    }
    // Dans AuthController.java
    @PostMapping("/whatsapp/request-otp")
    public ResponseEntity<?> requestOtp(@RequestBody Map<String, String> payload) {
        String phone = payload.get("phoneNumber"); // Récupère la clé exacte
        authService.sendWhatsAppOtp(phone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register-with-whatsapp")
    public AuthResponse registerWithWhatsApp(@Valid @RequestBody WhatsAppRegisterRequest req) {
        return authService.registerWithWhatsApp(req);
    }

    @PostMapping("/whatsapp/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody WhatsAppRegisterRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.ok(Map.of("message", "Votre mot de passe a été modifié avec succès."));
    }

    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody CreateAdminRequest req) {
        authService.createAdmin(req);
        return ResponseEntity.ok(Map.of("message", "Admin created successfully"));
    }
}
