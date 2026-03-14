package com.attendance.backend.auth.service;

import com.attendance.backend.auth.config.PasswordResetProperties;
import com.attendance.backend.auth.dto.AuthDtos;
import com.attendance.backend.auth.dto.ChangePasswordRequest;
import com.attendance.backend.auth.dto.RegisterRequest;
import com.attendance.backend.auth.dto.RegisterResponse;
import com.attendance.backend.auth.dto.UpdateMeRequest;
import com.attendance.backend.auth.repository.PasswordResetTokenRepository;
import com.attendance.backend.auth.repository.UserRepository;
import com.attendance.backend.auth.repository.UserSessionRepository;
import com.attendance.backend.common.exception.ApiException;
import com.attendance.backend.domain.entity.PasswordResetToken;
import com.attendance.backend.domain.entity.User;
import com.attendance.backend.domain.entity.UserSession;
import com.attendance.backend.domain.enums.PlatformRole;
import com.attendance.backend.domain.enums.UserStatus;
import com.attendance.backend.mail.MailService;
import com.attendance.backend.security.jwt.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final Logger securityAuditLog = LoggerFactory.getLogger("SECURITY_AUDIT");

    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private static final String REVOKE_REASON_LOGOUT = "LOGOUT";
    private static final String REVOKE_REASON_LOGOUT_ALL = "LOGOUT_ALL";
    private static final String REVOKE_REASON_PASSWORD_CHANGED = "PASSWORD_CHANGED";
    private static final String REVOKE_REASON_PASSWORD_RESET = "PASSWORD_RESET";

    private static final String RESET_TOKEN_REVOKE_REASON_SUPERSEDED = "SUPERSEDED";
    private static final String RESET_TOKEN_REVOKE_REASON_COMPLETED = "PASSWORD_RESET_COMPLETED";
    private static final String RESET_TOKEN_REVOKE_REASON_MAIL_DELIVERY_FAILED = "MAIL_DELIVERY_FAILED";

    private static final String FORGOT_PASSWORD_NEUTRAL_MESSAGE =
            "If the email exists, a password reset link has been sent.";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetAttemptService passwordResetAttemptService;
    private final PasswordPolicyService passwordPolicyService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetProperties passwordResetProperties;
    private final MailService mailService;
    private final Clock clock;

    public AuthService(UserRepository userRepository,
                       UserSessionRepository userSessionRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordResetAttemptService passwordResetAttemptService,
                       PasswordPolicyService passwordPolicyService,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       PasswordResetProperties passwordResetProperties,
                       MailService mailService,
                       Clock clock) {
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordResetAttemptService = passwordResetAttemptService;
        this.passwordPolicyService = passwordPolicyService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.passwordResetProperties = passwordResetProperties;
        this.mailService = mailService;
        this.clock = clock;
    }

    @Transactional
    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest req, String ipAddress, String userAgent) {
        String emailNorm = normalizeEmail(req.email());

        User user = userRepository.findForLogin(emailNorm)
                .orElseThrow(() -> ApiException.unauthorized("INVALID_CREDENTIALS", "Invalid email or password"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw ApiException.unauthorized("USER_DISABLED", "User is not active");
        }

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("INVALID_CREDENTIALS", "Invalid email or password");
        }

        Instant now = Instant.now(clock);
        UUID sessionId = UUID.randomUUID();

        JwtService.TokenBundle tokenBundle = jwtService.issueTokenBundle(user, sessionId, now);

        UserSession session = new UserSession();
        session.setId(sessionId);
        session.setUserId(user.getId());
        session.setRefreshTokenHash(jwtService.hashRefreshToken(tokenBundle.refreshToken()));
        session.setDeviceId(normalizeNullable(req.deviceId()));
        session.setIpAddress(normalizeNullable(ipAddress));
        session.setUserAgent(truncateUserAgent(userAgent));
        session.setIssuedAt(now);
        session.setExpiresAt(tokenBundle.refreshTokenExpiresAt());

        userSessionRepository.save(session);

        return toLoginResponse(user, tokenBundle);
    }

    @Transactional
    public RegisterResponse register(RegisterRequest req, String ipAddress, String userAgent) {
        try {
            String emailNorm = normalizeEmail(req.email());
            String fullName = normalizeText(req.fullName());
            String userCode = normalizeNullable(req.userCode());
            String deviceId = normalizeNullable(req.deviceId());

            if (!StringUtils.hasText(emailNorm)) {
                throw ApiException.badRequest("EMAIL_REQUIRED", "email is required");
            }

            passwordPolicyService.validateOrThrow(req.password());

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

            userRepository.saveAndFlush(user);

            Instant now = Instant.now(clock);
            UUID sessionId = UUID.randomUUID();

            JwtService.TokenBundle tokenBundle = jwtService.issueTokenBundle(user, sessionId, now);

            UserSession session = new UserSession();
            session.setId(sessionId);
            session.setUserId(user.getId());
            session.setRefreshTokenHash(jwtService.hashRefreshToken(tokenBundle.refreshToken()));
            session.setDeviceId(deviceId);
            session.setIpAddress(normalizeNullable(ipAddress));
            session.setUserAgent(truncateUserAgent(userAgent));
            session.setIssuedAt(now);
            session.setExpiresAt(tokenBundle.refreshTokenExpiresAt());

            userSessionRepository.save(session);

            return new RegisterResponse(
                    TOKEN_TYPE_BEARER,
                    tokenBundle.accessToken(),
                    tokenBundle.accessTokenExpiresAt(),
                    tokenBundle.refreshToken(),
                    tokenBundle.refreshTokenExpiresAt(),
                    tokenBundle.sessionId(),
                    new RegisterResponse.UserSummary(
                            user.getId(),
                            user.getEmail(),
                            user.getFullName(),
                            resolvePlatformRole(user).name()
                    ),
                    true
            );
        } catch (DataIntegrityViolationException ex) {
            throw mapRegisterConflict(ex);
        }
    }

    @Transactional
    public AuthDtos.LoginResponse refresh(AuthDtos.RefreshRequest req) {
        String rawRefreshToken = req.refreshToken();
        if (!StringUtils.hasText(rawRefreshToken)) {
            throw ApiException.badRequest("REFRESH_TOKEN_REQUIRED", "refreshToken is required");
        }

        JwtService.ParsedJwt parsed;
        try {
            parsed = jwtService.parseAndValidate(rawRefreshToken);
        } catch (Exception ex) {
            throw ApiException.unauthorized("INVALID_REFRESH_TOKEN", "Invalid refresh token");
        }

        if (!JwtService.TOKEN_TYPE_REFRESH.equals(parsed.type())) {
            throw ApiException.unauthorized("INVALID_REFRESH_TOKEN", "Invalid refresh token");
        }

        Instant now = Instant.now(clock);

        UserSession session = userSessionRepository.findByIdForUpdate(parsed.sessionId())
                .orElseThrow(() -> ApiException.unauthorized("INVALID_REFRESH_TOKEN", "Invalid refresh token"));

        if (session.isRevoked() || session.isExpiredAt(now)) {
            throw ApiException.unauthorized("SESSION_REVOKED", "Session is no longer active");
        }

        if (!session.getUserId().equals(parsed.userId())) {
            throw ApiException.unauthorized("INVALID_REFRESH_TOKEN", "Invalid refresh token");
        }

        if (!jwtService.matchesRefreshTokenHash(rawRefreshToken, session.getRefreshTokenHash())) {
            throw ApiException.unauthorized("INVALID_REFRESH_TOKEN", "Invalid refresh token");
        }

        User user = getCurrentActiveUserOrThrow(parsed.userId());

        JwtService.TokenBundle tokenBundle = jwtService.issueTokenBundle(user, session.getId(), now);

        session.setRefreshTokenHash(jwtService.hashRefreshToken(tokenBundle.refreshToken()));
        session.setIssuedAt(now);
        session.setExpiresAt(tokenBundle.refreshTokenExpiresAt());
        session.setLastUsedAt(now);

        userSessionRepository.save(session);

        return toLoginResponse(user, tokenBundle);
    }

    @Transactional
    public void logout(AuthDtos.LogoutRequest req) {
        String rawRefreshToken = req.refreshToken();
        if (!StringUtils.hasText(rawRefreshToken)) {
            throw ApiException.badRequest("REFRESH_TOKEN_REQUIRED", "refreshToken is required");
        }

        JwtService.ParsedJwt parsed;
        try {
            parsed = jwtService.parseAndValidate(rawRefreshToken);
        } catch (Exception ex) {
            return;
        }

        if (!JwtService.TOKEN_TYPE_REFRESH.equals(parsed.type())) {
            return;
        }

        Instant now = Instant.now(clock);

        userSessionRepository.findByIdForUpdate(parsed.sessionId())
                .ifPresent(session -> {
                    if (!session.isRevoked()) {
                        session.revoke(now, REVOKE_REASON_LOGOUT);
                        userSessionRepository.save(session);
                    }
                });
    }

    @Transactional
    public AuthDtos.ForgotPasswordResponse forgotPassword(AuthDtos.ForgotPasswordRequest req,
                                                          String ipAddress,
                                                          String userAgent) {
        String emailNorm = normalizeEmail(req.email());
        if (!StringUtils.hasText(emailNorm)) {
            throw ApiException.badRequest("EMAIL_REQUIRED", "email is required");
        }

        String normalizedIp = normalizeNullable(ipAddress);
        String normalizedUserAgent = truncateUserAgent(userAgent);

        User user = userRepository.findByEmailNorm(emailNorm).orElse(null);
        boolean userExists = user != null;
        boolean userActive = userExists && user.getStatus() == UserStatus.ACTIVE;

        PasswordResetAttemptService.Decision decision = passwordResetAttemptService.evaluateAndRecord(
                emailNorm,
                normalizedIp,
                normalizedUserAgent,
                userExists ? user.getId() : null,
                userExists,
                userActive
        );

        if (!decision.allowed()) {
            auditForgotPassword(emailNorm, userExists ? user.getId() : null, normalizedIp, decision.reason());
            return new AuthDtos.ForgotPasswordResponse(FORGOT_PASSWORD_NEUTRAL_MESSAGE);
        }

        Instant now = Instant.now(clock);
        String plainToken = generateOpaqueToken();
        byte[] tokenHash = sha256(plainToken);

        passwordResetTokenRepository.revokeAllActiveByUserId(
                user.getId(),
                now,
                RESET_TOKEN_REVOKE_REASON_SUPERSEDED
        );

        PasswordResetToken token = new PasswordResetToken();
        token.setId(UUID.randomUUID());
        token.setUserId(user.getId());
        token.setTokenHash(tokenHash);
        token.setRequestedIp(normalizedIp);
        token.setUserAgent(normalizedUserAgent);
        token.setExpiresAt(now.plus(passwordResetProperties.getTokenMinutes(), ChronoUnit.MINUTES));

        passwordResetTokenRepository.saveAndFlush(token);

        String resetUrl = buildResetUrl(passwordResetProperties.getFrontendResetUrl(), plainToken);

        try {
            mailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getFullName(),
                    resetUrl,
                    token.getExpiresAt()
            );
            auditForgotPassword(emailNorm, user.getId(), normalizedIp, "ISSUED");
        } catch (Exception ex) {
            token.revoke(now, RESET_TOKEN_REVOKE_REASON_MAIL_DELIVERY_FAILED);
            passwordResetTokenRepository.saveAndFlush(token);
            auditForgotPassword(emailNorm, user.getId(), normalizedIp, RESET_TOKEN_REVOKE_REASON_MAIL_DELIVERY_FAILED);
        }

        return new AuthDtos.ForgotPasswordResponse(FORGOT_PASSWORD_NEUTRAL_MESSAGE);
    }

    @Transactional
    public void resetPassword(AuthDtos.ResetPasswordRequest req) {
        String rawToken = req.token();
        if (!StringUtils.hasText(rawToken)) {
            throw ApiException.badRequest("RESET_TOKEN_REQUIRED", "token is required");
        }

        passwordPolicyService.validateOrThrow(req.newPassword());

        Instant now = Instant.now(clock);
        byte[] tokenHash = sha256(rawToken);

        PasswordResetToken token = passwordResetTokenRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> {
                    auditResetPassword(null, null, "INVALID_TOKEN");
                    return ApiException.badRequest("RESET_TOKEN_INVALID", "Reset token is invalid");
                });

        if (token.isUsed()) {
            auditResetPassword(token.getUserId(), token.getRequestedIp(), "ALREADY_USED");
            throw ApiException.badRequest("RESET_TOKEN_ALREADY_USED", "Reset token has already been used");
        }

        if (token.isRevoked()) {
            auditResetPassword(token.getUserId(), token.getRequestedIp(), "REVOKED");
            throw ApiException.badRequest("RESET_TOKEN_REVOKED", "Reset token has been revoked");
        }

        if (token.isExpiredAt(now)) {
            auditResetPassword(token.getUserId(), token.getRequestedIp(), "EXPIRED");
            throw ApiException.badRequest("RESET_TOKEN_EXPIRED", "Reset token has expired");
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(token.getUserId())
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            auditResetPassword(user.getId(), token.getRequestedIp(), "USER_NOT_ACTIVE");
            throw ApiException.badRequest("USER_DISABLED", "User is not active");
        }

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);

        token.markUsed(now);
        passwordResetTokenRepository.save(token);

        passwordResetTokenRepository.revokeAllActiveByUserId(
                user.getId(),
                now,
                RESET_TOKEN_REVOKE_REASON_COMPLETED
        );

        userSessionRepository.revokeAllActiveByUserId(
                user.getId(),
                now,
                REVOKE_REASON_PASSWORD_RESET
        );

        auditResetPassword(user.getId(), token.getRequestedIp(), "SUCCESS");
    }

    @Transactional
    public void logoutAll(UUID userId) {
        User user = getCurrentActiveUserOrThrow(userId);
        Instant now = Instant.now(clock);
        userSessionRepository.revokeAllActiveByUserId(user.getId(), now, REVOKE_REASON_LOGOUT_ALL);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(UUID userId) {
        return getCurrentActiveUserOrThrow(userId);
    }

    @Transactional
    public User updateMe(UUID userId, UpdateMeRequest req) {
        rejectUnknownFields(req.getUnknownFields());

        User user = getCurrentActiveUserOrThrow(userId);

        boolean changed = false;

        if (req.getFullName() != null) {
            String fullName = normalizeText(req.getFullName());
            if (!StringUtils.hasText(fullName) || fullName.length() < 2 || fullName.length() > 120) {
                throw ApiException.badRequest("FULL_NAME_INVALID", "fullName must be between 2 and 120 characters");
            }
            user.setFullName(fullName);
            changed = true;
        }

        if (req.getAvatarUrl() != null) {
            String avatarUrl = normalizeNullable(req.getAvatarUrl());
            if (avatarUrl != null && avatarUrl.length() > 500) {
                throw ApiException.badRequest("AVATAR_URL_TOO_LONG", "avatarUrl must be at most 500 characters");
            }
            user.setAvatarUrl(avatarUrl);
            changed = true;
        }

        if (!changed) {
            throw ApiException.badRequest("NO_FIELDS_TO_UPDATE", "At least one updatable field is required");
        }

        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest req) {
        rejectUnknownFields(req.getUnknownFields());

        User user = getCurrentActiveUserOrThrow(userId);

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw ApiException.unprocessable("CURRENT_PASSWORD_INCORRECT", "Current password is incorrect");
        }

        if (req.getCurrentPassword().equals(req.getNewPassword())) {
            throw ApiException.unprocessable(
                    "NEW_PASSWORD_MUST_BE_DIFFERENT",
                    "New password must be different from current password"
            );
        }

        passwordPolicyService.validateOrThrow(req.getNewPassword());

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);

        Instant now = Instant.now(clock);
        userSessionRepository.revokeAllActiveByUserId(user.getId(), now, REVOKE_REASON_PASSWORD_CHANGED);
    }

    private User getCurrentActiveUserOrThrow(UUID userId) {
        if (userId == null) {
            throw ApiException.unauthorized("UNAUTHORIZED", "Missing JWT principal");
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw ApiException.unauthorized("USER_DISABLED", "User is not active");
        }

        return user;
    }

    private AuthDtos.LoginResponse toLoginResponse(User user, JwtService.TokenBundle tokenBundle) {
        return new AuthDtos.LoginResponse(
                TOKEN_TYPE_BEARER,
                tokenBundle.accessToken(),
                tokenBundle.accessTokenExpiresAt(),
                tokenBundle.refreshToken(),
                tokenBundle.refreshTokenExpiresAt(),
                tokenBundle.sessionId(),
                new AuthDtos.UserInfo(
                        user.getId(),
                        user.getEmail(),
                        user.getFullName(),
                        resolvePlatformRole(user).name()
                )
        );
    }

    private PlatformRole resolvePlatformRole(User user) {
        return user.getPlatformRole() != null ? user.getPlatformRole() : PlatformRole.USER;
    }

    private void rejectUnknownFields(Set<String> unknownFields) {
        if (unknownFields == null || unknownFields.isEmpty()) {
            return;
        }

        throw ApiException.badRequest(
                "INVALID_REQUEST_BODY",
                "Unknown field(s): " + String.join(", ", unknownFields)
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

    private String buildResetUrl(String frontendResetUrl, String token) {
        if (!StringUtils.hasText(frontendResetUrl)) {
            throw ApiException.badRequest(
                    "PASSWORD_RESET_FRONTEND_URL_MISSING",
                    "password reset frontend URL is not configured"
            );
        }

        String separator = frontendResetUrl.contains("?") ? "&" : "?";
        return frontendResetUrl + separator + "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(raw.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to hash value", ex);
        }
    }

    private void auditForgotPassword(String emailNorm, UUID userId, String ipAddress, String outcome) {
        securityAuditLog.info(
                "event=forgot_password userId={} email={} ip={} outcome={}",
                userId,
                maskEmail(emailNorm),
                ipAddress,
                outcome
        );
    }

    private void auditResetPassword(UUID userId, String ipAddress, String outcome) {
        securityAuditLog.info(
                "event=reset_password userId={} ip={} outcome={}",
                userId,
                ipAddress,
                outcome
        );
    }

    private String maskEmail(String email) {
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            return "***";
        }

        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 2) {
            return "***@" + domain;
        }

        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
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

    private static String truncateUserAgent(String userAgent) {
        String normalized = normalizeNullable(userAgent);
        if (normalized == null) {
            return null;
        }
        return normalized.length() <= 255 ? normalized : normalized.substring(0, 255);
    }
}