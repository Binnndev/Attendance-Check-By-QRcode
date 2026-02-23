SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET @@session.collation_connection = 'utf8mb4_unicode_ci';
SET @now = UTC_TIMESTAMP(3);
SET @today = CURDATE();

INSERT INTO users (id, platform_role, email, password_hash, full_name, avatar_url, user_code, primary_device_id, status)
    VALUES
        (UUID_TO_BIN(UUID(), 1), 'ADMIN', 'admin@demo.local',
         '$2b$10$yLY4/euWLaBjf.JVDnmZuO.pqsIo5NGw.ag2r44jaE.jpXUVGyix.',
         'Admin Demo', NULL, 'ADM001', 'DEV_ADMIN_01', 'ACTIVE'),

        (UUID_TO_BIN(UUID(), 1), 'USER', 'owner@demo.local',
         '$2b$10$sURET26P0PTwodDqLiEXCucNIVSXAhLRXw1n//lyKB3jx6eZdt0RC',
         'Owner Demo', NULL, 'OWN001', 'DEV_OWNER_01', 'ACTIVE'),

        (UUID_TO_BIN(UUID(), 1), 'USER', 'cohost@demo.local',
         '$2b$10$nEMwXMb3Y2YXFmpK4VX2de6sb6.R7rlYJDJxWkSZOY7Ph4nJtNnNW',
         'Co-host Demo', NULL, 'COH001', 'DEV_COHOST_01', 'ACTIVE'),

        (UUID_TO_BIN(UUID(), 1), 'USER', 'member1@demo.local',
         '$2b$10$nEMwXMb3Y2YXFmpK4VX2de6sb6.R7rlYJDJxWkSZOY7Ph4nJtNnNW',
         'Member One', NULL, 'MBR001', 'DEV_MEMBER1_01', 'ACTIVE'),

        (UUID_TO_BIN(UUID(), 1), 'USER', 'member2@demo.local',
         '$2b$10$nEMwXMb3Y2YXFmpK4VX2de6sb6.R7rlYJDJxWkSZOY7Ph4nJtNnNW',
         'Member Two', NULL, 'MBR002', 'DEV_MEMBER2_01', 'ACTIVE')
        AS new
ON DUPLICATE KEY UPDATE
                     platform_role     = new.platform_role,
                     password_hash     = new.password_hash,
                     full_name         = new.full_name,
                     avatar_url        = new.avatar_url,
                     user_code         = new.user_code,
                     primary_device_id = new.primary_device_id,
                     status            = new.status;

SET @admin_id  = (SELECT id FROM users WHERE email_norm = (_utf8mb4'admin@demo.local'  COLLATE utf8mb4_unicode_ci) LIMIT 1);
SET @owner_id  = (SELECT id FROM users WHERE email_norm = (_utf8mb4'owner@demo.local'  COLLATE utf8mb4_unicode_ci) LIMIT 1);
SET @cohost_id = (SELECT id FROM users WHERE email_norm = (_utf8mb4'cohost@demo.local' COLLATE utf8mb4_unicode_ci) LIMIT 1);
SET @m1_id     = (SELECT id FROM users WHERE email_norm = (_utf8mb4'member1@demo.local' COLLATE utf8mb4_unicode_ci) LIMIT 1);
SET @m2_id     = (SELECT id FROM users WHERE email_norm = (_utf8mb4'member2@demo.local' COLLATE utf8mb4_unicode_ci) LIMIT 1);

INSERT INTO class_groups
(id, owner_user_id, name, code, join_code, description, semester, room, approval_mode, allow_auto_join_on_checkin, status)
    VALUES
        (UUID_TO_BIN(UUID(),1), @owner_id, 'Demo Group A', 'CS101-2026', 'JOIN2026A',
         'Demo group A for QR attendance', '2026A', 'Room A1', 'MANUAL', 0, 'ACTIVE'),
        (UUID_TO_BIN(UUID(),1), @owner_id, 'Demo Group B', 'ENG102-2026', 'JOIN2026B',
         'Demo group B for QR attendance', '2026A', 'Room B2', 'AUTO', 0, 'ACTIVE')
        AS new
ON DUPLICATE KEY UPDATE
                     owner_user_id = new.owner_user_id,
                     name          = new.name,
                     join_code     = new.join_code,
                     description   = new.description,
                     semester      = new.semester,
                     room          = new.room,
                     approval_mode = new.approval_mode,
                     status        = new.status;

