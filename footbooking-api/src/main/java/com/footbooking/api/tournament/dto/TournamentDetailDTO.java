package com.footbooking.api.tournament.dto;

import java.time.LocalDate;
import java.util.List;

public record TournamentDetailDTO(
        Long id,
        String name,
        String type,
        String terrain,
        Double cost,
        Double prize,
        LocalDate startDate,
        LocalDate endDate,
        Integer maxTeams,
        Integer remainingTeams,
        String description,
        String rules,
        String organizer,
        String status,
        String imageUrl,
        List<String> participants) {
}
