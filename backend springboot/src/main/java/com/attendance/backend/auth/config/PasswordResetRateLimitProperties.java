package com.attendance.backend.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.auth.password-reset.rate-limit")
public class PasswordResetRateLimitProperties {

    /**
     * Prefix cho Redis keys.
     */
    private String redisKeyPrefix = "auth:password-reset:rl";

    /**
     * Window rate limit tính theo phút.
     */
    private int windowMinutes = 60;

    /**
     * Tối đa bao nhiêu request / window / email.
     */
    private int maxRequestsPerEmail = 3;

    /**
     * Tối đa bao nhiêu request / window / IP.
     */
    private int maxRequestsPerIp = 20;

    /**
     * Nếu Redis lỗi:
     * - true  => cho đi tiếp (dev/local friendly)
     * - false => chặn request (prod security-first)
     */
    private boolean failOpen = true;

    public String getRedisKeyPrefix() {
        return redisKeyPrefix;
    }

    public void setRedisKeyPrefix(String redisKeyPrefix) {
        this.redisKeyPrefix = redisKeyPrefix;
    }

    public int getWindowMinutes() {
        return windowMinutes;
    }

    public void setWindowMinutes(int windowMinutes) {
        this.windowMinutes = windowMinutes;
    }

    public int getMaxRequestsPerEmail() {
        return maxRequestsPerEmail;
    }

    public void setMaxRequestsPerEmail(int maxRequestsPerEmail) {
        this.maxRequestsPerEmail = maxRequestsPerEmail;
    }

    public int getMaxRequestsPerIp() {
        return maxRequestsPerIp;
    }

    public void setMaxRequestsPerIp(int maxRequestsPerIp) {
        this.maxRequestsPerIp = maxRequestsPerIp;
    }

    public boolean isFailOpen() {
        return failOpen;
    }

    public void setFailOpen(boolean failOpen) {
        this.failOpen = failOpen;
    }
}
