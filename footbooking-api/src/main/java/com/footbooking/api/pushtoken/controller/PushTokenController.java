package com.footbooking.api.pushtoken.controller;

import com.footbooking.api.pushtoken.dto.PushTokenDTO;
import com.footbooking.api.pushtoken.service.PushTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/me/push-token")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PushTokenController {

    private final PushTokenService pushTokenService;

    @PostMapping
    public ResponseEntity<?> registerPushToken(
            @Valid @RequestBody PushTokenDTO request,
            @AuthenticationPrincipal UserDetails user) {
        pushTokenService.registerPushToken(user.getUsername(), request.token(), request.deviceType());
        return ResponseEntity.ok(Map.of("message", "Push token registered successfully"));
    }
}
