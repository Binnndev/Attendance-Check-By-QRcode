package com.attendance.backend.domain.entity;

import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class SessionAttendanceId implements Serializable {
    @Column(name="session_id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID sessionId;

    @Column(name="user_id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID userId;

    public SessionAttendanceId() {}
    public SessionAttendanceId(UUID sessionId, UUID userId) { this.sessionId = sessionId; this.userId = userId; }
}
