package com.footbooking.api.terrain.dto;

import java.time.LocalDateTime;

public record TerrainReviewDTO(
        Long id,
        String userName,
        Integer rating,
        String comment,
        LocalDateTime createdAt) {
}