SET @g1_id = (SELECT id FROM class_groups WHERE code = (_utf8mb4'CS101-2026' COLLATE utf8mb4_unicode_ci) LIMIT 1);
SET @g2_id = (SELECT id FROM class_groups WHERE code = (_utf8mb4'ENG102-2026' COLLATE utf8mb4_unicode_ci) LIMIT 1);

INSERT INTO group_members (group_id, user_id, role, member_status, joined_at, invited_by)
    VALUES
        (@g1_id, @owner_id,  'OWNER',   'APPROVED', @now, NULL),
        (@g1_id, @cohost_id, 'CO_HOST', 'APPROVED', @now, @owner_id),
        (@g1_id, @m1_id,     'MEMBER',  'APPROVED', @now, @owner_id),
        (@g1_id, @m2_id,     'MEMBER',  'APPROVED', @now, @owner_id),

        (@g2_id, @owner_id,  'OWNER',   'APPROVED', @now, NULL),
        (@g2_id, @cohost_id, 'CO_HOST', 'APPROVED', @now, @owner_id),
        (@g2_id, @m1_id,     'MEMBER',  'APPROVED', @now, @owner_id),
        (@g2_id, @m2_id,     'MEMBER',  'APPROVED', @now, @owner_id)
        AS new
ON DUPLICATE KEY UPDATE
                     role          = new.role,
                     member_status = new.member_status,
                     joined_at     = COALESCE(group_members.joined_at, new.joined_at);

SET @g1_open_id = (SELECT id FROM attendance_sessions WHERE group_id=@g1_id AND status='OPEN' LIMIT 1);
SET @g1_open_title = 'G1 - OPEN (Live)' COLLATE utf8mb4_unicode_ci;
SET @g1_open_start = DATE_SUB(@now, INTERVAL 5 MINUTE) COLLATE utf8mb4_unicode_ci;
SET @g1_open_window = 15;
SET @g1_open_late = 5;
SET @g1_open_close = DATE_ADD(@g1_open_start, INTERVAL @g1_open_window MINUTE);

INSERT INTO attendance_sessions
(id, group_id, created_by_user_id, title, session_date, status,
 start_at, checkin_open_at, checkin_close_at, end_at,
 time_window_minutes, late_after_minutes, qr_rotate_seconds,
 session_secret, allow_manual_override, note)
SELECT
    UUID_TO_BIN(UUID(),1), @g1_id, @owner_id, @g1_open_title, @today, 'OPEN',
    @g1_open_start, @g1_open_start, @g1_open_close, DATE_ADD(@g1_open_start, INTERVAL 30 MINUTE),
    @g1_open_window, @g1_open_late, 15,
    'g1-open-secret', 1, 'Seed L3 open session'
FROM DUAL
WHERE @g1_open_id IS NULL;

SET @g1_open_id = (SELECT id FROM attendance_sessions WHERE group_id=@g1_id AND status='OPEN' LIMIT 1);

SET @g1_closed_title = 'G1 - CLOSED (History)';
SET @g1_closed_date = DATE_SUB(@today, INTERVAL 1 DAY);
SET @g1_closed_start = DATE_SUB(@g1_open_start, INTERVAL 1 DAY);
SET @g1_closed_close = DATE_ADD(@g1_closed_start, INTERVAL 15 MINUTE);

INSERT INTO attendance_sessions
(id, group_id, created_by_user_id, title, session_date, status,
 start_at, checkin_open_at, checkin_close_at, end_at,
 time_window_minutes, late_after_minutes, qr_rotate_seconds,
 session_secret, allow_manual_override, note)
SELECT
    UUID_TO_BIN(UUID(),1), @g1_id, @owner_id, @g1_closed_title, @g1_closed_date, 'CLOSED',
    @g1_closed_start, @g1_closed_start, @g1_closed_close, DATE_ADD(@g1_closed_start, INTERVAL 30 MINUTE),
    15, 5, 15,
    'g1-closed-secret', 1, 'Seed L3 closed session'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM attendance_sessions
    WHERE group_id=@g1_id AND session_date=@g1_closed_date AND title=@g1_closed_title
);

SET @g1_closed_id = (
    SELECT id FROM attendance_sessions
    WHERE group_id=@g1_id AND session_date=@g1_closed_date AND title=@g1_closed_title
    LIMIT 1
);

