package com.attendance.backend.domain.entity;

import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import com.attendance.backend.domain.id.SessionAttendanceId;
import com.attendance.backend.domain.enums.AttendanceStatus;
import com.attendance.backend.domain.enums.CheckInMethod;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="session_attendance",
        indexes = {
                @Index(name="idx_sa_user_created", columnList="user_id,created_at"),
                @Index(name="idx_sa_session_status", columnList="session_id,attendance_status"),
                @Index(name="idx_sa_checkin", columnList="session_id,check_in_at"),
                @Index(name="idx_sa_session_qr_token", columnList="session_id,qr_token_id")
        }
)
public class SessionAttendance {
    @EmbeddedId
    public SessionAttendanceId id;

    @Enumerated(EnumType.STRING)
    @Column(name="attendance_status", nullable=false)
    public AttendanceStatus attendanceStatus = AttendanceStatus.ABSENT;

    @Column(name="check_in_at")
    public Instant checkInAt;

    @Enumerated(EnumType.STRING)
    @Column(name="check_in_method", nullable=false)
    public CheckInMethod checkInMethod = CheckInMethod.QR;

    @Column(name="qr_token_id", length=64)
    public String qrTokenId;

    @Column(name="device_id", length=120)
    public String deviceId;

    @Column(name="ip_address", length=45)
    public String ipAddress;

    @Column(name="user_agent", length=255)
    public String userAgent;

    @Column(name="geo_lat", precision=10, scale=7)
    public BigDecimal geoLat;

    @Column(name="geo_lng", precision=10, scale=7)
    public BigDecimal geoLng;

    @Column(name="distance_meter")
    public Integer distanceMeter;

    @Column(name="suspicious_flag", nullable=false)
    public boolean suspiciousFlag;

    @Column(name="suspicious_reason", length=500)
    public String suspiciousReason;

    @Column(name="excused_by_request_id", columnDefinition="BINARY(16)")
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID excusedByRequestId;

    @Column(name="created_at", nullable=false)
    public Instant createdAt;

    @Column(name="updated_at", nullable=false)
    public Instant updatedAt;

    public static SessionAttendance createNew(UUID sessionId, UUID userId) {
        SessionAttendance sa = new SessionAttendance();
        sa.id = new SessionAttendanceId(sessionId, userId);
        sa.attendanceStatus = AttendanceStatus.ABSENT;
        sa.checkInMethod = CheckInMethod.QR;
        sa.suspiciousFlag = false;
        return sa;
    }
}
