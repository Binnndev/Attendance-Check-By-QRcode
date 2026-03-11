package com.attendance.backend.group.dto;

import java.util.List;

public record PageMemberResponse(
        List<MemberResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}