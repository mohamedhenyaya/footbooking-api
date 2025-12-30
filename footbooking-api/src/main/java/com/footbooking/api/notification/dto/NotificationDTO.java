package com.footbooking.api.notification.dto;

import java.time.LocalDateTime;

public record NotificationDTO(
        Long id,
        String title,
        String message,
        boolean isRead,
        LocalDateTime createdAt) {
}
