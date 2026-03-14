package com.attendance.backend.auth.service;

import com.attendance.backend.auth.config.LoginRateLimitProperties;
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
public class LoginRedisRateLimitService {

    private static final Logger log = LoggerFactory.getLogger(LoginRedisRateLimitService.class);

    public static final String REASON_THROTTLED_IP = "THROTTLED_IP";
    public static final String REASON_THROTTLED_EMAIL_IP = "THROTTLED_EMAIL_IP";
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
    private final LoginRateLimitProperties properties;

    public LoginRedisRateLimitService(StringRedisTemplate redisTemplate,
                                      LoginRateLimitProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public Decision checkBlocked(String emailNorm, String requestedIp) {
        try {
            if (StringUtils.hasText(requestedIp)) {
                long ipCount = currentCount(buildIpKey(requestedIp));
                if (ipCount >= properties.getMaxRequestsPerIp()) {
                    return Decision.block(REASON_THROTTLED_IP, ttlSeconds(buildIpKey(requestedIp)));
                }
            }

            String pairKey = buildPairKey(emailNorm, requestedIp);
            long pairCount = currentCount(pairKey);
            if (pairCount >= properties.getMaxRequestsPerEmailIp()) {
                return Decision.block(REASON_THROTTLED_EMAIL_IP, ttlSeconds(pairKey));
            }

            return Decision.allow();
        } catch (Exception ex) {
            if (properties.isFailOpen()) {
                log.warn("Redis unavailable during login pre-check. failOpen=true", ex);
                return Decision.allowDegraded(REASON_REDIS_UNAVAILABLE);
            }
            log.error("Redis unavailable during login pre-check. failOpen=false", ex);
            return Decision.block(REASON_REDIS_UNAVAILABLE, 0L);
        }
    }

    public Decision recordFailure(String emailNorm, String requestedIp) {
        try {
            if (StringUtils.hasText(requestedIp)) {
                long ipCount = increment(buildIpKey(requestedIp));
                if (ipCount > properties.getMaxRequestsPerIp()) {
                    return Decision.block(REASON_THROTTLED_IP, ttlSeconds(buildIpKey(requestedIp)));
                }
            }

            String pairKey = buildPairKey(emailNorm, requestedIp);
            long pairCount = increment(pairKey);
            if (pairCount > properties.getMaxRequestsPerEmailIp()) {
                return Decision.block(REASON_THROTTLED_EMAIL_IP, ttlSeconds(pairKey));
            }

            return Decision.allow();
        } catch (Exception ex) {
            if (properties.isFailOpen()) {
                log.warn("Redis unavailable during login failure increment. failOpen=true", ex);
                return Decision.allowDegraded(REASON_REDIS_UNAVAILABLE);
            }
            log.error("Redis unavailable during login failure increment. failOpen=false", ex);
            return Decision.block(REASON_REDIS_UNAVAILABLE, 0L);
        }
    }

    public void clearOnSuccess(String emailNorm, String requestedIp) {
        try {
            redisTemplate.delete(buildPairKey(emailNorm, requestedIp));
        } catch (Exception ex) {
            if (!properties.isFailOpen()) {
                log.error("Failed to clear login rate limit key on success", ex);
            } else {
                log.warn("Failed to clear login rate limit key on success", ex);
            }
        }
    }

    private long currentCount(String key) {
        String value = redisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(value)) {
            return 0L;
        }
        return Long.parseLong(value);
    }

    private long increment(String key) {
        long ttlMillis = Duration.ofMinutes(properties.getWindowMinutes()).toMillis();
        Long current = redisTemplate.execute(
                INCREMENT_WITH_TTL_SCRIPT,
                Collections.singletonList(key),
                String.valueOf(ttlMillis)
        );
        return current == null ? 0L : current;
    }

    private long ttlSeconds(String key) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return ttl == null || ttl < 0 ? 0L : ttl;
    }

    private String buildIpKey(String ip) {
        return properties.getRedisKeyPrefix() + ":ip:" + ip;
    }

    private String buildPairKey(String emailNorm, String ip) {
        return properties.getRedisKeyPrefix() + ":pair:" + sha256Hex(emailNorm + "|" + (ip == null ? "" : ip));
    }

    private String sha256Hex(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to hash login rate limit key", ex);
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