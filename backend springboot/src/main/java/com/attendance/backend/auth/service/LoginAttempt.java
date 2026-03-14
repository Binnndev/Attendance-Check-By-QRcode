package com.attendance.backend.auth.service;

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
        name = "login_attempts",
        indexes = {
                @Index(name = "idx_la_email_created", columnList = "email_hash, created_at"),
                @Index(name = "idx_la_ip_created", columnList = "requested_ip, created_at"),
                @Index(name = "idx_la_user_created", columnList = "user_id, created_at"),
                @Index(name = "idx_la_outcome_created", columnList = "outcome, created_at")
        }
)
public class LoginAttempt {

    @Id
    @Type(value = MysqlUuidBinary16SwapType.class)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    public UUID id;

    @Column(name = "email_hash", nullable = false, columnDefinition = "VARBINARY(32)")
    public byte[] emailHash;

    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID userId;

    @Column(name = "requested_ip", length = 45)
    public String requestedIp;

    @Column(name = "user_agent", length = 255)
    public String userAgent;

    @Column(name = "outcome", nullable = false, length = 32)
    public String outcome;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    public Instant createdAt;
}