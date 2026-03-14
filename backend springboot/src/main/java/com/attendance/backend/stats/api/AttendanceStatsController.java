package com.attendance.backend.stats.api;

import com.attendance.backend.common.exception.ApiException;
import com.attendance.backend.security.UserPrincipal;
import com.attendance.backend.stats.dto.AttendanceSummaryResponse;
import com.attendance.backend.stats.dto.GroupAttendanceSummaryPageResponse;
import com.attendance.backend.stats.service.AttendanceStatsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Validated
public class AttendanceStatsController {

    private final AttendanceStatsService attendanceStatsService;

    public AttendanceStatsController(AttendanceStatsService attendanceStatsService) {
        this.attendanceStatsService = attendanceStatsService;
    }

    @GetMapping("/me/attendance/summary")
    public AttendanceSummaryResponse getMyAttendanceSummary(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null || principal.getUserId() == null) {
            throw ApiException.unauthorized("UNAUTHORIZED", "Unauthorized");
        }

        return attendanceStatsService.getMySummary(principal.getUserId());
    }

    @GetMapping("/groups/{groupId}/attendance/summary")
    public GroupAttendanceSummaryPageResponse getGroupAttendanceSummary(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) int size
    ) {
        if (principal == null || principal.getUserId() == null) {
            throw ApiException.unauthorized("UNAUTHORIZED", "Unauthorized");
        }

        return attendanceStatsService.getGroupSummary(groupId, principal.getUserId(), page, size);
    }
}