package com.attendance.backend.attendance.repository;

import com.attendance.backend.domain.entity.SessionAttendance;
import com.attendance.backend.domain.id.SessionAttendanceId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SessionAttendanceRepository extends JpaRepository<SessionAttendance, SessionAttendanceId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select a
        from SessionAttendance a
        where a.id.sessionId = :sessionId
          and a.id.userId = :userId
    """)
    Optional<SessionAttendance> findBySessionAndUserForUpdate(@Param("sessionId") UUID sessionId,
                                                              @Param("userId") UUID userId);
}
