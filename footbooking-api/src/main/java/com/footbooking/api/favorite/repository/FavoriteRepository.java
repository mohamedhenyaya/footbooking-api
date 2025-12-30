package com.footbooking.api.favorite.repository;

import com.footbooking.api.favorite.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserId(Long userId);

    Optional<Favorite> findByUserIdAndTerrainId(Long userId, Long terrainId);

    boolean existsByUserIdAndTerrainId(Long userId, Long terrainId);

    void deleteByUserIdAndTerrainId(Long userId, Long terrainId);
}
