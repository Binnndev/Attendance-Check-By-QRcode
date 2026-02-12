package com.attendance.backend.domain.entity;

import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
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
    @Column(name="id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
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

    @Column(name="requested_date")
    public LocalDate requestedDate;

    @Column(name="reason", length=500, nullable=false)
    public String reason;

    @Column(name="evidence_url", length=500)
    public String evidenceUrl;

    @Enumerated(EnumType.STRING)
    @Column(name="request_status", nullable=false)
    public AbsenceRequestStatus requestStatus = AbsenceRequestStatus.PENDING;

    @Column(name="reviewer_user_id", columnDefinition="BINARY(16)")
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID reviewerUserId;

    @Column(name="reviewer_note", length=500)
    public String reviewerNote;

    @Column(name="reviewed_at")
    public Instant reviewedAt;

    @Column(name="created_at", nullable=false)
    public Instant createdAt;

    @Column(name="updated_at", nullable=false)
    public Instant updatedAt;

    @Column(name="cancelled_at")
    public Instant cancelledAt;
}
