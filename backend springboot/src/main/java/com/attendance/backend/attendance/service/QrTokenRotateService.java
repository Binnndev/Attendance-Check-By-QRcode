package com.attendance.backend.attendance.service;

import com.attendance.backend.attendance.repository.AttendanceSessionRepository;
import com.attendance.backend.attendance.repository.QrTokenRepository;
import com.attendance.backend.common.exception.ApiException;
import com.attendance.backend.domain.entity.AttendanceSession;
import com.attendance.backend.domain.entity.QrToken;
import com.attendance.backend.domain.enums.MemberRole;
import com.attendance.backend.domain.enums.MemberStatus;
import com.attendance.backend.domain.enums.SessionStatus;
import jakarta.persistence.EntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class QrTokenRotateService {

    private static final SecureRandom RNG = new SecureRandom();

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final QrTokenRepository qrTokenRepository;
    private final EntityManager entityManager;
    private final Clock clock = Clock.systemUTC();

    public QrTokenRotateService(
            AttendanceSessionRepository attendanceSessionRepository,
            QrTokenRepository qrTokenRepository,
            EntityManager entityManager
    ) {
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.qrTokenRepository = qrTokenRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public RotateResult rotate(UUID sessionId, UUID actorUserId, String note) {
        final Instant now = Instant.now(clock);

        AttendanceSession session = attendanceSessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> ApiException.notFound("SESSION_NOT_FOUND", "Session not found"));

        if (session.getStatus() != SessionStatus.OPEN) {
            throw ApiException.conflict("SESSION_NOT_OPEN", "Session is not OPEN");
        }

        ensureHostOrCoHost(session.getGroupId(), actorUserId);

        String rotatedFrom = qrTokenRepository.findLatestActiveForUpdate(sessionId, now)
                .map(QrToken::getTokenId)
                .orElse(null);

        qrTokenRepository.revokeActiveTokens(sessionId, now, "ROTATED");

        final int maxAttempts = 5;
        for (int i = 0; i < maxAttempts; i++) {
            String tokenId = randomBase64Url(32);
            String secret = randomBase64Url(32);
            byte[] tokenHash = sha256(secret);

            Instant expiresAt = now.plusSeconds(session.getQrRotateSeconds());
            if (!expiresAt.isAfter(now)) {
                throw ApiException.unprocessable("TOKEN_EXP_INVALID", "expiresAt must be after issuedAt");
            }

            if (note != null && note.length() > 255) {
                throw ApiException.badRequest("NOTE_TOO_LONG", "note max length is 255");
            }

            QrToken t = new QrToken();
            t.setTokenId(tokenId);
            t.setSessionId(sessionId);
            t.setTokenHash(tokenHash);
            t.setIssuedAt(now);
            t.setExpiresAt(expiresAt);
            t.setRevokedAt(null);
            t.setRevokedReason(null);
            t.setRotatedFromTokenId(rotatedFrom);
            t.setIssuedByUserId(actorUserId);
            t.setNote(note);

            try {
                qrTokenRepository.saveAndFlush(t);
                return new RotateResult(sessionId, tokenId + "." + secret, now, expiresAt, rotatedFrom);
            } catch (DataIntegrityViolationException ex) {
                String msg = rootMessage(ex);
                if (msg.contains("uk_qt_token_hash") || msg.contains("Duplicate entry") || msg.contains("PRIMARY")) {
                    continue;
                }
                throw ex;
            }
        }

        throw ApiException.unprocessable("TOKEN_GENERATION_FAILED", "Cannot generate unique token");
    }

    private void ensureHostOrCoHost(UUID groupId, UUID actorUserId) {
        Long count = entityManager.createQuery("""
                select count(gm)
                from GroupMember gm
                where gm.id.groupId = :groupId
                  and gm.id.userId = :userId
                  and gm.memberStatus = :status
                  and gm.role in (:r1, :r2)
                """, Long.class)
                .setParameter("groupId", groupId)
                .setParameter("userId", actorUserId)
                .setParameter("status", MemberStatus.APPROVED)
                .setParameter("r1", MemberRole.OWNER)
                .setParameter("r2", MemberRole.CO_HOST)
                .getSingleResult();

        if (count == null || count == 0L) {
            throw ApiException.forbidden("FORBIDDEN", "Only OWNER/CO_HOST can rotate QR");
        }
    }

    private static String randomBase64Url(int bytes) {
        byte[] b = new byte[bytes];
        RNG.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private static byte[] sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String rootMessage(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() == null ? "" : t.getMessage();
    }

    public record RotateResult(
            UUID sessionId,
            String token,
            Instant issuedAt,
            Instant expiresAt,
            String rotatedFromTokenId
    ) {}
}
