package com.footbooking.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Le numéro de téléphone ou l'email est requis")
        String email,

        @NotBlank(message = "Le mot de passe est requis")
        String password
) {}