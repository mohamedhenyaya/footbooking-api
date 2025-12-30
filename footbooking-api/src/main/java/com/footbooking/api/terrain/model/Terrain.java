package com.footbooking.api.terrain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "terrain")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Terrain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(name = "price_per_hour", nullable = false)
    private BigDecimal pricePerHour;

    @Column(nullable = false)
    private boolean indoor;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String amenities; // JSON array of amenities

    private String surface; // gazon, synthétique, etc.

    private Integer capacity; // max number of players

    @Column(precision = 3, scale = 2)
    private BigDecimal rating; // average rating

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
