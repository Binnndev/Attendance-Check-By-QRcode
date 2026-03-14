package com.attendance.backend.mail;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, UUID> {

    @Query(value = """
        select *
        from email_outbox eo
        where eo.status in ('PENDING', 'RETRY')
          and eo.next_attempt_at <= :now
        order by eo.next_attempt_at asc
        limit :limit
        for update skip locked
        """, nativeQuery = true)
    List<EmailOutbox> findDueForClaim(@Param("now") Instant now, @Param("limit") int limit);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EmailOutbox e where e.id = :id")
    Optional<EmailOutbox> findByIdForUpdate(@Param("id") UUID id);

    long countByStatusAndCreatedAtAfter(String status, Instant since);

    Page<EmailOutbox> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}