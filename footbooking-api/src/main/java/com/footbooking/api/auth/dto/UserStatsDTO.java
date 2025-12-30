package com.footbooking.api.auth.dto;

public record UserStatsDTO(
        Long totalBookings,
        Long matchesPlayed,
        Integer score,
        Integer rank) {
}
