package com.attendance.backend.auth.repository;

import com.attendance.backend.domain.entity.UserSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from UserSession s where s.id = :id")
    Optional<UserSession> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
        select s
        from UserSession s
        where s.id = :id
          and s.revokedAt is null
          and s.expiresAt > :now
    """)
    Optional<UserSession> findActiveById(@Param("id") UUID id, @Param("now") Instant now);

    @Modifying
    @Query("""
        update UserSession s
           set s.revokedAt = :now,
               s.revokedReason = :reason
         where s.userId = :userId
           and s.revokedAt is null
           and s.expiresAt > :now
    """)
    int revokeAllActiveByUserId(@Param("userId") UUID userId,
                                @Param("now") Instant now,
                                @Param("reason") String reason);
}