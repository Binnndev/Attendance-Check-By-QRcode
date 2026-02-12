package com.attendance.backend.domain.entity;

import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="qr_tokens",
        indexes = {
                @Index(name="idx_qt_session_issued", columnList="session_id,issued_at"),
                @Index(name="idx_qt_session_active", columnList="session_id,revoked_at,expires_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name="uk_qt_token_hash", columnNames="token_hash"),
                @UniqueConstraint(name="uk_qt_session_token", columnNames={"session_id","token_id"})
        }
)
public class QrToken {
    @Id
    @Column(name="token_id", length=64, nullable=false)
    private String tokenId;

    @Column(name="session_id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID sessionId;

    @Column(name="token_hash", columnDefinition="VARBINARY(32)", nullable=false)
    private byte[] tokenHash;

    @Column(name="issued_at", nullable=false)
    private Instant issuedAt;

    @Column(name="expires_at", nullable=false)
    private Instant expiresAt;

    @Column(name="revoked_at")
    private Instant revokedAt;

    @Column(name="revoked_reason", length=255)
    private String revokedReason;

    @Column(name="rotated_from_token_id", length=64)
    private String rotatedFromTokenId;

    @Column(name="issued_by_user_id", columnDefinition="BINARY(16)")
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID issuedByUserId;

    @Column(name="note", length=255)
    private String note;

}
