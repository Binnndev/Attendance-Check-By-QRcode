package com.attendance.backend.attendance.repository;

import com.attendance.backend.domain.entity.AttendanceSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {

    /** Dùng cho check-in: share lock (ít nghẽn) */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select s from AttendanceSession s where s.id = :id")
    Optional<AttendanceSession> findByIdForShare(@Param("id") UUID id);

    /** Dùng cho close/cancel: for update */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AttendanceSession s where s.id = :id")
    Optional<AttendanceSession> findByIdForUpdate(@Param("id") UUID id);
}
