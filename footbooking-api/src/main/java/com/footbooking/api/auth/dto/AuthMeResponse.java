package com.footbooking.api.auth.dto;

import java.util.List;

public record AuthMeResponse(
                Long id,
                String email,
                String name,
                List<String> roles) {
}
