package com.attendance.backend.attendance.api;

import com.attendance.backend.attendance.service.AttendanceCheckinService;
import com.attendance.backend.domain.enums.AttendanceStatus;
import com.attendance.backend.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Validated
public class AttendanceQrController {

    private final AttendanceCheckinService checkinService;

    public AttendanceQrController(AttendanceCheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @PostMapping("/sessions/{sessionId}/checkin/qr")
    public ResponseEntity<QrCheckinResponse> checkinQr(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal me,
            @RequestBody QrCheckinRequest body
    ) {
        UUID userId = me.getUserId();

        var cmd = new AttendanceCheckinService.QrCheckinCommand(
                sessionId,
                userId,
                body.token(),
                body.deviceId(),
                body.ipAddress(),
                body.userAgent(),
                body.geoLat(),
                body.geoLng(),
                body.distanceMeter()
        );

        var r = checkinService.qrCheckin(cmd);

        return ResponseEntity.ok(new QrCheckinResponse(
                r.sessionId(),
                r.userId(),
                r.attendanceStatus(),
                r.checkInAt(),
                r.qrTokenId()
        ));
    }

    public record QrCheckinRequest(
            String token,
            String deviceId,
            String ipAddress,
            String userAgent,
            BigDecimal geoLat,
            BigDecimal geoLng,
            Integer distanceMeter
    ) {}

    public record QrCheckinResponse(
            UUID sessionId,
            UUID userId,
            AttendanceStatus attendanceStatus,
            Instant checkInAt,
            String qrTokenId
    ) {}
}