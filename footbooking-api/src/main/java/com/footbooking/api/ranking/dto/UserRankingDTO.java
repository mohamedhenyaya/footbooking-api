package com.footbooking.api.ranking.dto;

import lombok.Builder;

@Builder
public record UserRankingDTO(
        Long id,
        String name,
        Integer score) {
}
