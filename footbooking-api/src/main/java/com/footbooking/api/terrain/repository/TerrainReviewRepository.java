package com.footbooking.api.terrain.repository;

import com.footbooking.api.terrain.model.TerrainReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TerrainReviewRepository extends JpaRepository<TerrainReview, Long> {

    List<TerrainReview> findByTerrainIdOrderByCreatedAtDesc(Long terrainId);

    List<TerrainReview> findByUserId(Long userId);
}
