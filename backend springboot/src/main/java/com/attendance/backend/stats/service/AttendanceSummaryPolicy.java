package com.attendance.backend.stats.service;

import com.attendance.backend.stats.dto.AttendanceSummaryResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AttendanceSummaryPolicy {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal WARNING_THRESHOLD = new BigDecimal("15.00");
    private static final BigDecimal CRITICAL_THRESHOLD = new BigDecimal("20.00");

    public CalculatedFields calculate(
            long totalSessions,
            long presentCount,
            long lateCount,
            long absentCount,
            long excusedCount
    ) {
        long attendedCount = presentCount + lateCount + excusedCount;

        BigDecimal attendancePercent = percentage(attendedCount, totalSessions);
        BigDecimal absencePercent = percentage(absentCount, totalSessions);

        return new CalculatedFields(
                attendancePercent,
                absencePercent,
                determineWarningLevel(absencePercent),
                determineRiskLevel(absencePercent)
        );
    }

    private BigDecimal percentage(long numerator, long denominator) {
        if (denominator <= 0L) {
            return ZERO;
        }

        return BigDecimal.valueOf(numerator)
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private AttendanceSummaryResponse.WarningLevel determineWarningLevel(BigDecimal absencePercent) {
        if (absencePercent.compareTo(CRITICAL_THRESHOLD) >= 0) {
            return AttendanceSummaryResponse.WarningLevel.CRITICAL_20;
        }
        if (absencePercent.compareTo(WARNING_THRESHOLD) >= 0) {
            return AttendanceSummaryResponse.WarningLevel.WARNING_15;
        }
        return AttendanceSummaryResponse.WarningLevel.NONE;
    }

    private AttendanceSummaryResponse.RiskLevel determineRiskLevel(BigDecimal absencePercent) {
        if (absencePercent.compareTo(CRITICAL_THRESHOLD) >= 0) {
            return AttendanceSummaryResponse.RiskLevel.HIGH;
        }
        if (absencePercent.compareTo(WARNING_THRESHOLD) >= 0) {
            return AttendanceSummaryResponse.RiskLevel.MEDIUM;
        }
        return AttendanceSummaryResponse.RiskLevel.LOW;
    }

    public record CalculatedFields(
            BigDecimal attendancePercent,
            BigDecimal absencePercent,
            AttendanceSummaryResponse.WarningLevel warningLevel,
            AttendanceSummaryResponse.RiskLevel riskLevel
    ) {
    }
}