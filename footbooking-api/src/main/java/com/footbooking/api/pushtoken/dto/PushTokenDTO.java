package com.footbooking.api.pushtoken.dto;

import jakarta.validation.constraints.NotBlank;

public record PushTokenDTO(
        @NotBlank(message = "Token is required") String token,

        String deviceType // 'ios' or 'android'
) {
}
