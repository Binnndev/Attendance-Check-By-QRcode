package com.attendance.backend.domain.entity;

import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import com.attendance.backend.domain.enums.AttendanceStatus;
import com.attendance.backend.domain.enums.EventType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="attendance_events",
        indexes = {
                @Index(name="idx_ae_session_user_time", columnList="session_id,user_id,created_at"),
                @Index(name="idx_ae_actor_time", columnList="actor_user_id,created_at"),
                @Index(name="idx_ae_type_time", columnList="event_type,created_at"),
                @Index(name="idx_ae_qr_token", columnList="qr_token_id")
        }
)
public class AttendanceEvent {
    @Id
    @Column(name="id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID id;

    @Column(name="session_id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID sessionId;

    @Column(name="user_id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID userId;

    @Column(name="actor_user_id", columnDefinition="BINARY(16)")
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name="event_type", nullable=false)
    public EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name="old_status")
    public AttendanceStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name="new_status")
    public AttendanceStatus newStatus;

    @Column(name="qr_token_id", length=64)
    public String qrTokenId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name="event_payload", columnDefinition="JSON")
    public JsonNode eventPayload;

    @Column(name="created_at", nullable=false)
    public Instant createdAt;
}
