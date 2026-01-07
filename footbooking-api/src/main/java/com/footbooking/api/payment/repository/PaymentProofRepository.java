package com.footbooking.api.payment.repository;

import com.footbooking.api.payment.model.PaymentProof;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentProofRepository extends JpaRepository<PaymentProof, Long> {

    Optional<PaymentProof> findByBookingId(Long bookingId);

    List<PaymentProof> findByValidationStatus(String status);

    boolean existsByBookingId(Long bookingId);
}
