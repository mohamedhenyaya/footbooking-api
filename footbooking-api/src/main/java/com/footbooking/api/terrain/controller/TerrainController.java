package com.footbooking.api.terrain.controller;

import com.footbooking.api.terrain.dto.TerrainAvailabilityResponseDto;
import com.footbooking.api.terrain.dto.TerrainResponseDto;
import com.footbooking.api.terrain.service.TerrainAvailabilityService;
import com.footbooking.api.terrain.service.TerrainService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/terrains")
@RequiredArgsConstructor
public class TerrainController {

    private final TerrainService terrainService;
    private final TerrainAvailabilityService terrainAvailabilityService;
    @GetMapping
    public List<TerrainResponseDto> getAllTerrains() {
        return terrainService.getAllTerrains();
    }

    @GetMapping("/{id}")
    public TerrainResponseDto getTerrainById(@PathVariable Long id) {
        return terrainService.getTerrainById(id);
    }
    @GetMapping("/{id}/availability")
    public TerrainAvailabilityResponseDto getAvailability(
            @PathVariable Long id,
            @RequestParam LocalDate date
    ) {
        return terrainAvailabilityService.getAvailability(id, date);
    }

}
