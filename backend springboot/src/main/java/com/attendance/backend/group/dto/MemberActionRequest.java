package com.attendance.backend.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberActionRequest(
        @NotBlank(message = "action is required")
        String action,

        @Size(max = 500, message = "note max length is 500")
        String note
) {}