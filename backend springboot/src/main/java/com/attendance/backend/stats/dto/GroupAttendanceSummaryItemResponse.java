package com.attendance.backend.stats.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GroupAttendanceSummaryItemResponse(
        UUID userId,
        String fullName,
        String email,
        long totalSessions,
        long presentCount,
        long lateCount,
        long absentCount,
        long excusedCount,
        BigDecimal attendancePercent,
        BigDecimal absencePercent,
        AttendanceSummaryResponse.WarningLevel warningLevel,
        AttendanceSummaryResponse.RiskLevel riskLevel
) {
}