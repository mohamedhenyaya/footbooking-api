package com.footbooking.api.ranking.controller;

import com.footbooking.api.ranking.dto.UserRankingDTO;
import com.footbooking.api.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping
    public List<UserRankingDTO> getRankings() {
        return rankingService.getRankings();
    }
}
