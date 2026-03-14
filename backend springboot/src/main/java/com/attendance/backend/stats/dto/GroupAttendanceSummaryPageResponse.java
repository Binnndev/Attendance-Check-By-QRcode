package com.attendance.backend.stats.dto;

import java.util.List;

public record GroupAttendanceSummaryPageResponse(
        List<GroupAttendanceSummaryItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}