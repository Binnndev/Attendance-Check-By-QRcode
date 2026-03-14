package com.attendance.backend.stats.dto;

import java.math.BigDecimal;

public record AttendanceSummaryResponse(
        long totalSessions,
        long presentCount,
        long lateCount,
        long absentCount,
        long excusedCount,
        BigDecimal attendancePercent,
        BigDecimal absencePercent,
        WarningLevel warningLevel,
        RiskLevel riskLevel
) {
    public enum WarningLevel {
        NONE,
        WARNING_15,
        CRITICAL_20
    }

    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH
    }
}