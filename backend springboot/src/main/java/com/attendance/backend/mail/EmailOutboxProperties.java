package com.attendance.backend.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.auth.email-outbox")
public class EmailOutboxProperties {

    private boolean enabled = true;
    private long pollDelayMs = 5000;
    private int batchSize = 20;
    private int maxAttempts = 5;
    private long baseBackoffSeconds = 30;
    private String encryptionKeyBase64;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getPollDelayMs() {
        return pollDelayMs;
    }

    public void setPollDelayMs(long pollDelayMs) {
        this.pollDelayMs = pollDelayMs;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public long getBaseBackoffSeconds() {
        return baseBackoffSeconds;
    }

    public void setBaseBackoffSeconds(long baseBackoffSeconds) {
        this.baseBackoffSeconds = baseBackoffSeconds;
    }

    public String getEncryptionKeyBase64() {
        return encryptionKeyBase64;
    }

    public void setEncryptionKeyBase64(String encryptionKeyBase64) {
        this.encryptionKeyBase64 = encryptionKeyBase64;
    }
}