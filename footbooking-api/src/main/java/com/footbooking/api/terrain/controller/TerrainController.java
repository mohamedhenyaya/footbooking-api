package com.footbooking.api.terrain.controller;

import com.footbooking.api.terrain.dto.CreateReviewDTO;
import com.footbooking.api.terrain.dto.TerrainAvailabilityResponseDto;
import com.footbooking.api.terrain.dto.TerrainDetailDTO;
import com.footbooking.api.terrain.dto.TerrainResponseDto;
import com.footbooking.api.terrain.dto.TerrainReviewDTO;
import com.footbooking.api.terrain.service.TerrainAvailabilityService;
import com.footbooking.api.terrain.service.TerrainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/{id}/details")
    public TerrainDetailDTO getTerrainDetails(@PathVariable Long id) {
        return terrainService.getTerrainDetails(id);
    }

    @GetMapping("/{id}/images")
    public List<String> getTerrainImages(@PathVariable Long id) {
        return terrainService.getTerrainImages(id);
    }

    @GetMapping("/{id}/reviews")
    public List<TerrainReviewDTO> getTerrainReviews(@PathVariable Long id) {
        return terrainService.getTerrainReviews(id);
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> createReview(
            @PathVariable Long id,
            @Valid @RequestBody CreateReviewDTO request,
            @AuthenticationPrincipal UserDetails user) {
        terrainService.createReview(id, user.getUsername(), request.rating(), request.comment());
        return ResponseEntity.ok(Map.of("message", "Review created successfully"));
    }

    @GetMapping("/{id}/availability")
    public TerrainAvailabilityResponseDto getAvailability(
            @PathVariable Long id,
            @RequestParam LocalDate date) {
        return terrainAvailabilityService.getAvailability(id, date);
    }

    @GetMapping("/available")
    public List<TerrainResponseDto> getAvailableTerrains(
            @RequestParam LocalDate date,
            @RequestParam int hour) {
        return terrainService.getAvailableTerrains(date, hour);
    }
}
