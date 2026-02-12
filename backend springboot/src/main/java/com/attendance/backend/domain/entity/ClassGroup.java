package com.attendance.backend.domain.entity;

import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import com.attendance.backend.domain.enums.ApprovalMode;
import com.attendance.backend.domain.enums.GroupStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="class_groups",
        indexes = {
                @Index(name="idx_groups_owner", columnList="owner_user_id"),
                @Index(name="idx_groups_status_created", columnList="status,created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name="uk_groups_code", columnNames="code"),
                @UniqueConstraint(name="uk_groups_join_code", columnNames="join_code")
        }
)
public class ClassGroup {
    @Id
    @Column(name="id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID id;

    @Column(name="owner_user_id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID ownerUserId;

    @Column(name="name", length=150, nullable=false)
    private String name;

    @Column(name="code", length=20, nullable=false)
    private String code;

    @Column(name="join_code", length=16, nullable=false)
    private String joinCode;

    @Column(name="description", length=1000)
    private String description;

    @Column(name="semester", length=30)
    private String semester;

    @Column(name="room", length=80)
    private String room;

    @Enumerated(EnumType.STRING)
    @Column(name="approval_mode", nullable=false)
    private ApprovalMode approvalMode = ApprovalMode.AUTO;

    @Column(name="allow_auto_join_on_checkin", nullable=false)
    private boolean allowAutoJoinOnCheckin;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false)
    private GroupStatus status = GroupStatus.ACTIVE;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;

    @Column(name="deleted_at")
    private Instant deletedAt;
}
