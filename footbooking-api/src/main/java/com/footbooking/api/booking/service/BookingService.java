package com.footbooking.api.booking.service;

import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.booking.dto.BookingRequestDto;
import com.footbooking.api.booking.dto.BookingResponseDto;
import com.footbooking.api.booking.exception.SlotAlreadyBookedException;
import com.footbooking.api.booking.repository.BookingJdbcRepository;
import com.footbooking.api.terrain.exception.TerrainNotFoundException;
import com.footbooking.api.terrain.repository.TerrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingJdbcRepository bookingJdbcRepository;
    private final TerrainRepository terrainRepository;
    private final UserRepository userRepository;

    public BookingResponseDto createBooking(BookingRequestDto request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email))
                .getId();

        if (!terrainRepository.existsById(request.terrainId())) {
            throw new TerrainNotFoundException(request.terrainId());
        }

        try {
            Long bookingId = bookingJdbcRepository.createBooking(
                    userId,
                    request.terrainId(),
                    request.date(),
                    request.hour()
            );

            return new BookingResponseDto(
                    bookingId,
                    request.terrainId(),
                    request.date(),
                    request.hour()
            );

        } catch (DuplicateKeyException ex) {
            throw new SlotAlreadyBookedException();
        }
    }
    public List<BookingResponseDto> getMyBookings() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email))
                .getId();

        return bookingJdbcRepository.findBookingsByUserId(userId);
    }

}
