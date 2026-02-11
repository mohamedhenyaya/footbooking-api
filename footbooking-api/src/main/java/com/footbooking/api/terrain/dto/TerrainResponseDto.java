package com.footbooking.api.terrain.dto;

import java.math.BigDecimal;
import java.util.List;

public record TerrainResponseDto(
        Long id,
        String name,
        String city,
        BigDecimal pricePerHour,
        List<BankAccountSummaryDto> banques
) {}
