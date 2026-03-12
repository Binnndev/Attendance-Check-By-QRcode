package com.attendance.backend.auth.dto;

import java.time.Instant;
import java.util.UUID;

public record RegisterResponse(
        String tokenType,
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        UUID sessionId,
        UserSummary user,
        boolean firstLogin
) {
    public record UserSummary(
            UUID id,
            String email,
            String fullName,
            String platformRole
    ) {}
}