package com.footbooking.api.payment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_proofs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProof {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false, unique = true)
    private Long bookingId;

    @Column(name = "screenshot_url", nullable = false, columnDefinition = "TEXT")
    private String screenshotUrl;

    @Column(name = "whatsapp_message", columnDefinition = "TEXT")
    private String whatsappMessage;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "validated_by")
    private Long validatedBy;

    @Column(name = "validation_status", nullable = false)
    private String validationStatus = "pending";

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        if (validationStatus == null) {
            validationStatus = "pending";
        }
    }
}
