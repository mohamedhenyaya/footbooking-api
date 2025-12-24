package com.footbooking.api.terrain.repository;

import com.footbooking.api.terrain.model.Terrain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerrainRepository extends JpaRepository<Terrain, Long> {
}
