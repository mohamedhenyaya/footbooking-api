package com.footbooking.api.payment.service;

import com.footbooking.api.auth.model.User;
import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.payment.dto.PaymentMethodDTO;
import com.footbooking.api.payment.model.PaymentMethod;
import com.footbooking.api.payment.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;

    public List<PaymentMethodDTO> getUserPaymentMethods(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<PaymentMethod> paymentMethods = paymentMethodRepository
                .findByUserIdOrderByIsDefaultDescCreatedAtDesc(user.getId());

        return paymentMethods.stream()
                .map(pm -> new PaymentMethodDTO(
                        pm.getId(),
                        pm.getCardType(),
                        pm.getLastFourDigits(),
                        pm.getExpiryMonth(),
                        pm.getExpiryYear(),
                        pm.getIsDefault()))
                .collect(Collectors.toList());
    }

    public void deletePaymentMethod(Long paymentMethodId, String email) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Verify the payment method belongs to the user
        if (!paymentMethod.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to payment method");
        }

        paymentMethodRepository.delete(paymentMethod);
    }
}
