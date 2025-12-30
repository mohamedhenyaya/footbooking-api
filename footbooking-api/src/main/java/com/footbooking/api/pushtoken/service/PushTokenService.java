package com.footbooking.api.pushtoken.service;

import com.footbooking.api.auth.model.User;
import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.pushtoken.model.PushToken;
import com.footbooking.api.pushtoken.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PushTokenService {

    private final PushTokenRepository pushTokenRepository;
    private final UserRepository userRepository;

    public void registerPushToken(String email, String token, String deviceType) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Check if token already exists for this user
        var existingToken = pushTokenRepository.findByUserIdAndToken(user.getId(), token);

        if (existingToken.isPresent()) {
            // Update existing token
            PushToken pushToken = existingToken.get();
            pushToken.setDeviceType(deviceType);
            pushToken.setUpdatedAt(LocalDateTime.now());
            pushTokenRepository.save(pushToken);
        } else {
            // Create new token
            PushToken pushToken = PushToken.builder()
                    .userId(user.getId())
                    .token(token)
                    .deviceType(deviceType)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            pushTokenRepository.save(pushToken);
        }
    }
}
