package com.attendance.backend.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.auth.login-rate-limit")
public class LoginRateLimitProperties {

    private String redisKeyPrefix = "auth:login:rl";
    private int windowMinutes = 15;
    private int maxRequestsPerIp = 20;
    private int maxRequestsPerEmailIp = 5;
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

    public int getMaxRequestsPerIp() {
        return maxRequestsPerIp;
    }

    public void setMaxRequestsPerIp(int maxRequestsPerIp) {
        this.maxRequestsPerIp = maxRequestsPerIp;
    }

    public int getMaxRequestsPerEmailIp() {
        return maxRequestsPerEmailIp;
    }

    public void setMaxRequestsPerEmailIp(int maxRequestsPerEmailIp) {
        this.maxRequestsPerEmailIp = maxRequestsPerEmailIp;
    }

    public boolean isFailOpen() {
        return failOpen;
    }

    public void setFailOpen(boolean failOpen) {
        this.failOpen = failOpen;
    }
}