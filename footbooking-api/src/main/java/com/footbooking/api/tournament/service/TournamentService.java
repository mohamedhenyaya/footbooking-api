package com.footbooking.api.tournament.service;

import com.footbooking.api.tournament.dto.TournamentDTO;
import com.footbooking.api.tournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;

    public List<TournamentDTO> getAllTournaments() {
        return tournamentRepository.findAll().stream()
                .map(t -> TournamentDTO.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .type(t.getType())
                        .terrain(t.getTerrain())
                        .cost(t.getCost())
                        .prize(t.getPrize())
                        .startDate(t.getStartDate())
                        .endDate(t.getEndDate())
                        .maxTeams(t.getMaxTeams())
                        .remainingTeams(t.getRemainingTeams())
                        .build())
                .toList();
    }
}
