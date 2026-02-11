package com.footbooking.api.terrain.dto;

import java.math.BigDecimal;
import java.util.List;

public record TerrainDetailDTO(
        Long id,
        String name,
        String city,
        BigDecimal pricePerHour,
        String description,
        String surface,
        Integer capacity,
        BigDecimal rating,
        List<String> imageUrls) {
}
