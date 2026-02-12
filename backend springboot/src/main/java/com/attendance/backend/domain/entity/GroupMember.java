package com.attendance.backend.domain.entity;

import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import com.attendance.backend.domain.enums.MemberRole;
import com.attendance.backend.domain.enums.MemberStatus;
import com.attendance.backend.domain.id.GroupMemberId;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="group_members",
        indexes = {
                @Index(name="idx_gm_user", columnList="user_id"),
                @Index(name="idx_gm_group_status_role", columnList="group_id,member_status,role"),
                @Index(name="idx_gm_group_joined", columnList="group_id,joined_at")
        }
)
public class GroupMember {
    @EmbeddedId
    public GroupMemberId id;

    @Enumerated(EnumType.STRING)
    @Column(name="role", nullable=false)
    public MemberRole role = MemberRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(name="member_status", nullable=false)
    public MemberStatus memberStatus = MemberStatus.APPROVED;

    @Column(name="joined_at")
    public Instant joinedAt;

    @Column(name="invited_by", columnDefinition="BINARY(16)")
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID invitedBy;

    @Column(name="created_at", nullable=false)
    public Instant createdAt;

    @Column(name="updated_at", nullable=false)
    public Instant updatedAt;

    @Column(name="removed_at")
    public Instant removedAt;
}
