package com.attendance.backend.auth.service;

import com.attendance.backend.auth.config.PasswordResetProperties;
import com.attendance.backend.auth.repository.PasswordResetAttemptRepository;
import com.attendance.backend.domain.entity.PasswordResetAttempt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PasswordResetAttemptService {

    public static final String OUTCOME_ACCEPTED = "ACCEPTED";
    public static final String OUTCOME_EMAIL_NOT_FOUND = "EMAIL_NOT_FOUND";
    public static final String OUTCOME_USER_NOT_ACTIVE = "USER_NOT_ACTIVE";
    public static final String OUTCOME_THROTTLED_EMAIL = "THROTTLED_EMAIL";
    public static final String OUTCOME_THROTTLED_IP = "THROTTLED_IP";

    private final PasswordResetAttemptRepository repository;
    private final PasswordResetProperties properties;
    private final Clock clock;

    public PasswordResetAttemptService(PasswordResetAttemptRepository repository,
                                       PasswordResetProperties properties,
                                       Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public Decision evaluateAndRecord(String emailNorm,
                                      String requestedIp,
                                      String userAgent,
                                      UUID userId,
                                      boolean userExists,
                                      boolean userActive) {
        Instant now = Instant.now(clock);
        Instant cutoff = now.minus(properties.getRateLimitWindowMinutes(), ChronoUnit.MINUTES);
        byte[] emailHash = sha256(emailNorm);

        long emailCount = repository.countByEmailHashAndCreatedAtAfter(emailHash, cutoff);
        if (emailCount >= properties.getMaxRequestsPerWindowPerEmail()) {
            saveAttempt(emailHash, userId, requestedIp, userAgent, OUTCOME_THROTTLED_EMAIL);
            return Decision.deny(OUTCOME_THROTTLED_EMAIL);
        }

        if (StringUtils.hasText(requestedIp)) {
            long ipCount = repository.countByRequestedIpAndCreatedAtAfter(requestedIp, cutoff);
            if (ipCount >= properties.getMaxRequestsPerWindowPerIp()) {
                saveAttempt(emailHash, userId, requestedIp, userAgent, OUTCOME_THROTTLED_IP);
                return Decision.deny(OUTCOME_THROTTLED_IP);
            }
        }

        if (!userExists) {
            saveAttempt(emailHash, null, requestedIp, userAgent, OUTCOME_EMAIL_NOT_FOUND);
            return Decision.deny(OUTCOME_EMAIL_NOT_FOUND);
        }

        if (!userActive) {
            saveAttempt(emailHash, userId, requestedIp, userAgent, OUTCOME_USER_NOT_ACTIVE);
            return Decision.deny(OUTCOME_USER_NOT_ACTIVE);
        }

        saveAttempt(emailHash, userId, requestedIp, userAgent, OUTCOME_ACCEPTED);
        return Decision.permit();
    }

    private void saveAttempt(byte[] emailHash,
                             UUID userId,
                             String requestedIp,
                             String userAgent,
                             String outcome) {
        PasswordResetAttempt attempt = new PasswordResetAttempt();
        attempt.setId(UUID.randomUUID());
        attempt.setEmailHash(emailHash);
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

    public record Decision(boolean allowed, String reason) {
        public static Decision permit() {
            return new Decision(true, OUTCOME_ACCEPTED);
        }

        public static Decision deny(String reason) {
            return new Decision(false, reason);
        }
    }
}