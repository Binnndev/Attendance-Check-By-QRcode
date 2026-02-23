package com.attendance.backend.domain.entity;

import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import com.attendance.backend.domain.enums.SessionStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "attendance_sessions",
        indexes = {
                @Index(name = "idx_as_group_date", columnList = "group_id, session_date"),
                @Index(name = "idx_as_group_created", columnList = "group_id, created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_as_group_single_open", columnNames = {"group_id", "is_open"})
        }
)
public class AttendanceSession {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID id;

    @Column(name = "group_id", columnDefinition = "BINARY(16)", nullable = false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID groupId;

    @Column(name = "created_by_user_id", columnDefinition = "BINARY(16)", nullable = false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID createdByUserId;

    @Column(name = "title", length = 150)
    private String title;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status;

    @Column(name = "is_open", insertable = false, updatable = false)
    private Byte isOpen;

    @Column(name = "start_at", nullable = false, columnDefinition = "DATETIME(3)")
    private Instant startAt;

    @Column(name = "checkin_open_at", columnDefinition = "DATETIME(3)")
    private Instant checkinOpenAt;

    @Column(name = "checkin_close_at", columnDefinition = "DATETIME(3)")
    private Instant checkinCloseAt;

    @Column(name = "end_at", columnDefinition = "DATETIME(3)")
    private Instant endAt;

    @Column(name = "time_window_minutes", nullable = false)
    private Integer timeWindowMinutes;

    @Column(name = "late_after_minutes", nullable = false)
    private Integer lateAfterMinutes;

    @Column(name = "qr_rotate_seconds", nullable = false)
    private Integer qrRotateSeconds;

    @Column(name = "session_secret", nullable = false, length = 255)
    private String sessionSecret;

    @Column(name = "allow_manual_override", nullable = false)
    private Byte allowManualOverride;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at", nullable = false, columnDefinition = "DATETIME(3)", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME(3)", insertable = false, updatable = false)
    private Instant updatedAt;

    public boolean isAllowManualOverride() {
        return allowManualOverride != null && allowManualOverride == (byte) 1;
    }

    public void setAllowManualOverride(boolean allow) {
        this.allowManualOverride = (byte) (allow ? 1 : 0);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }

    public UUID getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(UUID createdByUserId) { this.createdByUserId = createdByUserId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }

    public Byte getIsOpen() { return isOpen; }

    public Instant getStartAt() { return startAt; }
    public void setStartAt(Instant startAt) { this.startAt = startAt; }

    public Instant getCheckinOpenAt() { return checkinOpenAt; }
    public void setCheckinOpenAt(Instant checkinOpenAt) { this.checkinOpenAt = checkinOpenAt; }

    public Instant getCheckinCloseAt() { return checkinCloseAt; }
    public void setCheckinCloseAt(Instant checkinCloseAt) { this.checkinCloseAt = checkinCloseAt; }

    public Instant getEndAt() { return endAt; }
    public void setEndAt(Instant endAt) { this.endAt = endAt; }

    public Integer getTimeWindowMinutes() { return timeWindowMinutes; }
    public void setTimeWindowMinutes(Integer timeWindowMinutes) { this.timeWindowMinutes = timeWindowMinutes; }

    public Integer getLateAfterMinutes() { return lateAfterMinutes; }
    public void setLateAfterMinutes(Integer lateAfterMinutes) { this.lateAfterMinutes = lateAfterMinutes; }

    public Integer getQrRotateSeconds() { return qrRotateSeconds; }
    public void setQrRotateSeconds(Integer qrRotateSeconds) { this.qrRotateSeconds = qrRotateSeconds; }

    public String getSessionSecret() { return sessionSecret; }
    public void setSessionSecret(String sessionSecret) { this.sessionSecret = sessionSecret; }

    public Byte getAllowManualOverrideRaw() { return allowManualOverride; }
    public void setAllowManualOverrideRaw(Byte allowManualOverride) { this.allowManualOverride = allowManualOverride; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}