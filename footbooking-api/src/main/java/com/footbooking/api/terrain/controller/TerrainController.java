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
    public List<TerrainResponseDto> getAllTerrains(@AuthenticationPrincipal UserDetails user) {
        if (user != null) {
            boolean isAdmin = user.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_ADMIN"));
            boolean isSuperAdmin = user.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("SUPERADMIN") || a.getAuthority().equals("ROLE_SUPERADMIN"));

            if (isAdmin && !isSuperAdmin) {
                System.out.println("DEBUG: User " + user.getUsername() + " is ADMIN. Returning owned terrains.");
                return terrainService.getMyTerrains(user.getUsername());
            } else {
                System.out.println("DEBUG: User " + user.getUsername() + " is NOT restricted (Admin=" + isAdmin
                        + ", Super=" + isSuperAdmin + ")");
            }
        }
        return terrainService.getAllTerrains();
    }

    @GetMapping("/my-terrains")
    public List<TerrainResponseDto> getMyTerrains(@AuthenticationPrincipal UserDetails user) {
        return terrainService.getMyTerrains(user.getUsername());
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

    @PostMapping
    public ResponseEntity<TerrainResponseDto> createTerrain(
            @Valid @RequestBody com.footbooking.api.terrain.dto.TerrainRequestDto request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(terrainService.createTerrain(request, user.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TerrainResponseDto> updateTerrain(
            @PathVariable Long id,
            @Valid @RequestBody com.footbooking.api.terrain.dto.TerrainRequestDto request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(terrainService.updateTerrain(id, request, user.getUsername()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTerrain(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        terrainService.deleteTerrain(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/whitelist")
    public ResponseEntity<?> whitelistUserByIdentifier(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal UserDetails user) {
        String identifier = body.get("login");
        if (identifier == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Login (email) is required"));
        }
        terrainService.addUserToWhitelistByIdentifier(id, identifier, user.getUsername());
        return ResponseEntity.ok(Map.of("message", "User added to whitelist successfully"));
    }

    @GetMapping("/{id}/whitelist")
    public ResponseEntity<java.util.Set<com.footbooking.api.booking.dto.UserSummaryDto>> getWhitelist(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(terrainService.getWhitelist(id, user.getUsername()));
    }

    @DeleteMapping("/{id}/whitelist/{identifier}")
    public ResponseEntity<?> removeFromWhitelist(
            @PathVariable Long id,
            @PathVariable String identifier,
            @AuthenticationPrincipal UserDetails user) {
        terrainService.removeUserFromWhitelist(id, identifier, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
