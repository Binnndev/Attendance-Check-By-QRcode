package com.attendance.backend.auth.dto;

import com.attendance.backend.domain.enums.PlatformRole;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.UUID;

public class AuthDtos {

    public record LoginRequest(
            @NotBlank String email,
            @NotBlank String password
    ) {}

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