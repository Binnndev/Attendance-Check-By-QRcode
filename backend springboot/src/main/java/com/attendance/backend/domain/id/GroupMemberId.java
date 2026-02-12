package com.attendance.backend.domain.id;

import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class GroupMemberId implements Serializable {
    @Column(name="group_id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID groupId;

    @Column(name="user_id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    public UUID userId;

    public GroupMemberId() {}
    public GroupMemberId(UUID groupId, UUID userId) { this.groupId = groupId; this.userId = userId; }
}
