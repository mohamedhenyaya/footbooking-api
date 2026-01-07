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

                List<Notification> notifications = notificationRepository
                                .findByUserIdOrderByCreatedAtDesc(user.getId());

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

        public void createNotificationForSuperadmins(String title, String message) {
                // Find all superadmins
                List<User> superadmins = userRepository.findAll().stream()
                                .filter(user -> user.getRoles().stream()
                                                .anyMatch(role -> role.getName().contains("SUPERADMIN")))
                                .toList();

                // Create notification for each superadmin
                for (User superadmin : superadmins) {
                        Notification notification = Notification.builder()
                                        .userId(superadmin.getId())
                                        .title(title)
                                        .message(message)
                                        .isRead(false)
                                        .createdAt(java.time.LocalDateTime.now())
                                        .build();
                        notificationRepository.save(notification);
                }
        }

        public void createNotificationForUser(Long userId, String title, String message) {
                Notification notification = Notification.builder()
                                .userId(userId)
                                .title(title)
                                .message(message)
                                .isRead(false)
                                .createdAt(java.time.LocalDateTime.now())
                                .build();
                notificationRepository.save(notification);
        }

        // Booking request notifications
        public void notifyTerrainAdminNewBookingRequest(com.footbooking.api.terrain.model.Terrain terrain,
                        com.footbooking.api.bookingrequest.model.BookingRequest request) {
                Long adminId = terrain.getOwner().getId();
                String title = "Nouvelle demande de réservation";
                String message = String.format("Nouvelle demande pour %s le %s à %dh par %s",
                                terrain.getName(), request.getBookingDate(), request.getBookingHour(),
                                request.getUser().getName());
                createNotificationForUser(adminId, title, message);
        }

        public void notifyAdminPaymentSubmitted(com.footbooking.api.bookingrequest.model.BookingRequest request) {
                Long adminId = request.getTerrain().getOwner().getId();
                String title = "Preuve de paiement soumise";
                String message = String.format("Preuve de paiement soumise pour la demande #%d - %s",
                                request.getId(), request.getTerrain().getName());
                createNotificationForUser(adminId, title, message);
        }

        public void notifyUserBookingApproved(com.footbooking.api.bookingrequest.model.BookingRequest request) {
                String title = "Réservation confirmée !";
                String message = String.format("Votre réservation pour %s le %s à %dh a été confirmée",
                                request.getTerrain().getName(), request.getBookingDate(), request.getBookingHour());
                createNotificationForUser(request.getUser().getId(), title, message);
        }

        public void notifyUserBookingRejected(com.footbooking.api.bookingrequest.model.BookingRequest request) {
                String title = "Demande de réservation rejetée";
                String message = String.format("Votre demande pour %s le %s à %dh a été rejetée. Raison: %s",
                                request.getTerrain().getName(), request.getBookingDate(), request.getBookingHour(),
                                request.getRejectionReason() != null ? request.getRejectionReason() : "Non spécifiée");
                createNotificationForUser(request.getUser().getId(), title, message);
        }
}
