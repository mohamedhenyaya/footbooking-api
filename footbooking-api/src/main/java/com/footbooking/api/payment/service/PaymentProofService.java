package com.footbooking.api.payment.service;

import com.footbooking.api.auth.model.User;
import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.notification.service.NotificationService;
import com.footbooking.api.payment.dto.BookingInfoDTO;
import com.footbooking.api.payment.dto.PaymentProofDTO;
import com.footbooking.api.payment.dto.SubmitPaymentProofDTO;
import com.footbooking.api.payment.dto.ValidatePaymentDTO;

import com.footbooking.api.payment.exception.PaymentProofNotFoundException;
import com.footbooking.api.payment.model.PaymentProof;
import com.footbooking.api.payment.repository.PaymentProofRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentProofService {

    private final PaymentProofRepository paymentProofRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final NotificationService notificationService;

    public PaymentProofDTO submitPaymentProof(SubmitPaymentProofDTO dto, String userEmail) {
        // Verify user exists
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        // Verify booking exists and belongs to user
        String checkBookingSql = """
                SELECT COUNT(*) FROM bookings
                WHERE id = ? AND user_id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(checkBookingSql, Integer.class, dto.bookingId(), user.getId());

        if (count == null || count == 0) {
            throw new RuntimeException("Booking not found or does not belong to you");
        }

        // Check if payment proof already exists
        if (paymentProofRepository.existsByBookingId(dto.bookingId())) {
            throw new RuntimeException("Payment proof already submitted for this booking");
        }

        // Create payment proof
        PaymentProof paymentProof = new PaymentProof();
        paymentProof.setBookingId(dto.bookingId());
        paymentProof.setScreenshotUrl(dto.screenshotUrl());
        paymentProof.setWhatsappMessage(dto.whatsappMessage());
        paymentProof.setValidationStatus("pending");

        PaymentProof saved = paymentProofRepository.save(paymentProof);

        // Update booking payment status to 'en_attente_validation'
        String updateBookingSql = """
                UPDATE bookings
                SET payment_status = 'en_attente_validation'
                WHERE id = ?
                """;
        jdbcTemplate.update(updateBookingSql, dto.bookingId());

        // Create notification for superadmins
        notificationService.createNotificationForSuperadmins(
                "Nouveau paiement à valider",
                "Un utilisateur a soumis une preuve de paiement pour la réservation #" + dto.bookingId());

        return convertToDTO(saved);
    }

    public List<PaymentProofDTO> getPendingPaymentProofs() {
        List<PaymentProof> proofs = paymentProofRepository.findByValidationStatus("pending");
        return proofs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PaymentProofDTO getPaymentProofByBookingId(Long bookingId, String userEmail) {
        PaymentProof proof = paymentProofRepository.findByBookingId(bookingId)
                .orElseThrow(
                        () -> new PaymentProofNotFoundException("No payment proof found for booking ID: " + bookingId));

        // Verify user owns the booking or is admin/superadmin
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().contains("ADMIN") || r.getName().contains("SUPERADMIN"));

        if (!isAdmin) {
            // Check if booking belongs to user
            String checkBookingSql = """
                    SELECT COUNT(*) FROM bookings
                    WHERE id = ? AND user_id = ?
                    """;
            Integer count = jdbcTemplate.queryForObject(checkBookingSql, Integer.class, bookingId, user.getId());

            if (count == null || count == 0) {
                throw new RuntimeException("Unauthorized access to payment proof");
            }
        }

        return convertToDTO(proof);
    }

    public PaymentProofDTO validatePaymentProof(Long proofId, ValidatePaymentDTO dto, String superadminEmail) {
        // Verify superadmin
        User superadmin = userRepository.findByEmail(superadminEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + superadminEmail));

        boolean isSuperAdmin = superadmin.getRoles().stream()
                .anyMatch(r -> r.getName().contains("SUPERADMIN"));

        if (!isSuperAdmin) {
            throw new RuntimeException("Unauthorized: Only superadmins can validate payment proofs");
        }

        // Get payment proof
        PaymentProof proof = paymentProofRepository.findById(proofId)
                .orElseThrow(() -> new PaymentProofNotFoundException(proofId));

        // Update validation status
        proof.setValidationStatus(dto.approved() ? "approved" : "rejected");
        proof.setValidatedAt(LocalDateTime.now());
        proof.setValidatedBy(superadmin.getId());

        if (!dto.approved() && dto.rejectionReason() != null) {
            proof.setRejectionReason(dto.rejectionReason());
        }

        PaymentProof updated = paymentProofRepository.save(proof);

        // Update booking payment status
        String newPaymentStatus = dto.approved() ? "payé" : "non_payé";
        String updateBookingSql = """
                UPDATE bookings
                SET payment_status = ?
                WHERE id = ?
                """;
        jdbcTemplate.update(updateBookingSql, newPaymentStatus, proof.getBookingId());

        // Get user ID from booking
        String getUserIdSql = """
                SELECT user_id FROM bookings WHERE id = ?
                """;
        Long userId = jdbcTemplate.queryForObject(getUserIdSql, Long.class, proof.getBookingId());

        // Create notification for user
        if (dto.approved()) {
            notificationService.createNotificationForUser(
                    userId,
                    "Paiement validé",
                    "Votre paiement pour la réservation #" + proof.getBookingId() + " a été validé avec succès.");
        } else {
            String message = "Votre paiement pour la réservation #" + proof.getBookingId() + " a été rejeté.";
            if (dto.rejectionReason() != null) {
                message += " Raison: " + dto.rejectionReason();
            }
            notificationService.createNotificationForUser(userId, "Paiement rejeté", message);
        }

        return convertToDTO(updated);
    }

    private PaymentProofDTO convertToDTO(PaymentProof proof) {
        // Get booking info
        String bookingInfoSql = """
                SELECT b.id, t.name as terrain_name, b.booking_date, b.booking_hour,
                       u.name as user_name, u.email as user_email
                FROM bookings b
                JOIN terrain t ON b.terrain_id = t.id
                JOIN users u ON b.user_id = u.id
                WHERE b.id = ?
                """;

        BookingInfoDTO bookingInfo = jdbcTemplate.queryForObject(bookingInfoSql, (rs, rowNum) -> new BookingInfoDTO(
                rs.getLong("id"),
                rs.getString("terrain_name"),
                rs.getDate("booking_date").toLocalDate(),
                rs.getInt("booking_hour"),
                rs.getString("user_name"),
                rs.getString("user_email")), proof.getBookingId());

        return new PaymentProofDTO(
                proof.getId(),
                proof.getBookingId(),
                proof.getScreenshotUrl(),
                proof.getWhatsappMessage(),
                proof.getSubmittedAt(),
                proof.getValidatedAt(),
                proof.getValidatedBy(),
                proof.getValidationStatus(),
                proof.getRejectionReason(),
                bookingInfo);
    }
}
