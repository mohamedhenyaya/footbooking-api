package com.footbooking.api.tournament.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record TournamentDTO(
        Long id,
        String name,
        String type,
        String terrain,
        Double cost,
        Double prize,
        LocalDate startDate,
        LocalDate endDate,
        Integer maxTeams,
        Integer remainingTeams) {
}
