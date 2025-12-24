package com.footbooking.api.terrain.dto;

import java.math.BigDecimal;

public record TerrainResponseDto(
        Long id,
        String name,
        String city,
        BigDecimal pricePerHour,
        boolean indoor
) {}
