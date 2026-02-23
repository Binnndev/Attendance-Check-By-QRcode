package com.attendance.backend.domain.entity;

import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "qr_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_qt_token_hash", columnNames = {"token_hash"}),
                @UniqueConstraint(name = "uk_qt_session_token", columnNames = {"session_id", "token_id"})
        },
        indexes = {
                @Index(name = "idx_qt_session_issued", columnList = "session_id, issued_at"),
                @Index(name = "idx_qt_session_active", columnList = "session_id, revoked_at, expires_at")
        }
)
public class QrToken {

    @Id
    @Column(name = "token_id", nullable = false, length = 64)
    private String tokenId;

    @Column(name = "session_id", columnDefinition = "BINARY(16)", nullable = false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID sessionId;

    @Column(name = "token_hash", nullable = false, columnDefinition = "VARBINARY(32)")
    private byte[] tokenHash;

    @Column(name = "issued_at", nullable = false, columnDefinition = "DATETIME(3)")
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false, columnDefinition = "DATETIME(3)")
    private Instant expiresAt;

    @Column(name = "revoked_at", columnDefinition = "DATETIME(3)")
    private Instant revokedAt;

    @Column(name = "revoked_reason", length = 255)
    private String revokedReason;

    @Column(name = "rotated_from_token_id", length = 64)
    private String rotatedFromTokenId;

    @Column(name = "issued_by_user_id", columnDefinition = "BINARY(16)")
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID issuedByUserId;

    @Column(name = "note", length = 255)
    private String note;

    public String getTokenId() { return tokenId; }
    public void setTokenId(String tokenId) { this.tokenId = tokenId; }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public byte[] getTokenHash() { return tokenHash; }
    public void setTokenHash(byte[] tokenHash) { this.tokenHash = tokenHash; }

    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }

    public String getRevokedReason() { return revokedReason; }
    public void setRevokedReason(String revokedReason) { this.revokedReason = revokedReason; }

    public String getRotatedFromTokenId() { return rotatedFromTokenId; }
    public void setRotatedFromTokenId(String rotatedFromTokenId) { this.rotatedFromTokenId = rotatedFromTokenId; }

    public UUID getIssuedByUserId() { return issuedByUserId; }
    public void setIssuedByUserId(UUID issuedByUserId) { this.issuedByUserId = issuedByUserId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}