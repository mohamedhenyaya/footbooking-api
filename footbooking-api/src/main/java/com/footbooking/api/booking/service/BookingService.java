package com.footbooking.api.booking.service;

import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.booking.dto.AdminBookingResponseDto;
import com.footbooking.api.booking.dto.BookingRequestDto;
import com.footbooking.api.booking.dto.BookingResponseDto;
import com.footbooking.api.booking.exception.SlotAlreadyBookedException;
import com.footbooking.api.booking.repository.BookingJdbcRepository;
import com.footbooking.api.payment.dto.BankAccountDTO; // Import
import com.footbooking.api.payment.repository.BankAccountRepository;
import com.footbooking.api.terrain.exception.TerrainNotFoundException;
import com.footbooking.api.terrain.repository.TerrainRepository;
import com.footbooking.api.terrain.service.TerrainAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingJdbcRepository bookingJdbcRepository;
    private final com.footbooking.api.booking.repository.BookingRepository bookingRepository;
    private final TerrainRepository terrainRepository;
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final TerrainAvailabilityService terrainAvailabilityService;

    public BookingResponseDto createBooking(BookingRequestDto request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        var availability = terrainAvailabilityService.getAvailability(request.terrainId(), request.date());
        if (!availability.availableHours().contains(request.hour())) {
            throw new SlotAlreadyBookedException();
        }

        var terrain = terrainRepository.findById(request.terrainId())
                .orElseThrow(() -> new TerrainNotFoundException(request.terrainId()));

        try {
            Long bookingId = bookingJdbcRepository.createBooking(
                    user.getId(),
                    request.terrainId(),
                    request.date(),
                    request.hour(),
                    request.moovMoneyNumber()
                    );

            List<BankAccountDTO> bankAccountDTOs = bankAccountRepository.findByTerrainId(request.terrainId())
                    .stream()
                    .map(ba -> new BankAccountDTO(
                            ba.getId(),
                            ba.getTerrainId(),
                            ba.getBankName(),
                            ba.getAccountNumber(),
                            ba.getAdditionalInfo()))
                    .collect(Collectors.toList());
            // ------------------------------------------------------------------

            // Increment user score
            user.setScore(user.getScore() + 1);
            userRepository.save(user);

            return new BookingResponseDto(
                    bookingId,
                    request.terrainId(),
                    terrain.getName(),
                    terrain.getCity(),
                    request.date(),
                    request.hour(),
                    request.moovMoneyNumber(),
                    bankAccountDTOs); // On passe la liste

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

    public List<AdminBookingResponseDto> getIncomingBookings(java.time.LocalDate date) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("SUPERADMIN") || r.getName().equals("ROLE_SUPERADMIN"));

        List<com.footbooking.api.booking.model.Booking> bookings;

        if (isSuperAdmin) {
            bookings = bookingRepository.findAllbookings(date);
        } else {
            bookings = bookingRepository.findByTerrainOwnerId(user.getId(), date);
        }

        return bookings.stream().map(b -> new AdminBookingResponseDto(
                        b.getId(),
                        b.getDate(),
                        b.getHour(),
                        new com.footbooking.api.booking.dto.UserSummaryDto(
                                b.getUser().getName(),
                                b.getUser().getEmail(),
                                b.getUser().getPhone()),
                        new com.footbooking.api.booking.dto.TerrainSummaryDto(
                                b.getTerrain().getId(),
                                b.getTerrain().getName())))
                .toList();
    }

    public BookingResponseDto createAdminBooking(BookingRequestDto request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var terrain = terrainRepository.findById(request.terrainId())
                .orElseThrow(() -> new TerrainNotFoundException(request.terrainId()));

        if (terrain.getOwner() == null || !terrain.getOwner().getId().equals(admin.getId())) {
            boolean isSuper = admin.getRoles().stream().anyMatch(r -> r.getName().contains("SUPERADMIN"));
            if (!isSuper) {
                throw new RuntimeException("Unauthorized: You do not own this terrain");
            }
        }

        try {
            Long bookingId = bookingJdbcRepository.createBooking(
                    admin.getId(),
                    request.terrainId(),
                    request.date(),
                    request.hour(),
                    request.moovMoneyNumber()
            );

            // Pour l'admin, on peut renvoyer la réponse sans les banques (ou avec, au choix)
            // Ici j'utilise le constructeur simplifié qui mettra les banques à null
            return new BookingResponseDto(
                    bookingId,
                    request.terrainId(),
                    terrain.getName(),
                    terrain.getCity(),
                    request.date(),
                    request.hour(),
                    request.moovMoneyNumber(),
                    null);

        } catch (DuplicateKeyException ex) {
            throw new SlotAlreadyBookedException();
        }
    }
}