package com.attendance.backend.auth.service;

import com.attendance.backend.auth.dto.AuthDtos;
import com.attendance.backend.auth.dto.RegisterRequest;
import com.attendance.backend.auth.dto.RegisterResponse;
import com.attendance.backend.auth.repository.UserRepository;
import com.attendance.backend.common.exception.ApiException;
import com.attendance.backend.domain.entity.User;
import com.attendance.backend.domain.enums.PlatformRole;
import com.attendance.backend.domain.enums.UserStatus;
import com.attendance.backend.security.jwt.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.UUID;

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

        PlatformRole platformRole = (user.getPlatformRole() != null)
                ? user.getPlatformRole()
                : PlatformRole.USER;

        JwtService.AccessTokenResult token = jwtService.issueAccessToken(user);

        return new AuthDtos.LoginResponse(
                "Bearer",
                token.accessToken(),
                token.expiresAt(),
                new AuthDtos.UserInfo(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        platformRole.name()
                )
        );
    }

    @Transactional
    public RegisterResponse register(RegisterRequest req) {
        String emailNorm = normalizeEmail(req.email());
        String fullName = normalizeText(req.fullName());
        String userCode = normalizeNullable(req.userCode());
        String deviceId = normalizeNullable(req.deviceId());

        if (!StringUtils.hasText(emailNorm)) {
            throw ApiException.badRequest("EMAIL_REQUIRED", "email is required");
        }

        if (!StringUtils.hasText(req.password())) {
            throw ApiException.badRequest("PASSWORD_REQUIRED", "password is required");
        }

        if (req.password().length() < 8) {
            throw ApiException.badRequest("PASSWORD_TOO_SHORT", "password must be at least 8 characters");
        }

        if (!StringUtils.hasText(fullName) || fullName.length() < 2) {
            throw ApiException.badRequest("FULL_NAME_INVALID", "fullName must be at least 2 characters");
        }

        if (userRepository.existsByEmailNorm(emailNorm)) {
            throw ApiException.conflict("EMAIL_ALREADY_EXISTS", "Email already exists");
        }

        if (StringUtils.hasText(userCode) && userRepository.existsByUserCode(userCode)) {
            throw ApiException.conflict("USER_CODE_ALREADY_EXISTS", "User code already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(emailNorm);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setFullName(fullName);
        user.setUserCode(userCode);
        user.setPrimaryDeviceId(deviceId);
        user.setPlatformRole(PlatformRole.USER);
        user.setStatus(UserStatus.ACTIVE);

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw mapRegisterConflict(ex);
        }

        PlatformRole platformRole = (user.getPlatformRole() != null)
                ? user.getPlatformRole()
                : PlatformRole.USER;

        JwtService.AccessTokenResult token = jwtService.issueAccessToken(user);

        return new RegisterResponse(
                "Bearer",
                token.accessToken(),
                token.expiresAt(),
                new RegisterResponse.UserSummary(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        platformRole.name()
                ),
                true
        );
    }

    private ApiException mapRegisterConflict(DataIntegrityViolationException ex) {
        String msg = extractErrorMessage(ex);

        if (msg.contains("uk_users_email_norm")) {
            return ApiException.conflict("EMAIL_ALREADY_EXISTS", "Email already exists");
        }

        if (msg.contains("uk_users_user_code")) {
            return ApiException.conflict("USER_CODE_ALREADY_EXISTS", "User code already exists");
        }

        if (msg.contains("email_norm")) {
            return ApiException.conflict("EMAIL_ALREADY_EXISTS", "Email already exists");
        }

        if (msg.contains("user_code")) {
            return ApiException.conflict("USER_CODE_ALREADY_EXISTS", "User code already exists");
        }

        return ApiException.conflict("USER_ALREADY_EXISTS", "User already exists");
    }

    private String extractErrorMessage(Throwable ex) {
        Throwable current = ex;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String msg = current.getMessage();
        return msg == null ? "" : msg.toLowerCase(Locale.ROOT);
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}