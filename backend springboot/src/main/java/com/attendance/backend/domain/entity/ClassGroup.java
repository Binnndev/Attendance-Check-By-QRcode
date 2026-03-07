package com.attendance.backend.domain.entity;

import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import com.attendance.backend.common.persistence.MysqlUuidBinary16SwapType;
import org.hibernate.annotations.Type;
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
    @Type(value = MysqlUuidBinary16SwapType.class)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name="owner_user_id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID ownerUserId;

    @Column(name="name", length=150, nullable=false)
    private String name;

    @Column(name="description", length=500)
    private String description;

    @Column(name="code", length=32, nullable=false)
    private String code;

    @Column(name="join_code", length=32, nullable=false)
    private String joinCode;

    @Enumerated(EnumType.STRING)
    @Column(name="approval_mode", nullable=false, length=20)
    private ApprovalMode approvalMode;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false, length=20)
    private GroupStatus status;

    @Column(name="allow_auto_join_on_checkin", nullable=false)
    private boolean allowAutoJoinOnCheckin;

    @Column(name="created_at", nullable=false, insertable=false, updatable=false)
    private Instant createdAt;

    @Column(name="updated_at", nullable=false, insertable=false, updatable=false)
    private Instant updatedAt;

    @Column(name="deleted_at")
    private Instant deletedAt;

    public ClassGroup() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(UUID ownerUserId) { this.ownerUserId = ownerUserId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getJoinCode() { return joinCode; }
    public void setJoinCode(String joinCode) { this.joinCode = joinCode; }

    public ApprovalMode getApprovalMode() { return approvalMode; }
    public void setApprovalMode(ApprovalMode approvalMode) { this.approvalMode = approvalMode; }

    public GroupStatus getStatus() { return status; }
    public void setStatus(GroupStatus status) { this.status = status; }

    public boolean isAllowAutoJoinOnCheckin() { return allowAutoJoinOnCheckin; }
    public void setAllowAutoJoinOnCheckin(boolean allowAutoJoinOnCheckin) { this.allowAutoJoinOnCheckin = allowAutoJoinOnCheckin; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}