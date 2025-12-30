package com.footbooking.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "L'identifiant est requis")
        String email,

        @NotBlank(message = "Le mot de passe est requis")
        String password
) {}