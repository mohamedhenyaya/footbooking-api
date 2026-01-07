package com.footbooking.api.booking.service;

import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.booking.dto.AdminBookingResponseDto;
import com.footbooking.api.booking.dto.BookingRequestDto;
import com.footbooking.api.booking.dto.BookingResponseDto;
import com.footbooking.api.booking.exception.SlotAlreadyBookedException;
import com.footbooking.api.booking.repository.BookingJdbcRepository;
import com.footbooking.api.payment.repository.BankAccountRepository;
import com.footbooking.api.terrain.exception.TerrainNotFoundException;
import com.footbooking.api.terrain.repository.TerrainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingJdbcRepository bookingJdbcRepository;
    private final com.footbooking.api.booking.repository.BookingRepository bookingRepository;
    private final TerrainRepository terrainRepository;
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;

    public BookingResponseDto createBooking(BookingRequestDto request) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        var terrain = terrainRepository.findById(request.terrainId())
                .orElseThrow(() -> new TerrainNotFoundException(request.terrainId()));

        // Check whitelist
        boolean isWhitelisted = terrain.getWhitelist().contains(user);
        String status = isWhitelisted ? "confirmée" : "en_attente";

        try {
            Long bookingId = bookingJdbcRepository.createBooking(
                    user.getId(),
                    request.terrainId(),
                    request.date(),
                    request.hour(),
                    status);

            // Get bank account information if available
            com.footbooking.api.payment.dto.BankAccountDTO bankAccount = null;
            var bankAccountOpt = bankAccountRepository.findByTerrainId(request.terrainId());

            if (bankAccountOpt.isPresent()) {
                var ba = bankAccountOpt.get();
                bankAccount = new com.footbooking.api.payment.dto.BankAccountDTO(
                        ba.getId(),
                        ba.getAccountHolderName(),
                        ba.getBankName(),
                        ba.getAccountNumber(),
                        ba.getRib(),
                        ba.getAdditionalInfo());
            }

            return new BookingResponseDto(
                    bookingId,
                    request.terrainId(),
                    terrain.getName(),
                    terrain.getCity(),
                    request.date(),
                    request.hour(),
                    bankAccount);

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

    public void cancelBooking(Long bookingId) {
        // Restricted to Admin and Superadmin
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("SUPERADMIN") || r.getName().equals("ROLE_SUPERADMIN"));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("ROLE_ADMIN"));

        if (isSuperAdmin) {
            // Superadmin can delete anything.
            // We need a generic delete in repository that ignores ownership/user checks?
            // Currently deleteBooking checks userId. We need a deleteById.
            // Improvised: deleteBookingAsOwner with owner check bypass?
            // Or better: add deleteBookingById(id) to repo.
            // Assuming I need to add it.
            bookingJdbcRepository.deleteBookingAny(bookingId);
            return;
        }

        if (isAdmin) {
            // Admin can cancel if they own the terrain.
            boolean deleted = bookingJdbcRepository.deleteBookingAsOwner(bookingId, user.getId());
            if (!deleted) {
                throw new RuntimeException("Booking not found or you are not the owner of this terrain");
            }
            return;
        }

        throw new RuntimeException("Only Admin or Superadmin can cancel bookings");
    }

    public void adminAcceptBooking(Long bookingId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean updated = bookingJdbcRepository.updateBookingStatus(bookingId, "confirmée", admin.getId());
        if (!updated) {
            throw new RuntimeException("Booking not found or you are not the owner of this terrain");
        }
    }

    public void adminCancelBooking(Long bookingId) {
        cancelBooking(bookingId); // Reuse logic
    }

    public List<AdminBookingResponseDto> getIncomingBookings(java.time.LocalDate date,
            String status) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean isSuperAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("SUPERADMIN") || r.getName().equals("ROLE_SUPERADMIN"));

        List<com.footbooking.api.booking.model.Booking> bookings;

        // Note: the repository uses BookingRepository (JPA interface), not
        // BookingJdbcRepository
        if (isSuperAdmin) {
            bookings = bookingRepository.findAllbookings(date, status);
        } else {
            // For regular admin, find bookings for their terrains
            bookings = bookingRepository.findByTerrainOwnerId(user.getId(), date, status);
        }

        // Map to DTO
        return bookings.stream().map(b -> new AdminBookingResponseDto(
                b.getId(),
                b.getDate(),
                b.getHour(),
                b.getStatus(),
                b.getPaymentStatus(),
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
            // Check superadmin? User request says Admin, implies Owner. Superadmin usually
            // can do all.
            // Staying safe: strictly owner as requested.
            // "Le backend doit empêcher un Admin de réserver un terrain dont il n'est PAS
            // propriétaire"
            // But if I am superadmin, I might want to? Assuming Admin rule for now.
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
                    "confirmée"); // Always confirmed for admin

            return new BookingResponseDto(
                    bookingId,
                    request.terrainId(),
                    request.date(),
                    request.hour());

        } catch (DuplicateKeyException ex) {
            throw new SlotAlreadyBookedException();
        }
    }
}
