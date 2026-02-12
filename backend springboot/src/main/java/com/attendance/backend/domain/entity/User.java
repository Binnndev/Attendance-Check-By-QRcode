package com.attendance.backend.domain.entity;

import com.attendance.backend.common.persistence.UuidBinary16SwapConverter;
import com.attendance.backend.domain.enums.PlatformRole;
import com.attendance.backend.domain.enums.UserStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users",
        indexes = {
                @Index(name="idx_users_role_status", columnList="platform_role,status"),
                @Index(name="idx_users_name", columnList="full_name")
        },
        uniqueConstraints = {
                @UniqueConstraint(name="uk_users_email_norm", columnNames="email_norm"),
                @UniqueConstraint(name="uk_users_user_code", columnNames="user_code")
        }
)
public class User {
    @Id
    @Column(name="id", columnDefinition="BINARY(16)", nullable=false)
    @Convert(converter = UuidBinary16SwapConverter.class)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name="platform_role", nullable=false)
    private PlatformRole platformRole = PlatformRole.USER;

    @Column(name="email", length=190, nullable=false)
    private String email;

    @Column(name="email_norm", length=190, insertable=false, updatable=false)
    private String emailNorm;

    @Column(name="password_hash", length=255, nullable=false)
    private String passwordHash;

    @Column(name="full_name", length=120, nullable=false)
    private String fullName;

    @Column(name="avatar_url", length=500)
    private String avatarUrl;

    @Column(name="user_code", length=40)
    private String userCode;

    @Column(name="primary_device_id", length=120)
    private String primaryDeviceId;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;

    @Column(name="deleted_at")
    private Instant deletedAt;

}
