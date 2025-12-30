package com.footbooking.api.auth.dto;

import java.time.LocalDateTime;

public record UserProfileDTO(
        Long id,
        String email,
        String name,
        String phone,
        String avatar,
        LocalDateTime createdAt,
        Integer score) {
}
