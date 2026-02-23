package com.attendance.backend.auth.dto;

import java.time.Instant;
import java.util.UUID;

public class AuthDtos {

    public record LoginRequest(String email, String password) {}

    public record LoginResponse(
            String tokenType,
            String accessToken,
            Instant accessTokenExpiresAt,
            UserInfo user
    ) {}

    public record UserInfo(
            UUID id,
            String email,
            String fullName,
            String platformRole
    ) {}
}
