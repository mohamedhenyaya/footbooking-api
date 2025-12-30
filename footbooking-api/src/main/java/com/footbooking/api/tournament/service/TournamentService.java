package com.footbooking.api.tournament.service;

import com.footbooking.api.tournament.dto.TournamentDTO;
import com.footbooking.api.tournament.dto.TournamentDetailDTO;
import com.footbooking.api.tournament.model.Tournament;
import com.footbooking.api.tournament.model.TournamentParticipant;
import com.footbooking.api.tournament.repository.TournamentParticipantRepository;
import com.footbooking.api.tournament.repository.TournamentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentService {

        private final TournamentRepository tournamentRepository;
        private final TournamentParticipantRepository tournamentParticipantRepository;

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

        public TournamentDetailDTO getTournamentDetails(Long id) {
                Tournament tournament = tournamentRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Tournament not found with id: " + id));

                // Get participants list
                List<String> participants = tournamentParticipantRepository.findByTournamentId(id)
                                .stream()
                                .map(TournamentParticipant::getTeamName)
                                .collect(Collectors.toList());

                return new TournamentDetailDTO(
                                tournament.getId(),
                                tournament.getName(),
                                tournament.getType(),
                                tournament.getTerrain(),
                                tournament.getCost(),
                                tournament.getPrize(),
                                tournament.getStartDate(),
                                tournament.getEndDate(),
                                tournament.getMaxTeams(),
                                tournament.getRemainingTeams(),
                                tournament.getDescription(),
                                tournament.getRules(),
                                tournament.getOrganizer(),
                                tournament.getStatus(),
                                tournament.getImageUrl(),
                                participants);
        }

        public void registerForTournament(Long tournamentId, String teamName) {
                Tournament tournament = tournamentRepository.findById(tournamentId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Tournament not found with id: " + tournamentId));

                // Check if tournament is open for registration
                if (!"inscription_ouverte".equals(tournament.getStatus())) {
                        throw new RuntimeException("Tournament registration is closed");
                }

                // Check if there are remaining slots
                if (tournament.getRemainingTeams() <= 0) {
                        throw new RuntimeException("Tournament is full");
                }

                // Check if team name already exists
                List<TournamentParticipant> existingParticipants = tournamentParticipantRepository
                                .findByTournamentId(tournamentId);
                boolean teamExists = existingParticipants.stream()
                                .anyMatch(p -> p.getTeamName().equalsIgnoreCase(teamName));

                if (teamExists) {
                        throw new RuntimeException("Team name already registered for this tournament");
                }

                // Create participant
                TournamentParticipant participant = TournamentParticipant.builder()
                                .tournamentId(tournamentId)
                                .teamName(teamName)
                                .registeredAt(java.time.LocalDateTime.now())
                                .build();

                tournamentParticipantRepository.save(participant);

                // Update remaining teams
                tournament.setRemainingTeams(tournament.getRemainingTeams() - 1);
                tournamentRepository.save(tournament);
        }
}
