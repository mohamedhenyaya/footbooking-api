package com.footbooking.api.tournament.dto;

import jakarta.validation.constraints.NotBlank;

public record TournamentRegistrationDTO(
        @NotBlank(message = "Team name is required") String teamName) {
}
