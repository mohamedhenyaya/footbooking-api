package com.footbooking.api.tournament.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tournaments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;
    private String terrain;
    private Double cost;
    private Double prize;

    private LocalDate startDate;
    private LocalDate endDate;

    private Integer maxTeams;
    private Integer remainingTeams;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String rules;

    private String organizer;

    private String status; // inscription_ouverte, en_cours, terminé

    @Column(name = "image_url")
    private String imageUrl;
}
