package com.attendance.backend.attendance.repository;

import com.attendance.backend.domain.entity.AttendanceEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, UUID> {

    List<AttendanceEvent> findBySessionIdOrderByCreatedAtDesc(UUID sessionId, Pageable pageable);

    List<AttendanceEvent> findBySessionIdAndUserIdOrderByCreatedAtDesc(
            UUID sessionId,
            UUID userId,
            Pageable pageable
    );
}