package com.attendance.backend.domain.entity;

import com.attendance.backend.domain.enums.SessionStatus;
import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="attendance_sessions",
        indexes = {
                @Index(name="idx_as_group_date", columnList="group_id,session_date"),
                @Index(name="idx_as_group_created", columnList="group_id,created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name="uk_as_group_single_open", columnNames={"group_id","is_open"})
        }
)
public class AttendanceSession {
    @Id
    @Column(name="id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID id;

    @Column(name="group_id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID groupId;

    @Column(name="created_by_user_id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID createdByUserId;

    @Column(name="title", length=150)
    private String title;

    @Column(name="session_date", nullable=false)
    private LocalDate sessionDate;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false)
    private SessionStatus status = SessionStatus.OPEN;

    @Column(name="is_open", insertable=false, updatable=false)
    private Integer isOpen;

    @Column(name="start_at", nullable=false)
    private Instant startAt;

    @Column(name="checkin_open_at")
    private Instant checkinOpenAt;

    @Column(name="checkin_close_at")
    private Instant checkinCloseAt;

    @Column(name="end_at")
    private Instant endAt;

    @Column(name="time_window_minutes", nullable=false)
    private int timeWindowMinutes = 15;

    @Column(name="late_after_minutes", nullable=false)
    private int lateAfterMinutes = 5;

    @Column(name="qr_rotate_seconds", nullable=false)
    private int qrRotateSeconds = 15;

    @Column(name="session_secret", length=255, nullable=false)
    private String sessionSecret;

    @Column(name="allow_manual_override", nullable=false)
    private boolean allowManualOverride = true;

    @Column(name="note", length=500)
    private String note;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;
}
