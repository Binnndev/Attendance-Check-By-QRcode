package com.attendance.backend.auth.api;

import com.attendance.backend.auth.dto.UpdateMeRequest;
import com.attendance.backend.auth.service.AuthService;
import com.attendance.backend.common.exception.ApiException;
import com.attendance.backend.domain.entity.User;
import com.attendance.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class MeController {

    private final AuthService authService;

    public MeController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        UUID userId = requireUserId(principal);
        User user = authService.getCurrentUser(userId);
        return toResponse(user);
    }

    @PatchMapping("/me")
    public MeResponse updateMe(@AuthenticationPrincipal UserPrincipal principal,
                               @Valid @RequestBody UpdateMeRequest request) {
        UUID userId = requireUserId(principal);
        User user = authService.updateMe(userId, request);
        return toResponse(user);
    }

    private UUID requireUserId(UserPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            throw ApiException.unauthorized("UNAUTHORIZED", "Missing JWT principal");
        }
        return principal.getUserId();
    }

    private MeResponse toResponse(User user) {
        return new MeResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getPlatformRole().name()
        );
    }

    public record MeResponse(
            UUID id,
            String email,
            String fullName,
            String avatarUrl,
            String platformRole
    ) {
    }
}