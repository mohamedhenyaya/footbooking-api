package com.footbooking.api.auth.service;

import com.footbooking.api.auth.dto.UserProfileDTO;
import com.footbooking.api.auth.dto.UserStatsDTO;
import com.footbooking.api.auth.model.User;
import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.booking.repository.BookingJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final BookingJdbcRepository bookingRepository;

    public UserProfileDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new UserProfileDTO(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getAvatar(),
                user.getCreatedAt(),
                user.getScore());
    }

    public UserStatsDTO getUserStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Get total bookings count
        Long totalBookings = (long) bookingRepository.findBookingsByUserId(user.getId()).size();

        // For now, matches played equals total bookings
        Long matchesPlayed = totalBookings;

        // Get user rank based on score
        Integer rank = calculateUserRank(user.getId(), user.getScore());

        return new UserStatsDTO(
                totalBookings,
                matchesPlayed,
                user.getScore(),
                rank);
    }

    private Integer calculateUserRank(Long userId, Integer score) {
        // Count how many users have a higher score
        Long higherScoreCount = userRepository.countByScoreGreaterThan(score);
        return higherScoreCount.intValue() + 1;
    }
}
