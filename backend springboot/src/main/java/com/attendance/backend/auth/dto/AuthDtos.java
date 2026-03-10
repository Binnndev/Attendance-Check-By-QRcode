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
            String password
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