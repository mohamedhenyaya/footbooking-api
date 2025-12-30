package com.footbooking.api.terrain.repository;

import com.footbooking.api.terrain.model.TerrainImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TerrainImageRepository extends JpaRepository<TerrainImage, Long> {

    List<TerrainImage> findByTerrainIdOrderByIsPrimaryDescCreatedAtAsc(Long terrainId);

    List<TerrainImage> findByTerrainId(Long terrainId);
}
