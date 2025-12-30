package com.footbooking.api.auth.controller;

import com.footbooking.api.auth.dto.UserProfileDTO;
import com.footbooking.api.auth.dto.UserStatsDTO;
import com.footbooking.api.auth.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    public UserProfileDTO getProfile(@AuthenticationPrincipal UserDetails user) {
        return userProfileService.getUserProfile(user.getUsername());
    }

    @GetMapping("/stats")
    public UserStatsDTO getStats(@AuthenticationPrincipal UserDetails user) {
        return userProfileService.getUserStats(user.getUsername());
    }
}
