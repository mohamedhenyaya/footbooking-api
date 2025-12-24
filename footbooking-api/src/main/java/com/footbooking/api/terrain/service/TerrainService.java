package com.footbooking.api.terrain.service;

import com.footbooking.api.terrain.dto.TerrainResponseDto;
import com.footbooking.api.terrain.exception.TerrainNotFoundException;
import com.footbooking.api.terrain.model.Terrain;
import com.footbooking.api.terrain.repository.TerrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TerrainService {

    private final TerrainRepository terrainRepository;

    public List<TerrainResponseDto> getAllTerrains() {
        return terrainRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    private TerrainResponseDto toDto(Terrain terrain) {
        return new TerrainResponseDto(
                terrain.getId(),
                terrain.getName(),
                terrain.getCity(),
                terrain.getPricePerHour(),
                terrain.isIndoor()
        );
    }

    public TerrainResponseDto getTerrainById(Long id) {
        Terrain terrain = terrainRepository.findById(id)
                .orElseThrow(() -> new TerrainNotFoundException(id));
        return toDto(terrain);
    }

}
