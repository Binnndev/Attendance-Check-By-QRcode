package com.attendance.backend.stats.repository;

import org.springframework.data.domain.Page;

import java.util.UUID;

public interface AttendanceStatsQueryRepository {

    StudentSummaryRow findStudentSummary(UUID userId);

    Page<GroupSummaryRow> findGroupSummaryPage(UUID groupId, int page, int size);

    boolean groupExists(UUID groupId);

    boolean isOwnerOrCoHost(UUID groupId, UUID userId);

    record StudentSummaryRow(
            long totalSessions,
            long presentCount,
            long lateCount,
            long absentCount,
            long excusedCount
    ) {
    }

    record GroupSummaryRow(
            UUID userId,
            String fullName,
            String email,
            long totalSessions,
            long presentCount,
            long lateCount,
            long absentCount,
            long excusedCount
    ) {
    }
}