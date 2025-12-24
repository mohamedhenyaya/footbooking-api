package com.footbooking.api.auth.dto;

import java.util.List;

public record AuthMeResponse(
        String email,
        List<String> roles
) {}
