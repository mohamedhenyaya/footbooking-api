package com.footbooking.api.notification.controller;

import com.footbooking.api.notification.dto.NotificationDTO;
import com.footbooking.api.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationDTO> getNotifications(@AuthenticationPrincipal UserDetails user) {
        return notificationService.getUserNotifications(user.getUsername());
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        notificationService.markAsRead(id, user.getUsername());
        return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserDetails user) {
        Long count = notificationService.getUnreadCount(user.getUsername());
        return ResponseEntity.ok(Map.of("count", count));
    }
}
