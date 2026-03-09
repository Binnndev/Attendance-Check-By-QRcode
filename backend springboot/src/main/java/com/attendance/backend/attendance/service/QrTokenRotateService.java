package com.attendance.backend.attendance.service;

import com.attendance.backend.attendance.repository.AttendanceSessionRepository;
import com.attendance.backend.attendance.repository.QrTokenRepository;
import com.attendance.backend.common.exception.ApiException;
import com.attendance.backend.domain.entity.AttendanceSession;
import com.attendance.backend.domain.entity.QrToken;
import com.attendance.backend.domain.enums.SessionStatus;
import jakarta.persistence.EntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
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

    private static String newTokenId() {
        return "qt_" + UUID.randomUUID().toString().replace("-", ""); // ~35 chars, <= 64
    }

    private static String newSecret() {
        byte[] b = new byte[18];
        RNG.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private static byte[] sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(raw.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
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

        int rotateSeconds = 15;
        Integer cfg = session.getQrRotateSeconds();
        if (cfg != null && cfg > 0) rotateSeconds = cfg;

        Instant expiresAt = now.plusSeconds(rotateSeconds);
        if (!expiresAt.isAfter(now)) {
            throw ApiException.unprocessable("TOKEN_EXP_INVALID", "expiresAt must be after issuedAt");
        }

        if (note != null && note.length() > 255) {
            throw ApiException.badRequest("NOTE_TOO_LONG", "note max length is 255");
        }

        String rotatedFrom = qrTokenRepository.findLatestActiveForUpdate(sessionId, now)
                .map(QrToken::getTokenId)
                .orElse(null);

        qrTokenRepository.revokeActiveTokens(sessionId, now, "ROTATED");

        final int maxAttempts = 5;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String tokenId = newTokenId();
            String secret = newSecret();
            byte[] tokenHash = sha256(secret);

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
                if (attempt == maxAttempts) throw ex;

                String msg = rootMessage(ex);
                if (msg.contains("uk_qt_token_hash")
                        || msg.contains("Duplicate entry")
                        || msg.contains("PRIMARY")) {
                    continue;
                }
                throw ex;
            }
        }

        throw ApiException.unprocessable("TOKEN_GENERATION_FAILED", "Cannot generate unique token");
    }

    private void ensureHostOrCoHost(UUID groupId, UUID actorUserId) {
        Number cnt = (Number) entityManager.createNativeQuery("""
        select count(*)
        from group_members gm
        where gm.group_id = UUID_TO_BIN(:groupId, 1)
          and gm.user_id  = UUID_TO_BIN(:userId, 1)
          and gm.member_status = 'APPROVED'
          and gm.role in ('OWNER','CO_HOST')
    """)
                .setParameter("groupId", groupId.toString())
                .setParameter("userId", actorUserId.toString())
                .getSingleResult();

        if (cnt == null || cnt.longValue() == 0L) {
            throw ApiException.forbidden("FORBIDDEN", "Only OWNER/CO_HOST can rotate QR");
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