package com.footbooking.api.terrain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "terrain_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerrainImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terrain_id", nullable = false)
    private Long terrainId;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