SET @g2_open_id = (SELECT id FROM attendance_sessions WHERE group_id=@g2_id AND status='OPEN' LIMIT 1);
SET @g2_open_title = 'G2 - OPEN (Live)' COLLATE utf8mb4_unicode_ci;
SET @g2_open_start = DATE_SUB(@now, INTERVAL 3 MINUTE);
SET @g2_open_close = DATE_ADD(@g2_open_start, INTERVAL 20 MINUTE);

INSERT INTO attendance_sessions
(id, group_id, created_by_user_id, title, session_date, status,
 start_at, checkin_open_at, checkin_close_at, end_at,
 time_window_minutes, late_after_minutes, qr_rotate_seconds,
 session_secret, allow_manual_override, note)
SELECT
    UUID_TO_BIN(UUID(),1), @g2_id, @cohost_id, @g2_open_title, @today, 'OPEN',
    @g2_open_start, @g2_open_start, @g2_open_close, DATE_ADD(@g2_open_start, INTERVAL 40 MINUTE),
    20, 7, 20,
    'g2-open-secret', 1, 'Seed L3 open session'
FROM DUAL
WHERE @g2_open_id IS NULL;

SET @g2_open_id = (SELECT id FROM attendance_sessions WHERE group_id=@g2_id AND status='OPEN' LIMIT 1);

INSERT INTO absence_requests
(id, group_id, requester_user_id, linked_session_id, requested_date, reason, evidence_url,
 request_status, reviewer_user_id, reviewer_note, reviewed_at)
SELECT
    UUID_TO_BIN(UUID(),1), @g1_id, @m2_id, @g1_closed_id, NULL,
    'Medical appointment', NULL,
    'APPROVED', @owner_id, 'Approved (seed)', @now
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM absence_requests
    WHERE group_id=@g1_id AND requester_user_id=@m2_id AND linked_session_id=@g1_closed_id
);

SET @ar_approved_id = (
    SELECT id FROM absence_requests
    WHERE group_id=@g1_id AND requester_user_id=@m2_id AND linked_session_id=@g1_closed_id
    LIMIT 1
);

INSERT INTO absence_requests
(id, group_id, requester_user_id, linked_session_id, requested_date, reason, evidence_url, request_status)
SELECT
    UUID_TO_BIN(UUID(),1), @g1_id, @m1_id, NULL, DATE_ADD(@today, INTERVAL 1 DAY),
    'Family event (pending demo)', NULL, 'PENDING'
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM absence_requests
    WHERE group_id=@g1_id AND requester_user_id=@m1_id AND requested_date=DATE_ADD(@today, INTERVAL 1 DAY)
);

INSERT INTO absence_requests
(id, group_id, requester_user_id, linked_session_id, requested_date, reason, evidence_url,
 request_status, reviewer_user_id, reviewer_note, reviewed_at)
SELECT
    UUID_TO_BIN(UUID(),1), @g1_id, @m2_id, NULL, DATE_SUB(@today, INTERVAL 1 DAY),
    'Late submit (rejected demo)', NULL,
    'REJECTED', @cohost_id, 'Rejected (seed)', @now
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM absence_requests
    WHERE group_id=@g1_id AND requester_user_id=@m2_id AND requested_date=DATE_SUB(@today, INTERVAL 1 DAY)
);

INSERT IGNORE INTO qr_tokens
(token_id, session_id, token_hash, issued_at, expires_at, revoked_at, revoked_reason,
 rotated_from_token_id, issued_by_user_id, note)
VALUES
    ('g1_tok_v1', @g1_open_id, UNHEX(SHA2('g1_secret_v1',256)),
     DATE_SUB(@now, INTERVAL 2 MINUTE), DATE_ADD(@now, INTERVAL 10 MINUTE),
     DATE_SUB(@now, INTERVAL 1 MINUTE), 'ROTATED',
     NULL, @owner_id, 'G1 token v1 (revoked)');

INSERT IGNORE INTO qr_tokens
(token_id, session_id, token_hash, issued_at, expires_at, revoked_at, revoked_reason,
 rotated_from_token_id, issued_by_user_id, note)
