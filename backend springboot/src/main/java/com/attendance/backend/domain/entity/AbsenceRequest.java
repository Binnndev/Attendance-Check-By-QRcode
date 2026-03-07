package com.attendance.backend.domain.entity;

import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import com.attendance.backend.common.persistence.MysqlUuidBinary16SwapType;
import org.hibernate.annotations.Type;
import com.attendance.backend.domain.enums.AbsenceRequestStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name="absence_requests",
        indexes = {
                @Index(name="idx_ar_group_status_created", columnList="group_id,request_status,created_at"),
                @Index(name="idx_ar_requester_created", columnList="requester_user_id,created_at"),
                @Index(name="idx_ar_session", columnList="linked_session_id")
        }
)
public class AbsenceRequest {
    @Id
    @Type(value = MysqlUuidBinary16SwapType.class)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    public UUID id;

    @Column(name="group_id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID groupId;

    @Column(name="requester_user_id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID requesterUserId;

    @Column(name="linked_session_id", columnDefinition="BINARY(16)")
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID linkedSessionId;

    @Column(name="request_status", nullable=false, length=20)
    @Enumerated(EnumType.STRING)
    public AbsenceRequestStatus requestStatus;

    @Column(name="request_reason", length=500, nullable=false)
    public String requestReason;

    @Column(name="requested_date", nullable=false)
    public LocalDate requestedDate;

    @Column(name="reviewer_user_id", columnDefinition="BINARY(16)")
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID reviewerUserId;

    @Column(name="reviewed_at")
    public Instant reviewedAt;

    @Column(name="cancelled_at")
    public Instant cancelledAt;

    @Column(name="created_at", nullable=false, insertable=false, updatable=false)
    public Instant createdAt;

    @Column(name="updated_at", nullable=false, insertable=false, updatable=false)
    public Instant updatedAt;

    public AbsenceRequest() {}

    public boolean isPending() { return requestStatus == AbsenceRequestStatus.PENDING; }
    public boolean isApproved() { return requestStatus == AbsenceRequestStatus.APPROVED; }
    public boolean isRejected() { return requestStatus == AbsenceRequestStatus.REJECTED; }
    public boolean isCancelled() { return requestStatus == AbsenceRequestStatus.CANCELLED; }
}