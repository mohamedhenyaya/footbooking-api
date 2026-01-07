package com.footbooking.api.bookingrequest.service;

import com.footbooking.api.booking.model.Booking;
import com.footbooking.api.booking.repository.BookingRepository;
import com.footbooking.api.bookingrequest.dto.*;
import com.footbooking.api.bookingrequest.exception.BookingRequestExpiredException;
import com.footbooking.api.bookingrequest.exception.BookingRequestNotFoundException;
import com.footbooking.api.bookingrequest.model.BookingRequest;
import com.footbooking.api.bookingrequest.model.BookingRequestStatus;
import com.footbooking.api.bookingrequest.repository.BookingRequestRepository;
import com.footbooking.api.notification.service.NotificationService;
import com.footbooking.api.payment.dto.BankAccountDTO;
import com.footbooking.api.payment.model.BankAccount;
import com.footbooking.api.payment.repository.BankAccountRepository;
import com.footbooking.api.terrain.model.Terrain;
import com.footbooking.api.terrain.repository.TerrainRepository;
import com.footbooking.api.auth.model.User;
import com.footbooking.api.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingRequestService {

    private final BookingRequestRepository bookingRequestRepository;
    private final TerrainRepository terrainRepository;
    private final UserRepository userRepository;
    private final BankAccountRepository bankAccountRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    private static final int PAYMENT_DEADLINE_MINUTES = 5;

    @Transactional
    public BookingRequestResponseDTO createBookingRequest(CreateBookingRequestDTO dto, Long userId) {
        // Validate terrain exists
        Terrain terrain = terrainRepository.findById(dto.terrainId())
                .orElseThrow(() -> new RuntimeException("Terrain not found"));

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create booking request
        BookingRequest request = new BookingRequest();
        request.setUser(user);
        request.setTerrain(terrain);
        request.setBookingDate(dto.date());
        request.setBookingHour(dto.hour());
        request.setStatus(BookingRequestStatus.EN_ATTENTE_PAIEMENT);
        request.setDeadline(LocalDateTime.now().plusMinutes(PAYMENT_DEADLINE_MINUTES));
        request.setCreatedAt(LocalDateTime.now());

        request = bookingRequestRepository.save(request);

        log.info("Booking request created: ID={}, User={}, Terrain={}, Date={}, Hour={}",
                request.getId(), userId, dto.terrainId(), dto.date(), dto.hour());

        // Send notification to terrain admin
        notificationService.notifyTerrainAdminNewBookingRequest(terrain, request);

        return toResponseDTO(request);
    }

    @Transactional
    public BookingRequestResponseDTO submitPayment(Long requestId, SubmitPaymentRequestDTO dto, Long userId) {
        BookingRequest request = bookingRequestRepository.findByIdAndUserId(requestId, userId)
                .orElseThrow(() -> new BookingRequestNotFoundException("Booking request not found"));

        // Check if expired
        if (LocalDateTime.now().isAfter(request.getDeadline())) {
            request.setStatus(BookingRequestStatus.EXPIREE);
            bookingRequestRepository.save(request);
            throw new BookingRequestExpiredException("Payment deadline has passed");
        }

        // Check status
        if (request.getStatus() != BookingRequestStatus.EN_ATTENTE_PAIEMENT &&
                request.getStatus() != BookingRequestStatus.EN_ATTENTE_VALIDATION_ADMIN) {
            throw new IllegalStateException("Cannot submit payment for request in status: " + request.getStatus());
        }

        // Update request
        request.setPaymentScreenshotUrl(dto.screenshotUrl());
        request.setWhatsappMessage(dto.whatsappMessage());
        request.setSubmittedAt(LocalDateTime.now());
        request.setStatus(BookingRequestStatus.EN_ATTENTE_VALIDATION_ADMIN);

        request = bookingRequestRepository.save(request);

        log.info("Payment submitted for booking request: ID={}", requestId);

        // Notify admin
        notificationService.notifyAdminPaymentSubmitted(request);

        return toResponseDTO(request);
    }

    @Transactional
    public BookingRequestResponseDTO approveRequest(Long requestId, Long adminId) {
        BookingRequest request = bookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new BookingRequestNotFoundException("Booking request not found"));

        // Check status
        if (request.getStatus() != BookingRequestStatus.EN_ATTENTE_VALIDATION_ADMIN) {
            throw new IllegalStateException("Cannot approve request in status: " + request.getStatus());
        }

        // Get admin user
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        // Verify admin has permission (owns terrain or is superadmin)
        boolean isSuperAdmin = admin.getRoles().stream().anyMatch(r -> r.getName().contains("SUPERADMIN"));
        if (!isSuperAdmin && !request.getTerrain().getOwner().getId().equals(adminId)) {
            throw new AccessDeniedException("You don't have permission to approve this request");
        }

        // Create the actual booking
        Booking booking = new Booking();
        booking.setUser(request.getUser());
        booking.setTerrain(request.getTerrain());
        booking.setDate(request.getBookingDate());
        booking.setHour(request.getBookingHour());
        booking.setStatus("confirmée");
        booking.setPaymentStatus("payé");
        booking.setCreatedAt(LocalDateTime.now());

        try {
            booking = bookingRepository.save(booking);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new com.footbooking.api.booking.exception.SlotAlreadyBookedException();
        }

        // Update request
        request.setStatus(BookingRequestStatus.APPROUVEE);
        request.setApprovedBy(admin);
        request.setApprovedAt(LocalDateTime.now());
        request.setBooking(booking);

        request = bookingRequestRepository.save(request);

        log.info("Booking request approved: ID={}, Booking created: ID={}", requestId, booking.getId());

        // Notify user
        notificationService.notifyUserBookingApproved(request);

        return toResponseDTO(request);
    }

    @Transactional
    public BookingRequestResponseDTO rejectRequest(Long requestId, RejectBookingRequestDTO dto, Long adminId) {
        BookingRequest request = bookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new BookingRequestNotFoundException("Booking request not found"));

        // Check status
        if (request.getStatus() != BookingRequestStatus.EN_ATTENTE_VALIDATION_ADMIN) {
            throw new IllegalStateException("Cannot reject request in status: " + request.getStatus());
        }

        // Get admin user
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        // Verify admin has permission
        boolean isSuperAdmin = admin.getRoles().stream().anyMatch(r -> r.getName().contains("SUPERADMIN"));
        if (!isSuperAdmin && !request.getTerrain().getOwner().getId().equals(adminId)) {
            throw new AccessDeniedException("You don't have permission to reject this request");
        }

        // Update request
        request.setStatus(BookingRequestStatus.REJETEE);
        request.setRejectionReason(dto.reason());
        request.setApprovedBy(admin);
        request.setApprovedAt(LocalDateTime.now());

        request = bookingRequestRepository.save(request);

        log.info("Booking request rejected: ID={}, Reason={}", requestId, dto.reason());

        // Notify user
        notificationService.notifyUserBookingRejected(request);

        return toResponseDTO(request);
    }

    @Transactional(readOnly = true)
    public List<BookingRequestResponseDTO> getPendingRequests(Long adminId, Long terrainId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        boolean isSuperAdmin = admin.getRoles().stream().anyMatch(r -> r.getName().contains("SUPERADMIN"));

        // If not superadmin, filter by admin's terrains
        if (!isSuperAdmin && terrainId == null) {
            // Get all terrains owned by this admin
            List<Terrain> adminTerrains = terrainRepository.findByOwnerId(adminId);
            return adminTerrains.stream()
                    .flatMap(terrain -> bookingRequestRepository
                            .findByTerrainIdAndStatus(terrain.getId(), BookingRequestStatus.EN_ATTENTE_VALIDATION_ADMIN)
                            .stream())
                    .map(this::toResponseDTO)
                    .collect(Collectors.toList());
        }

        return bookingRequestRepository.findPendingRequests(terrainId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingRequestResponseDTO> getMyRequests(Long userId) {
        return bookingRequestRepository.findByUserId(userId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private BookingRequestResponseDTO toResponseDTO(BookingRequest request) {
        // Get bank account if available
        BankAccountDTO bankAccountDTO = null;
        var bankAccountOpt = bankAccountRepository.findByTerrainId(request.getTerrain().getId());
        if (bankAccountOpt.isPresent()) {
            BankAccount ba = bankAccountOpt.get();
            bankAccountDTO = new BankAccountDTO(
                    ba.getId(),
                    ba.getAccountHolderName(),
                    ba.getBankName(),
                    ba.getAccountNumber(),
                    ba.getRib(),
                    ba.getAdditionalInfo());
        }

        return new BookingRequestResponseDTO(
                request.getId(),
                request.getUser().getId(),
                request.getUser().getName(),
                request.getUser().getEmail(),
                request.getUser().getPhone(),
                request.getTerrain().getId(),
                request.getTerrain().getName(),
                request.getTerrain().getCity(),
                request.getBookingDate(),
                request.getBookingHour(),
                request.getStatus().name(),
                request.getPaymentScreenshotUrl(),
                request.getWhatsappMessage(),
                request.getSubmittedAt(),
                request.getDeadline(),
                request.getApprovedBy() != null ? request.getApprovedBy().getId() : null,
                request.getApprovedAt(),
                request.getRejectionReason(),
                request.getBooking() != null ? request.getBooking().getId() : null,
                bankAccountDTO,
                request.getCreatedAt());
    }

    @Transactional
    public BookingRequestResponseDTO cancelRequest(Long requestId, Long userId) {
        BookingRequest request = bookingRequestRepository.findByIdAndUserId(requestId, userId)
                .orElseThrow(() -> new BookingRequestNotFoundException("Booking request not found"));

        // Only allow cancellation if status is pending
        if (request.getStatus() != BookingRequestStatus.EN_ATTENTE_PAIEMENT &&
                request.getStatus() != BookingRequestStatus.EN_ATTENTE_VALIDATION_ADMIN) {
            throw new IllegalStateException("Cannot cancel request in status: " + request.getStatus());
        }

        request.setStatus(BookingRequestStatus.ANNULEE);
        request.setUpdatedAt(LocalDateTime.now());

        request = bookingRequestRepository.save(request);

        log.info("Booking request cancelled by user: ID={}, User={}", requestId, userId);

        return toResponseDTO(request);
    }

    @Transactional
    public BookingRequestResponseDTO cancelOrRejectRequest(Long requestId, Long userId) {
        BookingRequest request = bookingRequestRepository.findById(requestId)
                .orElseThrow(() -> new BookingRequestNotFoundException("Booking request not found"));

        // If user is the creator -> Cancel
        if (request.getUser().getId().equals(userId)) {
            return cancelRequest(requestId, userId);
        }

        // Otherwise -> Try to reject (will check admin permissions inside)
        // We create a dummy reject DTO with a default reason
        RejectBookingRequestDTO rejectDto = new RejectBookingRequestDTO("Cancelled/Rejected by Admin");
        return rejectRequest(requestId, rejectDto, userId);
    }
}
