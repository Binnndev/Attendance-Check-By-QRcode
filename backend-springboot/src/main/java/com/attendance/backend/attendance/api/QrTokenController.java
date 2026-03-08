package com.attendance.backend.attendance.api;

import com.attendance.backend.attendance.service.QrTokenRotateService;
import com.attendance.backend.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/qr")
public class QrTokenController {

    private final QrTokenRotateService qrTokenRotateService;

    public QrTokenController(QrTokenRotateService qrTokenRotateService) {
        this.qrTokenRotateService = qrTokenRotateService;
    }

    @PostMapping("/rotate")
    public RotateQrResponse rotate(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody(required = false) RotateQrRequest req
    ) {
        if (principal == null) {
            throw com.attendance.backend.common.exception.ApiException.forbidden("UNAUTHORIZED", "Missing JWT principal");
        }

        String note = (req == null) ? null : req.note();
        var result = qrTokenRotateService.rotate(sessionId, principal.getUserId(), note);

        return new RotateQrResponse(
                result.sessionId(),
                result.token(),
                result.issuedAt(),
                result.expiresAt(),
                result.rotatedFromTokenId()
        );
    }

    public record RotateQrRequest(String note) {}

    public record RotateQrResponse(
            UUID sessionId,
            String token,
            Instant issuedAt,
            Instant expiresAt,
            String rotatedFromTokenId
    ) {}
}
