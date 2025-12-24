package com.footbooking.api.terrain.exception;

public class TerrainNotFoundException extends RuntimeException {
    public TerrainNotFoundException(Long id) {
        super("Terrain not found with id=" + id);
    }
}
