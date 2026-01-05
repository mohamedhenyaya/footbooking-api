package com.footbooking.api.terrain.repository;

import com.footbooking.api.terrain.model.Terrain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TerrainRepository extends JpaRepository<Terrain, Long> {
    List<Terrain> findByOwnerId(Long ownerId);
}