VALUES
    ('g1_tok_v2', @g1_open_id, UNHEX(SHA2('g1_secret_v2',256)),
     DATE_SUB(@now, INTERVAL 1 MINUTE), DATE_ADD(@now, INTERVAL 10 MINUTE),
     NULL, NULL,
     'g1_tok_v1', @owner_id, 'G1 token v2 (active)');

INSERT IGNORE INTO qr_tokens
(token_id, session_id, token_hash, issued_at, expires_at, revoked_at, revoked_reason,
 rotated_from_token_id, issued_by_user_id, note)
VALUES
    ('g2_tok_v1', @g2_open_id, UNHEX(SHA2('g2_secret_v1',256)),
     @now, DATE_ADD(@now, INTERVAL 10 MINUTE),
     NULL, NULL,
     NULL, @cohost_id, 'G2 token v1 (active)');

INSERT INTO session_attendance
(session_id, user_id, attendance_status, check_in_at, check_in_method, qr_token_id,
 device_id, ip_address, user_agent, geo_lat, geo_lng, distance_meter,
 suspicious_flag, suspicious_reason, excused_by_request_id, created_at, updated_at)
    VALUES
        (@g1_open_id, @m1_id, 'PRESENT', DATE_ADD(@g1_open_start, INTERVAL 2 MINUTE), 'QR', 'g1_tok_v2',
         'DEV_MEMBER1_01', '127.0.0.1', 'seed-agent', NULL, NULL, NULL,
         0, NULL, NULL, @now, @now),

        (@g1_open_id, @m2_id, 'LATE', DATE_ADD(@g1_open_start, INTERVAL 9 MINUTE), 'QR', 'g1_tok_v2',
         'DEV_MEMBER2_01', '127.0.0.1', 'seed-agent', NULL, NULL, NULL,
         0, NULL, NULL, @now, @now),

        (@g1_open_id, @cohost_id, 'ABSENT', NULL, 'QR', NULL,
         NULL, NULL, NULL, NULL, NULL, NULL,
         0, NULL, NULL, @now, @now),

        (@g1_open_id, @owner_id, 'PRESENT', @now, 'MANUAL', NULL,
         NULL, NULL, NULL, NULL, NULL, NULL,
         0, NULL, NULL, @now, @now)
        AS new
ON DUPLICATE KEY UPDATE
                     attendance_status = new.attendance_status,
                     check_in_at       = new.check_in_at,
                     check_in_method   = new.check_in_method,
                     qr_token_id       = new.qr_token_id,
                     device_id         = new.device_id,
                     ip_address        = new.ip_address,
                     user_agent        = new.user_agent,
                     updated_at        = new.updated_at;

INSERT INTO session_attendance
(session_id, user_id, attendance_status, check_in_at, check_in_method, qr_token_id,
 suspicious_flag, suspicious_reason, excused_by_request_id, created_at, updated_at)
    VALUES
        (@g1_closed_id, @m2_id, 'EXCUSED', NULL, 'MANUAL', NULL,
         0, NULL, @ar_approved_id, @now, @now)
        AS new
ON DUPLICATE KEY UPDATE
                     attendance_status     = new.attendance_status,
                     excused_by_request_id = new.excused_by_request_id,
                     updated_at            = new.updated_at;

INSERT INTO session_attendance
(session_id, user_id, attendance_status, check_in_at, check_in_method, qr_token_id,
 suspicious_flag, created_at, updated_at)
    VALUES
        (@g2_open_id, @m1_id, 'ABSENT', NULL, 'QR', NULL, 0, @now, @now),
        (@g2_open_id, @m2_id, 'PRESENT', DATE_ADD(@g2_open_start, INTERVAL 1 MINUTE), 'QR', 'g2_tok_v1', 0, @now, @now)
        AS new
ON DUPLICATE KEY UPDATE
                     attendance_status = new.attendance_status,
                     check_in_at       = new.check_in_at,
                     check_in_method   = new.check_in_method,
                     qr_token_id       = new.qr_token_id,
                     updated_at        = new.updated_at;

INSERT INTO attendance_events
(id, session_id, user_id, actor_user_id, event_type, old_status, new_status, qr_token_id, event_payload, created_at)
SELECT UUID_TO_BIN(UUID(),1), @g1_open_id, @owner_id, @owner_id, 'SESSION_OPENED', NULL, NULL, NULL,
       JSON_OBJECT('seed','level3','note','G1 opened'), DATE_SUB(@now, INTERVAL 5 MINUTE)
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM attendance_events
    WHERE session_id=@g1_open_id AND event_type = (_utf8mb4'CHECKIN_QR' COLLATE utf8mb4_unicode_ci)
);

