package com.attendance.backend.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinGroupRequest(
        @NotBlank(message = "joinCode is required")
        @Size(min = 6, max = 16, message = "joinCode length must be between 6 and 16")
        String joinCode
) {}