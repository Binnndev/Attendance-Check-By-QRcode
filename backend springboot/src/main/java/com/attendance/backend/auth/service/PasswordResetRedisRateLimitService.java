package com.attendance.backend.auth.service;

import com.attendance.backend.auth.config.PasswordResetRateLimitProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Collections;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

@Service
public class PasswordResetRedisRateLimitService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetRedisRateLimitService.class);

    public static final String REASON_THROTTLED_EMAIL = "THROTTLED_EMAIL";
    public static final String REASON_THROTTLED_IP = "THROTTLED_IP";
    public static final String REASON_REDIS_UNAVAILABLE = "REDIS_UNAVAILABLE";

    private static final DefaultRedisScript<Long> INCREMENT_WITH_TTL_SCRIPT = new DefaultRedisScript<>(
            """
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
                redis.call('PEXPIRE', KEYS[1], ARGV[1])
            end
            return current
            """,
            Long.class
    );

    private final StringRedisTemplate redisTemplate;
    private final PasswordResetRateLimitProperties properties;

    public PasswordResetRedisRateLimitService(StringRedisTemplate redisTemplate,
                                              PasswordResetRateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public Decision evaluate(String emailNorm, String requestedIp) {
        Decision emailDecision = checkCounter(
                buildEmailKey(emailNorm),
                properties.getMaxRequestsPerEmail(),
                REASON_THROTTLED_EMAIL
        );
        if (!emailDecision.allowed()) {
            return emailDecision;
        }

        if (StringUtils.hasText(requestedIp)) {
            Decision ipDecision = checkCounter(
                    buildIpKey(requestedIp),
                    properties.getMaxRequestsPerIp(),
                    REASON_THROTTLED_IP
            );
            if (!ipDecision.allowed()) {
                return ipDecision;
            }
        }

        return Decision.allow();
    }

    private Decision checkCounter(String key, int limit, String denyReason) {
        try {
            long ttlMillis = Duration.ofMinutes(properties.getWindowMinutes()).toMillis();

            Long current = redisTemplate.execute(
                    INCREMENT_WITH_TTL_SCRIPT,
                    Collections.singletonList(key),
                    String.valueOf(ttlMillis)
            );

            long currentValue = current == null ? 0L : current;
            if (currentValue <= limit) {
                return Decision.allow();
            }

            Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            long retryAfterSeconds = ttlSeconds == null || ttlSeconds < 0 ? 0L : ttlSeconds;

            return Decision.block(denyReason, retryAfterSeconds);
        } catch (Exception ex) {
            if (properties.isFailOpen()) {
                log.warn("Redis unavailable during password reset rate limiting. failOpen=true", ex);
                return Decision.allowDegraded(REASON_REDIS_UNAVAILABLE);
            }

            log.error("Redis unavailable during password reset rate limiting. failOpen=false", ex);
            return Decision.block(REASON_REDIS_UNAVAILABLE, 0L);
        }
    }

    private String buildEmailKey(String emailNorm) {
        return properties.getRedisKeyPrefix() + ":email:" + sha256Hex(emailNorm);
    }

    private String buildIpKey(String requestedIp) {
        return properties.getRedisKeyPrefix() + ":ip:" + requestedIp;
    }

    private String sha256Hex(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to hash value for Redis rate limit key", ex);
        }
    }

    public record Decision(boolean allowed,
                           String reason,
                           long retryAfterSeconds,
                           boolean degraded) {

        public static Decision allow() {
            return new Decision(true, "ALLOWED", 0L, false);
        }

        public static Decision allowDegraded(String reason) {
            return new Decision(true, reason, 0L, true);
        }

        public static Decision block(String reason, long retryAfterSeconds) {
            return new Decision(false, reason, retryAfterSeconds, false);
        }
    }
}