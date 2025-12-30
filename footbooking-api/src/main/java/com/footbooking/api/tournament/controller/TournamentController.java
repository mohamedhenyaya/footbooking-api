package com.footbooking.api.tournament.controller;

import com.footbooking.api.tournament.dto.TournamentDTO;
import com.footbooking.api.tournament.dto.TournamentDetailDTO;
import com.footbooking.api.tournament.dto.TournamentRegistrationDTO;
import com.footbooking.api.tournament.service.TournamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tournaments")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;

    @GetMapping
    public List<TournamentDTO> getAllTournaments() {
        return tournamentService.getAllTournaments();
    }

    @GetMapping("/{id}/details")
    public TournamentDetailDTO getTournamentDetails(@PathVariable Long id) {
        return tournamentService.getTournamentDetails(id);
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<?> registerForTournament(
            @PathVariable Long id,
            @Valid @RequestBody TournamentRegistrationDTO request) {
        tournamentService.registerForTournament(id, request.teamName());
        return ResponseEntity.ok(Map.of("message", "Successfully registered for tournament"));
    }
}
