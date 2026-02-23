package com.attendance.backend.attendance.repository;

import com.attendance.backend.domain.entity.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, UUID> {

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select s from AttendanceSession s where s.id = :id")
    Optional<AttendanceSession> findByIdForShare(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AttendanceSession s where s.id = :id")
    Optional<AttendanceSession> findByIdForUpdate(@Param("id") UUID id);
}
