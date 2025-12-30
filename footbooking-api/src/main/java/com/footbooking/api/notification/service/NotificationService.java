package com.footbooking.api.notification.service;

import com.footbooking.api.auth.model.User;
import com.footbooking.api.auth.repository.UserRepository;
import com.footbooking.api.notification.dto.NotificationDTO;
import com.footbooking.api.notification.model.Notification;
import com.footbooking.api.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationDTO> getUserNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return notifications.stream()
                .map(n -> new NotificationDTO(
                        n.getId(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getIsRead(),
                        n.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public void markAsRead(Long notificationId, String email) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Verify the notification belongs to the user
        if (!notification.getUserId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public Long getUnreadCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return notificationRepository.countByUserIdAndIsRead(user.getId(), false);
    }
}
