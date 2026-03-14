package com.attendance.backend.auth.service;

import com.attendance.backend.auth.repository.PasswordResetAttemptRepository;
import com.attendance.backend.domain.entity.PasswordResetAttempt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

@Service
public class PasswordResetAttemptService {

    public static final String OUTCOME_ACCEPTED = "ACCEPTED";
    public static final String OUTCOME_EMAIL_NOT_FOUND = "EMAIL_NOT_FOUND";
    public static final String OUTCOME_USER_NOT_ACTIVE = "USER_NOT_ACTIVE";
    public static final String OUTCOME_THROTTLED_EMAIL = "THROTTLED_EMAIL";
    public static final String OUTCOME_THROTTLED_IP = "THROTTLED_IP";
    public static final String OUTCOME_REDIS_UNAVAILABLE = "REDIS_UNAVAILABLE";
    public static final String OUTCOME_ISSUED = "ISSUED";
    public static final String OUTCOME_MAIL_DELIVERY_FAILED = "MAIL_DELIVERY_FAILED";
    public static final String OUTCOME_RESET_SUCCESS = "RESET_SUCCESS";
    public static final String OUTCOME_RESET_INVALID_TOKEN = "RESET_INVALID_TOKEN";
    public static final String OUTCOME_RESET_ALREADY_USED = "RESET_ALREADY_USED";
    public static final String OUTCOME_RESET_REVOKED = "RESET_REVOKED";
    public static final String OUTCOME_RESET_EXPIRED = "RESET_EXPIRED";
    public static final String OUTCOME_RESET_USER_NOT_ACTIVE = "RESET_USER_NOT_ACTIVE";

    private final PasswordResetAttemptRepository repository;

    public PasswordResetAttemptService(PasswordResetAttemptRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(String emailNorm,
                       UUID userId,
                       String requestedIp,
                       String userAgent,
                       String outcome) {
        PasswordResetAttempt attempt = new PasswordResetAttempt();
        attempt.setId(UUID.randomUUID());
        attempt.setEmailHash(sha256(emailNorm == null ? "" : emailNorm));
        attempt.setUserId(userId);
        attempt.setRequestedIp(requestedIp);
        attempt.setUserAgent(userAgent);
        attempt.setOutcome(outcome);
        repository.save(attempt);
    }

    private byte[] sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(raw.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to hash email for password reset attempt", ex);
        }
    }
}