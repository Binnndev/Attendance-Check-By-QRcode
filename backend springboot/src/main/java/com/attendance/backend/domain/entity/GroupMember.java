package com.attendance.backend.domain.entity;

import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import com.attendance.backend.domain.enums.MemberRole;
import com.attendance.backend.domain.enums.MemberStatus;
import com.attendance.backend.domain.id.GroupMemberId;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "group_members",
        indexes = {
                @Index(name = "idx_gm_user", columnList = "user_id"),
                @Index(name = "idx_gm_group_status_role", columnList = "group_id, member_status, role"),
                @Index(name = "idx_gm_group_joined", columnList = "group_id, joined_at")
        }
)
public class GroupMember {

    @EmbeddedId
    private GroupMemberId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status", nullable = false, length = 20)
    private MemberStatus memberStatus;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "invited_by", columnDefinition = "BINARY(16)")
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID invitedBy;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    @Column(name = "removed_at")
    private Instant removedAt;

    public GroupMember() {
    }

    public static GroupMember newMember(UUID groupId, UUID userId, MemberStatus memberStatus, Instant joinedAt) {
        GroupMember gm = new GroupMember();
        gm.setId(new GroupMemberId(groupId, userId));
        gm.setRole(MemberRole.MEMBER);
        gm.setMemberStatus(memberStatus);
        gm.setJoinedAt(joinedAt);
        gm.setInvitedBy(null);
        gm.setRemovedAt(null);
        return gm;
    }

    public boolean isApproved() {
        return memberStatus == MemberStatus.APPROVED;
    }

    public boolean isPending() {
        return memberStatus == MemberStatus.PENDING;
    }

    public boolean isRemoved() {
        return memberStatus == MemberStatus.REMOVED;
    }

    public GroupMemberId getId() {
        return id;
    }

    public void setId(GroupMemberId id) {
        this.id = id;
    }

    public UUID getGroupId() {
        return id == null ? null : id.getGroupId();
    }

    public void setGroupId(UUID groupId) {
        if (this.id == null) {
            this.id = new GroupMemberId();
        }
        this.id.setGroupId(groupId);
    }

    public UUID getUserId() {
        return id == null ? null : id.getUserId();
    }

    public void setUserId(UUID userId) {
        if (this.id == null) {
            this.id = new GroupMemberId();
        }
        this.id.setUserId(userId);
    }

    public MemberRole getRole() {
        return role;
    }

    public void setRole(MemberRole role) {
        this.role = role;
    }

    public MemberStatus getMemberStatus() {
        return memberStatus;
    }

    public void setMemberStatus(MemberStatus memberStatus) {
        this.memberStatus = memberStatus;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public UUID getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(UUID invitedBy) {
        this.invitedBy = invitedBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getRemovedAt() {
        return removedAt;
    }

    public void setRemovedAt(Instant removedAt) {
        this.removedAt = removedAt;
    }
}