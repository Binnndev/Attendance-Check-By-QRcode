package com.attendance.backend.absence.repository;

import com.attendance.backend.domain.entity.AbsenceRequest;
import com.attendance.backend.domain.enums.AbsenceRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AbsenceRequestRepository extends JpaRepository<AbsenceRequest, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ar from AbsenceRequest ar where ar.id = :id")
    Optional<AbsenceRequest> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
        select (count(ar) > 0)
        from AbsenceRequest ar
        where ar.requesterUserId = :requesterUserId
          and ar.linkedSessionId = :linkedSessionId
          and ar.requestStatus = com.attendance.backend.domain.enums.AbsenceRequestStatus.PENDING
    """)
    boolean existsPendingByRequesterAndSession(
            @Param("requesterUserId") UUID requesterUserId,
            @Param("linkedSessionId") UUID linkedSessionId
    );

    Page<AbsenceRequest> findByGroupIdOrderByCreatedAtDesc(UUID groupId, Pageable pageable);

    Page<AbsenceRequest> findByGroupIdAndRequestStatusOrderByCreatedAtDesc(
            UUID groupId,
            AbsenceRequestStatus requestStatus,
            Pageable pageable
    );

    Page<AbsenceRequest> findByRequesterUserIdOrderByCreatedAtDesc(UUID requesterUserId, Pageable pageable);

    Page<AbsenceRequest> findByRequesterUserIdAndRequestStatusOrderByCreatedAtDesc(
            UUID requesterUserId,
            AbsenceRequestStatus requestStatus,
            Pageable pageable
    );
}