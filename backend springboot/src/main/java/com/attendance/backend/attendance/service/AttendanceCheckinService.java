package com.attendance.backend.attendance.service;

import com.attendance.backend.attendance.repository.AttendanceSessionRepository;
import com.attendance.backend.attendance.repository.SessionAttendanceRepository;
import com.attendance.backend.common.exception.ApiException;
import com.attendance.backend.domain.entity.AttendanceEvent;
import com.attendance.backend.domain.entity.AttendanceSession;
import com.attendance.backend.domain.entity.GroupMember;
import com.attendance.backend.domain.entity.QrToken;
import com.attendance.backend.domain.entity.SessionAttendance;
import com.attendance.backend.domain.enums.AttendanceStatus;
import com.attendance.backend.domain.enums.CheckInMethod;
import com.attendance.backend.domain.enums.EventType;
import com.attendance.backend.domain.enums.MemberStatus;
import com.attendance.backend.domain.enums.SessionStatus;
import com.attendance.backend.domain.id.SessionAttendanceId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class AttendanceCheckinService {

    private static final String FK_SA_SESSION_QR_TOKEN = "fk_sa_session_qr_token"; // V6 exact

    private final AttendanceSessionRepository attendanceSessionRepository;
    private final SessionAttendanceRepository sessionAttendanceRepository;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final Clock clock = Clock.systemUTC();

    public AttendanceCheckinService(AttendanceSessionRepository attendanceSessionRepository,
                                    SessionAttendanceRepository sessionAttendanceRepository,
                                    EntityManager entityManager,
                                    ObjectMapper objectMapper) {
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.sessionAttendanceRepository = sessionAttendanceRepository;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    /**
     * RULE CHỐT:
     * - Reject if now < checkin_open_at
     * - Reject if now > checkin_close_at
     * - lateThreshold = checkin_open_at + late_after_minutes
     *   now <= threshold => PRESENT
     *   threshold < now <= close => LATE
     */
    @Transactional
    public QrCheckinResult qrCheckin(QrCheckinCommand cmd) {
        final Instant now = Instant.now(clock);

        try {
            // 1) Lock session bằng READ (scale tốt hơn WRITE)
            AttendanceSession session = attendanceSessionRepository.findByIdForShare(cmd.sessionId())
                    .orElseThrow(() -> ApiException.notFound("SESSION_NOT_FOUND", "Session not found"));

            if (session.getStatus() != SessionStatus.OPEN) {
                throw ApiException.conflict("SESSION_NOT_OPEN", "Session is not OPEN");
            }

            // 2) Verify user là member APPROVED
            ensureApprovedMember(session.getGroupId(), cmd.userId());

            // 3) Verify QR token (id + hash + expiry + revoke + belongs to session)
            QrToken token = verifyQrToken(cmd.token(), cmd.sessionId(), now);

            // 4) Apply check-in window rule
            Instant openAt = session.getCheckinOpenAt() != null ? session.getCheckinOpenAt() : session.getStartAt();
            Instant closeAt = session.getCheckinCloseAt() != null
                    ? session.getCheckinCloseAt()
                    : session.getStartAt().plus(Duration.ofMinutes(session.getTimeWindowMinutes()));

            // DB có chk_as_checkin_window, giữ check này như guard
            if (closeAt.isBefore(openAt)) {
                throw ApiException.unprocessable("SESSION_TIME_INVALID", "checkin_close_at is before checkin_open_at");
            }

            if (now.isBefore(openAt)) {
                throw ApiException.conflict("CHECKIN_NOT_OPEN_YET", "Check-in not open yet");
            }
            if (now.isAfter(closeAt)) {
                throw ApiException.conflict("CHECKIN_CLOSED", "Check-in window already closed");
            }

            Instant lateThreshold = openAt.plus(Duration.ofMinutes(session.getLateAfterMinutes()));
            AttendanceStatus computed = now.isAfter(lateThreshold) ? AttendanceStatus.LATE : AttendanceStatus.PRESENT;

            // 5) Upsert attendance “xịn” (race-safe)
            SessionAttendance attendance = getOrCreateAttendanceLocked(cmd.sessionId(), cmd.userId(), now);

            // Nếu đã checkin rồi thì reject
            if (attendance.checkInAt != null) {
                throw ApiException.conflict("ALREADY_CHECKED_IN", "User already checked in");
            }

            // Bonus: nếu EXCUSED thì không cho check-in (nếu bạn muốn logic này)
            if (attendance.attendanceStatus == AttendanceStatus.EXCUSED) {
                throw ApiException.conflict("EXCUSED_CANNOT_CHECKIN", "Excused user cannot check in");
            }

            AttendanceStatus oldStatus = attendance.attendanceStatus;

            // 6) Update attendance
            attendance.attendanceStatus = computed;
            attendance.checkInAt = now;
            attendance.checkInMethod = CheckInMethod.QR;
            attendance.qrTokenId = token.getTokenId();
            attendance.deviceId = cmd.deviceId();
            attendance.ipAddress = cmd.ipAddress();
            attendance.userAgent = cmd.userAgent();
            attendance.geoLat = cmd.geoLat();
            attendance.geoLng = cmd.geoLng();
            attendance.distanceMeter = cmd.distanceMeter();
            attendance.suspiciousFlag = false;
            attendance.suspiciousReason = null;
            attendance.updatedAt = now;

            if (attendance.id == null) {
                attendance.id = new SessionAttendanceId(cmd.sessionId(), cmd.userId());
            }

            sessionAttendanceRepository.saveAndFlush(attendance);

            // 7) Ghi event audit
            AttendanceEvent event = new AttendanceEvent();
            event.id = UUID.randomUUID();
            event.sessionId = cmd.sessionId();
            event.userId = cmd.userId();
            event.actorUserId = cmd.userId();
            event.eventType = EventType.CHECKIN_QR;
            event.oldStatus = oldStatus;
            event.newStatus = computed;
            event.qrTokenId = token.getTokenId();

            ObjectNode payload = objectMapper.createObjectNode();
            putIfNotNull(payload, "deviceId", cmd.deviceId());
            putIfNotNull(payload, "ipAddress", cmd.ipAddress());
            putIfNotNull(payload, "userAgent", cmd.userAgent());
            if (cmd.geoLat() != null) payload.put("geoLat", cmd.geoLat());
            if (cmd.geoLng() != null) payload.put("geoLng", cmd.geoLng());
            if (cmd.distanceMeter() != null) payload.put("distanceMeter", cmd.distanceMeter());
            payload.put("computedStatus", computed.name());
            payload.put("openAt", openAt.toString());
            payload.put("closeAt", closeAt.toString());
            payload.put("lateThreshold", lateThreshold.toString());
            event.eventPayload = payload;

            event.createdAt = now;
            entityManager.persist(event);

            // flush để bắt trigger/constraint sớm (optional nhưng “xịn”)
            entityManager.flush();

            return new QrCheckinResult(
                    cmd.sessionId(),
                    cmd.userId(),
                    computed,
                    now,
                    token.getTokenId()
            );

        } catch (DataIntegrityViolationException ex) {
            String root = rootMessage(ex);

            // Fix #4: phân loại FK v6
            if (root.contains(FK_SA_SESSION_QR_TOKEN)) {
                throw ApiException.conflict("QR_TOKEN_NOT_FOR_SESSION", "QR token does not belong to this session");
            }

            // Duplicate PK/unique thường là do concurrency
            if (root.contains("Duplicate entry") && (root.contains("PRIMARY") || root.contains("session_attendance"))) {
                throw ApiException.conflict("ALREADY_CHECKED_IN", "User already checked in");
            }

            throw ex;
        }
    }

    /**
     * Fix #3: Upsert attendance theo hướng:
     * - select FOR UPDATE
     * - nếu chưa có: thử insert
     * - nếu insert fail do race: select FOR UPDATE lại và dùng row đã tồn tại
     */
    private SessionAttendance getOrCreateAttendanceLocked(UUID sessionId, UUID userId, Instant now) {
        var locked = sessionAttendanceRepository.findBySessionAndUserForUpdate(sessionId, userId);
        if (locked.isPresent()) return locked.get();

        try {
            SessionAttendance sa = SessionAttendance.createNew(sessionId, userId);
            sa.id = new SessionAttendanceId(sessionId, userId);
            sa.createdAt = now;
            sa.updatedAt = now;
            sessionAttendanceRepository.saveAndFlush(sa);
        } catch (DataIntegrityViolationException ignore) {
            // someone else inserted concurrently
        }

        return sessionAttendanceRepository.findBySessionAndUserForUpdate(sessionId, userId)
                .orElseThrow(() -> ApiException.conflict("ATTENDANCE_ROW_MISSING", "Cannot load attendance row"));
    }

    private void ensureApprovedMember(UUID groupId, UUID userId) {
        Long count = entityManager.createQuery("""
                        select count(gm)
                        from GroupMember gm
                        where gm.id.groupId = :groupId
                          and gm.id.userId = :userId
                          and gm.memberStatus = :status
                        """, Long.class)
                .setParameter("groupId", groupId)
                .setParameter("userId", userId)
                .setParameter("status", MemberStatus.APPROVED)
                .getSingleResult();

        if (count == null || count == 0L) {
            throw ApiException.forbidden("NOT_A_GROUP_MEMBER", "User is not an APPROVED member of this group");
        }
    }

    private QrToken verifyQrToken(String plainToken, UUID expectedSessionId, Instant now) {
        ParsedToken parsed = parseToken(plainToken);

        // token_id là PK của qr_tokens
        QrToken token = entityManager.find(QrToken.class, parsed.tokenId(), LockModeType.PESSIMISTIC_READ);
        if (token == null) {
            throw ApiException.badRequest("QR_TOKEN_INVALID", "QR token is invalid");
        }

        if (!Objects.equals(token.getSessionId(), expectedSessionId)) {
            throw ApiException.conflict("QR_TOKEN_NOT_FOR_SESSION", "QR token does not belong to this session");
        }

        if (token.getRevokedAt() != null) {
            throw ApiException.conflict("QR_TOKEN_REVOKED", "QR token was revoked");
        }

        if (token.getExpiresAt() != null && now.isAfter(token.getExpiresAt())) {
            throw ApiException.conflict("QR_TOKEN_EXPIRED", "QR token expired");
        }

        // Fix #2: canonical scheme -> hash(secret)
        byte[] stored = token.getTokenHash();
        byte[] hashSecret = sha256(parsed.secret());

        // (compat) nếu trước đây bạn đã lưu hash(full token) thì giữ thêm 1 nhánh để không gãy
        byte[] hashFull = sha256(plainToken);

        boolean ok = MessageDigest.isEqual(stored, hashSecret) || MessageDigest.isEqual(stored, hashFull);
        if (!ok) {
            throw ApiException.badRequest("QR_TOKEN_INVALID", "QR token hash mismatch");
        }

        return token;
    }

    private ParsedToken parseToken(String token) {
        if (token == null || token.isBlank()) {
            throw ApiException.badRequest("QR_TOKEN_REQUIRED", "token is required");
        }

        String[] parts = token.split("\\.", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw ApiException.badRequest("QR_TOKEN_INVALID_FORMAT", "Expected format: <tokenId>.<secret>");
        }

        if (parts[0].length() > 64) {
            throw ApiException.badRequest("QR_TOKEN_INVALID_FORMAT", "tokenId length > 64");
        }

        return new ParsedToken(parts[0], parts[1]);
    }

    private byte[] sha256(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(raw.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw ApiException.badRequest("HASH_ERROR", "Cannot compute token hash");
        }
    }

    private void putIfNotNull(ObjectNode node, String key, String value) {
        if (value != null && !value.isBlank()) node.put(key, value);
    }

    private String rootMessage(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() == null ? "" : t.getMessage();
    }

    public record QrCheckinCommand(
            UUID sessionId,
            UUID userId,
            String token,
            String deviceId,
            String ipAddress,
            String userAgent,
            BigDecimal geoLat,
            BigDecimal geoLng,
            Integer distanceMeter
    ) {}

    public record QrCheckinResult(
            UUID sessionId,
            UUID userId,
            AttendanceStatus attendanceStatus,
            Instant checkInAt,
            String qrTokenId
    ) {}

    private record ParsedToken(String tokenId, String secret) {}
}
