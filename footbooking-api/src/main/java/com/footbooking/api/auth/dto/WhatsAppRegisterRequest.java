package com.footbooking.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WhatsAppRegisterRequest(
        @NotBlank String phoneNumber,
        @NotBlank @Size(min = 6, max = 6) String otpCode,
        @NotBlank @Size(min = 6) String password
) {}