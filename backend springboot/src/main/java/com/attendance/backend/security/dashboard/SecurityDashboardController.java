package com.attendance.backend.security.dashboard;

import com.attendance.backend.common.exception.ApiException;
import com.attendance.backend.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/security")
public class SecurityDashboardController {

    private final SecurityDashboardService service;

    public SecurityDashboardController(SecurityDashboardService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public SecurityDashboardService.OverviewResponse overview(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer hours
    ) {
        if (principal == null || principal.getUserId() == null) {
            throw ApiException.unauthorized("UNAUTHORIZED", "Missing JWT principal");
        }
        return service.getOverview(principal.getUserId(), hours);
    }

    @GetMapping("/password-reset-abuse")
    public List<SecurityDashboardService.AbuseItem> passwordResetAbuse(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer hours,
            @RequestParam(required = false) Integer limit
    ) {
        if (principal == null || principal.getUserId() == null) {
            throw ApiException.unauthorized("UNAUTHORIZED", "Missing JWT principal");
        }
        return service.getPasswordResetAbuse(principal.getUserId(), hours, limit);
    }

    @GetMapping("/login-abuse")
    public List<SecurityDashboardService.AbuseItem> loginAbuse(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer hours,
            @RequestParam(required = false) Integer limit
    ) {
        if (principal == null || principal.getUserId() == null) {
            throw ApiException.unauthorized("UNAUTHORIZED", "Missing JWT principal");
        }
        return service.getLoginAbuse(principal.getUserId(), hours, limit);
    }

    @GetMapping("/email-outbox")
    public List<SecurityDashboardService.DeadOutboxItem> deadOutbox(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer limit
    ) {
        if (principal == null || principal.getUserId() == null) {
            throw ApiException.unauthorized("UNAUTHORIZED", "Missing JWT principal");
        }
        return service.getDeadOutbox(principal.getUserId(), limit);
    }
}