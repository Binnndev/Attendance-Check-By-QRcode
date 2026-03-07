package com.attendance.backend.auth.service;

import com.attendance.backend.auth.dto.AuthDtos;
import com.attendance.backend.auth.repository.UserRepository;
import com.attendance.backend.common.exception.ApiException;
import com.attendance.backend.domain.entity.User;
import com.attendance.backend.domain.enums.PlatformRole;
import com.attendance.backend.domain.enums.UserStatus;
import com.attendance.backend.security.jwt.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest req) {
        String emailNorm = normalizeEmail(req.email());

        User user = userRepository.findForLogin(emailNorm)
                .orElseThrow(() -> ApiException.unauthorized("INVALID_CREDENTIALS", "Invalid email or password"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw ApiException.unauthorized("USER_DISABLED", "User is not active");
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("INVALID_CREDENTIALS", "Invalid email or password");
        }

        PlatformRole platformRole = (user.getPlatformRole() != null) ? user.getPlatformRole() : PlatformRole.USER;

        String accessToken = jwtService.generateAccessToken(user.getId(), platformRole.name());

        return new AuthDtos.LoginResponse(
                "Bearer",
                accessToken,
                jwtService.accessTokenExpiresAt(),
                new AuthDtos.UserInfo(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        platformRole.name()
                )
        );
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}