package com.attendance.backend.group.dto;

import java.time.Instant;
import java.util.UUID;

public record MemberResponse(
        UUID groupId,
        UUID userId,
        String role,
        String memberStatus,
        Instant joinedAt,
        UUID invitedBy,
        Instant createdAt,
        Instant updatedAt,
        Instant removedAt,
        String email,
        String fullName,
        String avatarUrl
) {}