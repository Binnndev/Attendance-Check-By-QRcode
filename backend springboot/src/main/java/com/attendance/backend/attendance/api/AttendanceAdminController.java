package com.attendance.backend.attendance.api;

import com.attendance.backend.attendance.service.AttendanceAdminService;
import com.attendance.backend.common.exception.ApiException;
import com.attendance.backend.security.UserPrincipal;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@Validated
public class AttendanceAdminController {

    private final AttendanceAdminService attendanceAdminService;

    public AttendanceAdminController(AttendanceAdminService attendanceAdminService) {
        this.attendanceAdminService = attendanceAdminService;
    }

    @PostMapping("/{sessionId}/checkin/reopen")
    public ReopenCheckinResponse reopenCheckin(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal me,
            @RequestBody(required = false) ReopenCheckinRequest req
    ) {
        if (me == null) {
            throw ApiException.forbidden("UNAUTHORIZED", "Missing JWT principal");
        }

        Integer openFromNowSeconds = req == null ? null : req.openFromNowSeconds();
        Integer closeFromNowSeconds = req == null ? null : req.closeFromNowSeconds();
        Integer lateAfterMinutes = req == null ? null : req.lateAfterMinutes();
        Integer qrRotateSeconds = req == null ? null : req.qrRotateSeconds();

        var result = attendanceAdminService.reopenCheckin(
                sessionId,
                me.getUserId(),
                openFromNowSeconds,
                closeFromNowSeconds,
                lateAfterMinutes,
                qrRotateSeconds
        );

        return new ReopenCheckinResponse(
                result.sessionId(),
                result.status(),
                result.checkinOpenAt(),
                result.checkinCloseAt(),
                result.lateAfterMinutes(),
                result.qrRotateSeconds()
        );
    }

    @PostMapping("/{sessionId}/attendance/{userId}/reset")
    public ResetAttendanceResponse resetAttendance(
            @PathVariable UUID sessionId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal UserPrincipal me
    ) {
        if (me == null) {
            throw ApiException.forbidden("UNAUTHORIZED", "Missing JWT principal");
        }

        var result = attendanceAdminService.resetAttendance(
                sessionId,
                userId,
                me.getUserId()
        );

        return new ResetAttendanceResponse(
                result.sessionId(),
                result.userId(),
                result.attendanceStatus(),
                result.checkInAt(),
                result.qrTokenId()
        );
    }

    @GetMapping("/{sessionId}/attendance-events")
    public AttendanceEventsResponse getAttendanceEvents(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal me,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) Integer limit
    ) {
        if (me == null) {
            throw ApiException.forbidden("UNAUTHORIZED", "Missing JWT principal");
        }

        var result = attendanceAdminService.getAttendanceEvents(
                sessionId,
                me.getUserId(),
                userId,
                limit
        );

        List<AttendanceEventItemResponse> items = result.items().stream()
                .map(it -> new AttendanceEventItemResponse(
                        it.id(),
                        it.sessionId(),
                        it.userId(),
                        it.actorUserId(),
                        it.eventType().name(),
                        it.oldStatus() == null ? null : it.oldStatus().name(),
                        it.newStatus() == null ? null : it.newStatus().name(),
                        it.qrTokenId(),
                        it.createdAt(),
                        it.eventPayload()
                ))
                .toList();

        return new AttendanceEventsResponse(result.sessionId(), items);
    }

    public record ReopenCheckinRequest(
            Integer openFromNowSeconds,
            Integer closeFromNowSeconds,
            Integer lateAfterMinutes,
            Integer qrRotateSeconds
    ) {}

    public record ReopenCheckinResponse(
            UUID sessionId,
            String status,
            Instant checkinOpenAt,
            Instant checkinCloseAt,
            Integer lateAfterMinutes,
            Integer qrRotateSeconds
    ) {}

    public record ResetAttendanceResponse(
            UUID sessionId,
            UUID userId,
            String attendanceStatus,
            Instant checkInAt,
            String qrTokenId
    ) {}

    public record AttendanceEventsResponse(
            UUID sessionId,
            List<AttendanceEventItemResponse> items
    ) {}

    public record AttendanceEventItemResponse(
            UUID id,
            UUID sessionId,
            UUID userId,
            UUID actorUserId,
            String eventType,
            String oldStatus,
            String newStatus,
            String qrTokenId,
            Instant createdAt,
            JsonNode eventPayload
    ) {}
}