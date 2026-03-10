package com.attendance.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "email is required")
        @Email(message = "email is invalid")
        @Size(max = 190, message = "email must be at most 190 characters")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 8, max = 200, message = "password must be between 8 and 200 characters")
        String password,

        @NotBlank(message = "fullName is required")
        @Size(min = 2, max = 120, message = "fullName must be between 2 and 120 characters")
        String fullName,

        @Size(max = 40, message = "userCode must be at most 40 characters")
        String userCode,

        @Size(max = 120, message = "deviceId must be at most 120 characters")
        String deviceId
) {}