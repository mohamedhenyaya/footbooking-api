package com.footbooking.api.favorite.controller;

import com.footbooking.api.favorite.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me/favorites")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public List<Long> getFavorites(@AuthenticationPrincipal UserDetails user) {
        return favoriteService.getUserFavorites(user.getUsername());
    }

    @PostMapping("/{terrainId}")
    public ResponseEntity<?> addFavorite(
            @PathVariable Long terrainId,
            @AuthenticationPrincipal UserDetails user) {
        favoriteService.addFavorite(user.getUsername(), terrainId);
        return ResponseEntity.ok(Map.of("message", "Terrain added to favorites"));
    }

    @DeleteMapping("/{terrainId}")
    public ResponseEntity<?> removeFavorite(
            @PathVariable Long terrainId,
            @AuthenticationPrincipal UserDetails user) {
        favoriteService.removeFavorite(user.getUsername(), terrainId);
        return ResponseEntity.ok(Map.of("message", "Terrain removed from favorites"));
    }
}
