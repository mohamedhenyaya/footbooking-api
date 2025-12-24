package com.footbooking.api.auth.controller;

import com.footbooking.api.auth.dto.*;
import com.footbooking.api.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public AuthMeResponse me(@AuthenticationPrincipal UserDetails user) {
        return new AuthMeResponse(
                user.getUsername(),
                user.getAuthorities().stream()
                        .map(a -> a.getAuthority())
                        .toList()
        );
    }

}
