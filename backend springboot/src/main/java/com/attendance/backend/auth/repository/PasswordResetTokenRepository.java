package com.attendance.backend.auth.repository;

import com.attendance.backend.domain.entity.PasswordResetToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    long countByUserIdAndCreatedAtAfter(UUID userId, Instant cutoff);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        update PasswordResetToken t
           set t.revokedAt = :now,
               t.revokedReason = :reason
         where t.userId = :userId
           and t.usedAt is null
           and t.revokedAt is null
           and t.expiresAt > :now
    """)
    int revokeAllActiveByUserId(
            @Param("userId") UUID userId,
            @Param("now") Instant now,
            @Param("reason") String reason
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select t
          from PasswordResetToken t
         where t.tokenHash = :tokenHash
    """)
    Optional<PasswordResetToken> findByTokenHashForUpdate(@Param("tokenHash") byte[] tokenHash);
}