INSERT INTO attendance_events
(id, session_id, user_id, actor_user_id, event_type, old_status, new_status, qr_token_id, event_payload, created_at)
SELECT UUID_TO_BIN(UUID(),1), @g2_open_id, @cohost_id, @cohost_id, 'SESSION_OPENED', NULL, NULL, NULL,
       JSON_OBJECT('seed','level3','note','G2 opened'), DATE_SUB(@now, INTERVAL 3 MINUTE)
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM attendance_events
    WHERE session_id=@g2_open_id AND event_type = (_utf8mb4'SESSION_OPENED' COLLATE utf8mb4_unicode_ci)
);

INSERT INTO attendance_events
(id, session_id, user_id, actor_user_id, event_type, old_status, new_status, qr_token_id, event_payload, created_at)
SELECT UUID_TO_BIN(UUID(),1), @g1_open_id, @m1_id, @m1_id, 'CHECKIN_QR', 'ABSENT', 'PRESENT', 'g1_tok_v2',
       JSON_OBJECT('deviceId','DEV_MEMBER1_01'), @now
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM attendance_events
    WHERE session_id=@g1_open_id AND user_id=@m1_id AND event_type='CHECKIN_QR'
);

INSERT INTO attendance_events
(id, session_id, user_id, actor_user_id, event_type, old_status, new_status, qr_token_id, event_payload, created_at)
SELECT UUID_TO_BIN(UUID(),1), @g1_open_id, @m2_id, @m2_id, 'CHECKIN_QR', 'ABSENT', 'LATE', 'g1_tok_v2',
       JSON_OBJECT('deviceId','DEV_MEMBER2_01'), @now
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM attendance_events
    WHERE session_id=@g1_open_id AND user_id=@m2_id AND event_type='CHECKIN_QR'
);

INSERT INTO attendance_events
(id, session_id, user_id, actor_user_id, event_type, old_status, new_status, qr_token_id, event_payload, created_at)
SELECT UUID_TO_BIN(UUID(),1), @g2_open_id, @m2_id, @m2_id, 'CHECKIN_QR', 'ABSENT', 'PRESENT', 'g2_tok_v1',
       JSON_OBJECT('deviceId','DEV_MEMBER2_01'), @now
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM attendance_events
    WHERE session_id=@g2_open_id AND user_id=@m2_id AND event_type='CHECKIN_QR'
);

INSERT INTO attendance_events
(id, session_id, user_id, actor_user_id, event_type, old_status, new_status, qr_token_id, event_payload, created_at)
SELECT UUID_TO_BIN(UUID(),1), @g1_open_id, @cohost_id, @owner_id, 'MARK_MANUAL_PRESENT', 'ABSENT', 'PRESENT', NULL,
       JSON_OBJECT('note','manual override by owner'), @now
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM attendance_events
    WHERE session_id=@g1_open_id AND user_id=@cohost_id AND event_type='MARK_MANUAL_PRESENT'
);

INSERT INTO attendance_events
(id, session_id, user_id, actor_user_id, event_type, old_status, new_status, qr_token_id, event_payload, created_at)
SELECT UUID_TO_BIN(UUID(),1), @g1_closed_id, @m2_id, @owner_id, 'MARK_EXCUSED', 'ABSENT', 'EXCUSED', NULL,
       JSON_OBJECT('absenceRequestId', BIN_TO_UUID(@ar_approved_id, 1)), @now
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM attendance_events
    WHERE session_id=@g1_closed_id AND user_id=@m2_id AND event_type='MARK_EXCUSED'
);

INSERT INTO attendance_events
(id, session_id, user_id, actor_user_id, event_type, old_status, new_status, qr_token_id, event_payload, created_at)
SELECT UUID_TO_BIN(UUID(),1), @g1_closed_id, @owner_id, @owner_id, 'SESSION_CLOSED', NULL, NULL, NULL,
       JSON_OBJECT('seed','level3','note','G1 closed'), DATE_SUB(@now, INTERVAL 1 DAY)
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM attendance_events
    WHERE session_id=@g1_closed_id AND event_type='SESSION_CLOSED'
);