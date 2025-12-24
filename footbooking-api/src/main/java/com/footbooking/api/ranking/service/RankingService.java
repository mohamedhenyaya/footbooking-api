package com.footbooking.api.ranking.service;

import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.ranking.dto.UserRankingDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final UserRepository userRepository;

    public List<UserRankingDTO> getRankings() {
        return userRepository.findAllByOrderByScoreDesc().stream()
                .map(user -> UserRankingDTO.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .score(user.getScore())
                        .build())
                .toList();
    }
}
