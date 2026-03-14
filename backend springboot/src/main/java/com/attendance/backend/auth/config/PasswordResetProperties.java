package com.attendance.backend.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.auth.password-reset")
public class PasswordResetProperties {

    /**
     * Token reset password sống bao nhiêu phút.
     */
    private int tokenMinutes = 30;

    /**
     * FE reset password URL.
     * Ví dụ: https://app.example.com/reset-password
     */
    private String frontendResetUrl;

    /**
     * Window rate limit tính theo phút.
     */
    private int rateLimitWindowMinutes = 60;

    /**
     * Số request forgot-password tối đa / window / email hash.
     */
    private int maxRequestsPerWindowPerEmail = 3;

    /**
     * Số request forgot-password tối đa / window / IP.
     */
    private int maxRequestsPerWindowPerIp = 20;

    public int getTokenMinutes() {
        return tokenMinutes;
    }

    public void setTokenMinutes(int tokenMinutes) {
        this.tokenMinutes = tokenMinutes;
    }

    public String getFrontendResetUrl() {
        return frontendResetUrl;
    }

    public void setFrontendResetUrl(String frontendResetUrl) {
        this.frontendResetUrl = frontendResetUrl;
    }

    public int getRateLimitWindowMinutes() {
        return rateLimitWindowMinutes;
    }

    public void setRateLimitWindowMinutes(int rateLimitWindowMinutes) {
        this.rateLimitWindowMinutes = rateLimitWindowMinutes;
    }

    public int getMaxRequestsPerWindowPerEmail() {
        return maxRequestsPerWindowPerEmail;
    }

    public void setMaxRequestsPerWindowPerEmail(int maxRequestsPerWindowPerEmail) {
        this.maxRequestsPerWindowPerEmail = maxRequestsPerWindowPerEmail;
    }

    public int getMaxRequestsPerWindowPerIp() {
        return maxRequestsPerWindowPerIp;
    }

    public void setMaxRequestsPerWindowPerIp(int maxRequestsPerWindowPerIp) {
        this.maxRequestsPerWindowPerIp = maxRequestsPerWindowPerIp;
    }
}