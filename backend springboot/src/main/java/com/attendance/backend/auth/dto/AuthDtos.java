package com.attendance.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public class AuthDtos {

    public record LoginRequest(
            @NotBlank(message = "email is required")
            @Email(message = "email is invalid")
            @Size(max = 190, message = "email must be at most 190 characters")
            String email,

            @NotBlank(message = "password is required")
            @Size(min = 8, max = 200, message = "password must be between 8 and 200 characters")
            String password,

            @Size(max = 120, message = "deviceId must be at most 120 characters")
            String deviceId
    ) {}

    public record RefreshRequest(
            @NotBlank(message = "refreshToken is required")
            String refreshToken
    ) {}

    public record LogoutRequest(
            @NotBlank(message = "refreshToken is required")
            String refreshToken
    ) {}

    public record LoginResponse(
            String tokenType,
            String accessToken,
            Instant accessTokenExpiresAt,
            String refreshToken,
            Instant refreshTokenExpiresAt,
            UUID sessionId,
            UserInfo user
    ) {}

    public record UserInfo(
            UUID id,
            String email,
            String fullName,
            String platformRole
    ) {}
}