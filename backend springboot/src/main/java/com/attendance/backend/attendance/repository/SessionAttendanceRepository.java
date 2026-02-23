package com.attendance.backend.attendance.repository;

import com.attendance.backend.domain.entity.SessionAttendance;
import com.attendance.backend.domain.id.SessionAttendanceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface SessionAttendanceRepository extends JpaRepository<SessionAttendance, SessionAttendanceId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select sa
        from SessionAttendance sa
        where sa.id.sessionId = :sessionId
          and sa.id.userId = :userId
    """)
    Optional<SessionAttendance> findBySessionAndUserForUpdate(
            @Param("sessionId") UUID sessionId,
            @Param("userId") UUID userId
    );
}
