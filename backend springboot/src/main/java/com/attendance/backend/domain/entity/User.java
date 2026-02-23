package com.attendance.backend.domain.entity;

import com.attendance.backend.domain.enums.PlatformRole;
import com.attendance.backend.domain.enums.UserStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email_norm", columnNames = {"email_norm"}),
                @UniqueConstraint(name = "uk_users_user_code", columnNames = {"user_code"})
        },
        indexes = {
                @Index(name = "idx_users_role_status", columnList = "platform_role,status"),
                @Index(name = "idx_users_name", columnList = "full_name")
        }
)
public class User {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform_role", nullable = false, length = 10)
    private PlatformRole platformRole;

    @Column(name = "email", nullable = false, length = 190)
    private String email;

    @Column(name = "email_norm", length = 190, insertable = false, updatable = false)
    private String emailNorm;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "user_code", length = 40)
    private String userCode;

    @Column(name = "primary_device_id", length = 120)
    private String primaryDeviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private UserStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public PlatformRole getPlatformRole() { return platformRole; }
    public void setPlatformRole(PlatformRole platformRole) { this.platformRole = platformRole; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEmailNorm() { return emailNorm; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }

    public String getPrimaryDeviceId() { return primaryDeviceId; }
    public void setPrimaryDeviceId(String primaryDeviceId) { this.primaryDeviceId = primaryDeviceId; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}