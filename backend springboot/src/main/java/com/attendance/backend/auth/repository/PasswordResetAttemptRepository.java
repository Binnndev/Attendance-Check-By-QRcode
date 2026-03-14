package com.attendance.backend.auth.repository;

import com.attendance.backend.domain.entity.PasswordResetAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface PasswordResetAttemptRepository extends JpaRepository<PasswordResetAttempt, UUID> {

    long countByEmailHashAndCreatedAtAfter(byte[] emailHash, Instant cutoff);

    long countByRequestedIpAndCreatedAtAfter(String requestedIp, Instant cutoff);
}