package com.footbooking.api.terrain.dto;

import java.time.LocalDate;
import java.util.List;

public record TerrainAvailabilityResponseDto(
        Long terrainId,
        LocalDate date,
        List<Integer> bookedHours,
        List<Integer> availableHours
) {}
