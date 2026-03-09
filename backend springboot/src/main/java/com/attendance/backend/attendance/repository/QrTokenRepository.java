package com.attendance.backend.attendance.repository;

import com.attendance.backend.domain.entity.QrToken;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.*;

public interface QrTokenRepository extends JpaRepository<QrToken, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select t
        from QrToken t
        where t.sessionId = :sessionId
          and t.revokedAt is null
          and (t.expiresAt is null or t.expiresAt > :now)
        order by t.issuedAt desc
    """)
    List<QrToken> findActiveForUpdate(
            @Param("sessionId") UUID sessionId,
            @Param("now") Instant now,
            PageRequest page
    );

    default Optional<QrToken> findLatestActiveForUpdate(UUID sessionId, Instant now) {
        return findActiveForUpdate(sessionId, now, PageRequest.of(0, 1))
                .stream()
                .findFirst();
    }

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update QrToken t
           set t.revokedAt = :now,
               t.revokedReason = :reason
         where t.sessionId = :sessionId
           and t.revokedAt is null
           and (t.expiresAt is null or t.expiresAt > :now)
    """)
    int revokeActiveTokens(
            @Param("sessionId") UUID sessionId,
            @Param("now") Instant now,
            @Param("reason") String reason
    );
}
