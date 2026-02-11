package com.footbooking.api.terrain.service;

import com.footbooking.api.booking.repository.BookingJdbcRepository;
import com.footbooking.api.terrain.dto.TerrainAvailabilityResponseDto;
import com.footbooking.api.terrain.exception.TerrainNotFoundException;
import com.footbooking.api.terrain.repository.TerrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

        List<BookingJdbcRepository.BookedSlotRecord> rawSlots =
                bookingJdbcRepository.findBookedSlotsDetails(terrainId, date);

        Set<Integer> bookedSet = rawSlots.stream()
                .map(BookingJdbcRepository.BookedSlotRecord::hour)
                .collect(Collectors.toSet());

        List<Integer> available = IntStream.range(0, 24)
                .filter(h -> !bookedSet.contains(h))
                .boxed()
                .toList();

        List<Integer> bookedHours = bookedSet.stream().sorted().toList();

        return new TerrainAvailabilityResponseDto(terrainId, date, bookedHours, available);
    }
}