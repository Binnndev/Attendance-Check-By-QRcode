package com.attendance.backend.domain.entity;

import com.attendance.backend.common.persistence.MysqlUuidBinary16SwapType;
import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "password_reset_attempts",
        indexes = {
                @Index(name = "idx_pra_email_created", columnList = "email_hash, created_at"),
                @Index(name = "idx_pra_ip_created", columnList = "requested_ip, created_at"),
                @Index(name = "idx_pra_user_created", columnList = "user_id, created_at"),
                @Index(name = "idx_pra_outcome_created", columnList = "outcome, created_at")
        }
)
public class PasswordResetAttempt {

    @Id
    @Type(value = MysqlUuidBinary16SwapType.class)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "email_hash", nullable = false, columnDefinition = "VARBINARY(32)")
    private byte[] emailHash;

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID userId;

    @Column(name = "requested_ip", length = 45)
    private String requestedIp;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "outcome", nullable = false, length = 32)
    private String outcome;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    public PasswordResetAttempt() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public byte[] getEmailHash() {
        return emailHash;
    }

    public void setEmailHash(byte[] emailHash) {
        this.emailHash = emailHash;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getRequestedIp() {
        return requestedIp;
    }

    public void setRequestedIp(String requestedIp) {
        this.requestedIp = requestedIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}