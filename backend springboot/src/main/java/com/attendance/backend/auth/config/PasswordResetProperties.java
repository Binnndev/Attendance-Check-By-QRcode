package com.attendance.backend.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.auth.password-reset")
public class PasswordResetProperties {

    /**
     * Số phút reset token còn hiệu lực.
     */
    private int tokenMinutes = 30;

    /**
     * URL FE nhận token reset, ví dụ:
     * http://localhost:3000/reset-password
     */
    private String frontendResetUrl;

    /**
     * Giới hạn số lần forgot-password trong 1 giờ / 1 user.
     */
    private int maxRequestsPerHour = 3;

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

    public int getMaxRequestsPerHour() {
        return maxRequestsPerHour;
    }

    public void setMaxRequestsPerHour(int maxRequestsPerHour) {
        this.maxRequestsPerHour = maxRequestsPerHour;
    }
}