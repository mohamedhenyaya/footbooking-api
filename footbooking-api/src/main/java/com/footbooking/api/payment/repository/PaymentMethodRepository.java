package com.footbooking.api.payment.repository;

import com.footbooking.api.payment.model.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    List<PaymentMethod> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);

    Optional<PaymentMethod> findByUserIdAndIsDefault(Long userId, Boolean isDefault);

    List<PaymentMethod> findByUserId(Long userId);
}
