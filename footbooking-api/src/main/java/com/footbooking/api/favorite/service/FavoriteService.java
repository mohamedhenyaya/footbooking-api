package com.footbooking.api.favorite.service;

import com.footbooking.api.auth.model.User;
import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.favorite.model.Favorite;
import com.footbooking.api.favorite.repository.FavoriteRepository;
import com.footbooking.api.terrain.exception.TerrainNotFoundException;
import com.footbooking.api.terrain.repository.TerrainRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final TerrainRepository terrainRepository;

    public List<Long> getUserFavorites(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return favoriteRepository.findByUserId(user.getId())
                .stream()
                .map(Favorite::getTerrainId)
                .collect(Collectors.toList());
    }

    public void addFavorite(String email, Long terrainId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Verify terrain exists
        if (!terrainRepository.existsById(terrainId)) {
            throw new TerrainNotFoundException(terrainId);
        }

        // Check if already favorited
        if (favoriteRepository.existsByUserIdAndTerrainId(user.getId(), terrainId)) {
            throw new RuntimeException("Terrain already in favorites");
        }

        Favorite favorite = Favorite.builder()
                .userId(user.getId())
                .terrainId(terrainId)
                .createdAt(LocalDateTime.now())
                .build();

        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(String email, Long terrainId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        if (!favoriteRepository.existsByUserIdAndTerrainId(user.getId(), terrainId)) {
            throw new RuntimeException("Terrain not in favorites");
        }

        favoriteRepository.deleteByUserIdAndTerrainId(user.getId(), terrainId);
    }
}
