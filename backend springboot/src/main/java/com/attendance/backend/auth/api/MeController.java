package com.attendance.backend.auth.api;

import com.attendance.backend.auth.repository.UserRepository;
import com.attendance.backend.common.exception.ApiException;
import com.attendance.backend.domain.entity.User;
import com.attendance.backend.security.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class MeController {

    private final UserRepository userRepository;

    public MeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw ApiException.unauthorized("UNAUTHORIZED", "Missing JWT principal");
        }

        UUID userId = principal.getUserId();

        User u = userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));

        return new MeResponse(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getAvatarUrl(),
                u.getPlatformRole().name()
        );
    }

    public record MeResponse(
            UUID id,
            String email,
            String fullName,
            String avatarUrl,
            String platformRole
    ) {}
}