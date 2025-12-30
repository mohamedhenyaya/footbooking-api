package com.footbooking.api.tournament.repository;

import com.footbooking.api.tournament.model.TournamentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, Long> {

    List<TournamentParticipant> findByTournamentId(Long tournamentId);

    Long countByTournamentId(Long tournamentId);
}
