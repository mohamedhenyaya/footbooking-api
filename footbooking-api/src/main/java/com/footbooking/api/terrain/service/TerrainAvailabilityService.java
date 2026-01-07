package com.footbooking.api.terrain.service;

import com.footbooking.api.booking.dto.RawBookingSlotDto;
import com.footbooking.api.booking.repository.BookingJdbcRepository;
import com.footbooking.api.terrain.dto.BookedSlotDto;
import com.footbooking.api.terrain.dto.TerrainAvailabilityResponseDto;
import com.footbooking.api.terrain.exception.TerrainNotFoundException;
import com.footbooking.api.terrain.repository.TerrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

        int openHour = 0; // inclus
        int closeHour = 24; // exclus

        List<RawBookingSlotDto> rawSlots = bookingJdbcRepository.findBookedSlotsDetails(terrainId, date);

        List<Integer> bookedHours = new ArrayList<>();
        List<BookedSlotDto> bookedSlots = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();

        for (RawBookingSlotDto slot : rawSlots) {
            String statusUpper = slot.status().toUpperCase();
            String mappedStatus = null;

            if ("CONFIRMED".equals(statusUpper) || "APPROUVEE".equals(statusUpper)) {
                mappedStatus = "CONFIRMED";
            } else if ("EN_ATTENTE_VALIDATION_ADMIN".equals(statusUpper)) {
                mappedStatus = "WAITING_ADMIN";
            } else if ("EN_ATTENTE_PAIEMENT".equals(statusUpper)) {
                // Check timeout: 15 minutes
                if (slot.createdAt() != null && slot.createdAt().plusMinutes(15).isBefore(now)) {
                    // Timeout -> Slot is free
                    continue;
                }
                mappedStatus = "WAITING_PROOF";
            } else {
                // Fallback for unexpected status (e.g. invalid data), treat as booked/confirmed
                // for safety
                // OR ignore? Let's treat as CONFIRMED to be safe against double booking?
                // actually 'rejetee' or 'expiree' shouldn't be here due to query filter.
                // But let's log or ignore.
                // Existing query filters IN ('EN_ATTENTE_PAIEMENT',
                // 'EN_ATTENTE_VALIDATION_ADMIN')
                // So this branch should be unreachable except for 'CONFIRMED'.
                if (!"CONFIRMED".equals(statusUpper)) {
                    // Probably shouldn't happen given the query.
                    continue;
                }
                mappedStatus = "CONFIRMED";
            }

            if (mappedStatus != null) {
                bookedHours.add(slot.hour());
                bookedSlots.add(new BookedSlotDto(slot.hour(), mappedStatus));
            }
        }

        Set<Integer> bookedSet = new HashSet<>(bookedHours);

        List<Integer> available = IntStream.range(openHour, closeHour)
                .filter(h -> !bookedSet.contains(h))
                .boxed()
                .toList();

        return new TerrainAvailabilityResponseDto(terrainId, date, bookedHours, available, bookedSlots);
    }
}
