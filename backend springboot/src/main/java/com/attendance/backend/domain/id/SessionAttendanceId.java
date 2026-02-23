package com.attendance.backend.domain.id;

import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class SessionAttendanceId implements Serializable {

    @Column(name = "session_id", columnDefinition = "BINARY(16)", nullable = false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID sessionId;

    @Column(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID userId;

    public SessionAttendanceId() {}

    public SessionAttendanceId(UUID sessionId, UUID userId) {
        this.sessionId = sessionId;
        this.userId = userId;
    }

    public UUID getSessionId() { return sessionId; }
    public UUID getUserId() { return userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SessionAttendanceId that)) return false;
        return Objects.equals(sessionId, that.sessionId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, userId);
    }
}