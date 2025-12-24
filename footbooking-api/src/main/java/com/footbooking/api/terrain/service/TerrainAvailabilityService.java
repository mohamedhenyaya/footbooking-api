package com.footbooking.api.terrain.service;

import com.footbooking.api.booking.repository.BookingJdbcRepository;
import com.footbooking.api.terrain.dto.TerrainAvailabilityResponseDto;
import com.footbooking.api.terrain.exception.TerrainNotFoundException;
import com.footbooking.api.terrain.repository.TerrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class TerrainAvailabilityService {

    private final TerrainRepository terrainRepository;
    private final BookingJdbcRepository bookingJdbcRepository;

    public TerrainAvailabilityResponseDto getAvailability(Long terrainId, LocalDate date) {
        if (!terrainRepository.existsById(terrainId)) {
            throw new TerrainNotFoundException(terrainId);
        }

        int openHour = 0;   // inclus
        int closeHour = 24; // exclus

        List<Integer> booked = bookingJdbcRepository.findBookedHours(terrainId, date);
        Set<Integer> bookedSet = new HashSet<>(booked);

        List<Integer> available = IntStream.range(openHour, closeHour)
                .filter(h -> !bookedSet.contains(h))
                .boxed()
                .toList();

        return new TerrainAvailabilityResponseDto(terrainId, date, booked, available);
    }
}
