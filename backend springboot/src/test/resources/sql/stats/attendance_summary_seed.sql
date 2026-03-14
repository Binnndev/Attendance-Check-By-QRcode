-- =========================================================
-- USERS
-- =========================================================
INSERT INTO users (
    id, platform_role, email, password_hash, full_name, status
) VALUES
      (
          UUID_TO_BIN('10000000-0000-0000-0000-000000000001', 1),
          'USER',
          'owner.a@example.com',
          '$2a$10$dummyhashownera',
          'Owner A',
          'ACTIVE'
      ),
      (
          UUID_TO_BIN('10000000-0000-0000-0000-000000000002', 1),
          'USER',
          'cohost.a@example.com',
          '$2a$10$dummyhashcohosta',
          'CoHost A',
          'ACTIVE'
      ),
      (
          UUID_TO_BIN('10000000-0000-0000-0000-000000000003', 1),
          'USER',
          'member.a1@example.com',
          '$2a$10$dummyhashmembera1',
          'Member A1',
          'ACTIVE'
      ),
      (
          UUID_TO_BIN('10000000-0000-0000-0000-000000000004', 1),
          'USER',
          'member.a2@example.com',
          '$2a$10$dummyhashmembera2',
          'Member A2',
          'ACTIVE'
      ),
      (
          UUID_TO_BIN('10000000-0000-0000-0000-000000000005', 1),
          'USER',
          'outsider@example.com',
          '$2a$10$dummyhashoutsider',
          'Outsider',
          'ACTIVE'
      ),
      (
          UUID_TO_BIN('10000000-0000-0000-0000-000000000006', 1),
          'USER',
          'owner.zero@example.com',
          '$2a$10$dummyhashownerzero',
          'Owner Zero',
          'ACTIVE'
      ),
      (
          UUID_TO_BIN('10000000-0000-0000-0000-000000000007', 1),
          'USER',
          'member.zero1@example.com',
          '$2a$10$dummyhashmemberzero1',
          'Member Zero 1',
          'ACTIVE'
      ),
      (
          UUID_TO_BIN('10000000-0000-0000-0000-000000000008', 1),
          'USER',
          'member.zero2@example.com',
          '$2a$10$dummyhashmemberzero2',
          'Member Zero 2',
          'ACTIVE'
      );

-- =========================================================
-- GROUPS
-- =========================================================
INSERT INTO class_groups (
    id, owner_user_id, name, code, join_code, description, semester, room, approval_mode, status
) VALUES
      (
          UUID_TO_BIN('20000000-0000-0000-0000-000000000001', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000001', 1),
          'Group A',
          'GROUPA001',
          'JOINA001',
          'Main group for attendance summary tests',
          '2026-S1',
          'A101',
          'AUTO',
          'ACTIVE'
      ),
      (
          UUID_TO_BIN('20000000-0000-0000-0000-000000000002', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000006', 1),
          'Group Zero',
          'GROUPZ001',
          'JOINZ001',
          'Group with zero CLOSED sessions',
          '2026-S1',
          'B202',
          'AUTO',
          'ACTIVE'
      );

-- =========================================================
-- GROUP MEMBERS
-- =========================================================
INSERT INTO group_members (
    group_id, user_id, role, member_status, joined_at, invited_by
) VALUES
      (
          UUID_TO_BIN('20000000-0000-0000-0000-000000000001', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000001', 1),
          'OWNER', 'APPROVED', '2026-02-01 08:00:00.000', NULL
      ),
      (
          UUID_TO_BIN('20000000-0000-0000-0000-000000000001', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000002', 1),
          'CO_HOST', 'APPROVED', '2026-02-01 08:05:00.000', UUID_TO_BIN('10000000-0000-0000-0000-000000000001', 1)
      ),
      (
          UUID_TO_BIN('20000000-0000-0000-0000-000000000001', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000003', 1),
          'MEMBER', 'APPROVED', '2026-02-01 08:10:00.000', UUID_TO_BIN('10000000-0000-0000-0000-000000000001', 1)
      ),
      (
          UUID_TO_BIN('20000000-0000-0000-0000-000000000001', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000004', 1),
          'MEMBER', 'APPROVED', '2026-02-01 08:15:00.000', UUID_TO_BIN('10000000-0000-0000-0000-000000000001', 1)
      ),

      (
          UUID_TO_BIN('20000000-0000-0000-0000-000000000002', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000006', 1),
          'OWNER', 'APPROVED', '2026-02-01 09:00:00.000', NULL
      ),
      (
          UUID_TO_BIN('20000000-0000-0000-0000-000000000002', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000007', 1),
          'MEMBER', 'APPROVED', '2026-02-01 09:05:00.000', UUID_TO_BIN('10000000-0000-0000-0000-000000000006', 1)
      ),
      (
          UUID_TO_BIN('20000000-0000-0000-0000-000000000002', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000008', 1),
          'MEMBER', 'APPROVED', '2026-02-01 09:10:00.000', UUID_TO_BIN('10000000-0000-0000-0000-000000000006', 1)
      );

-- =========================================================
-- ATTENDANCE SESSIONS - GROUP A
-- CLOSED x4, OPEN x1, CANCELLED x1
-- MEMBER_A1 có đủ PRESENT/LATE/ABSENT/EXCUSED ở 4 CLOSED session
-- =========================================================
INSERT INTO attendance_sessions (
    id,
    group_id,
    created_by_user_id,
    title,
    session_date,
    status,
    start_at,
    checkin_open_at,
    checkin_close_at,
    end_at,
    time_window_minutes,
    late_after_minutes,
    qr_rotate_seconds,
    session_secret,
    allow_manual_override,
    note
) VALUES
      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000001', 1),
          UUID_TO_BIN('20000000-0000-0000-0000-000000000001', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000001', 1),
          'Closed Present',
          '2026-03-01',
          'CLOSED',
          '2026-03-01 07:00:00.000',
          '2026-03-01 07:00:00.000',
          '2026-03-01 07:15:00.000',
          '2026-03-01 08:30:00.000',
          15,
          5,
          15,
          'secret-c1',
          1,
          'closed present session'
      ),
      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000002', 1),
          UUID_TO_BIN('20000000-0000-0000-0000-000000000001', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000001', 1),
          'Closed Late',
          '2026-03-02',
          'CLOSED',
          '2026-03-02 07:00:00.000',
          '2026-03-02 07:00:00.000',
          '2026-03-02 07:15:00.000',
          '2026-03-02 08:30:00.000',
          15,
          5,
          15,
          'secret-c2',
          1,
          'closed late session'
      ),
      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000003', 1),
          UUID_TO_BIN('20000000-0000-0000-0000-000000000001', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000001', 1),
          'Closed Absent',
          '2026-03-03',
          'CLOSED',
          '2026-03-03 07:00:00.000',
          '2026-03-03 07:00:00.000',
          '2026-03-03 07:15:00.000',
          '2026-03-03 08:30:00.000',
          15,
          5,
          15,
          'secret-c3',
          1,
          'closed absent session'
      ),
      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000004', 1),
          UUID_TO_BIN('20000000-0000-0000-0000-000000000001', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000001', 1),
          'Closed Excused',
          '2026-03-04',
          'CLOSED',
          '2026-03-04 07:00:00.000',
          '2026-03-04 07:00:00.000',
          '2026-03-04 07:15:00.000',
          '2026-03-04 08:30:00.000',
          15,
          5,
          15,
          'secret-c4',
          1,
          'closed excused session'
      ),
      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000005', 1),
          UUID_TO_BIN('20000000-0000-0000-0000-000000000001', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000001', 1),
          'Open Present Should Not Count',
          '2026-03-05',
          'OPEN',
          '2026-03-05 07:00:00.000',
          '2026-03-05 07:00:00.000',
          '2026-03-05 07:15:00.000',
          NULL,
          15,
          5,
          15,
          'secret-o1',
          1,
          'open session'
      ),
      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000006', 1),
          UUID_TO_BIN('20000000-0000-0000-0000-000000000001', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000001', 1),
          'Cancelled Absent Should Not Count',
          '2026-03-06',
          'CANCELLED',
          '2026-03-06 07:00:00.000',
          '2026-03-06 07:00:00.000',
          '2026-03-06 07:15:00.000',
          '2026-03-06 07:30:00.000',
          15,
          5,
          15,
          'secret-x1',
          1,
          'cancelled session'
      );

-- =========================================================
-- ATTENDANCE SESSIONS - GROUP ZERO
-- không có CLOSED session
-- =========================================================
INSERT INTO attendance_sessions (
    id,
    group_id,
    created_by_user_id,
    title,
    session_date,
    status,
    start_at,
    checkin_open_at,
    checkin_close_at,
    end_at,
    time_window_minutes,
    late_after_minutes,
    qr_rotate_seconds,
    session_secret,
    allow_manual_override,
    note
) VALUES
      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000007', 1),
          UUID_TO_BIN('20000000-0000-0000-0000-000000000002', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000006', 1),
          'Zero Group Open',
          '2026-03-07',
          'OPEN',
          '2026-03-07 07:00:00.000',
          '2026-03-07 07:00:00.000',
          '2026-03-07 07:15:00.000',
          NULL,
          15,
          5,
          15,
          'secret-z-open',
          1,
          'open only'
      ),
      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000008', 1),
          UUID_TO_BIN('20000000-0000-0000-0000-000000000002', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000006', 1),
          'Zero Group Cancelled',
          '2026-03-08',
          'CANCELLED',
          '2026-03-08 07:00:00.000',
          '2026-03-08 07:00:00.000',
          '2026-03-08 07:15:00.000',
          '2026-03-08 07:20:00.000',
          15,
          5,
          15,
          'secret-z-cancel',
          1,
          'cancelled only'
      );

-- =========================================================
-- ABSENCE REQUEST
-- chỉ để EXCUSED realistic hơn
-- =========================================================
INSERT INTO absence_requests (
    id,
    group_id,
    requester_user_id,
    linked_session_id,
    requested_date,
    reason,
    request_status,
    reviewer_user_id,
    reviewer_note,
    reviewed_at
) VALUES
    (
        UUID_TO_BIN('40000000-0000-0000-0000-000000000001', 1),
        UUID_TO_BIN('20000000-0000-0000-0000-000000000001', 1),
        UUID_TO_BIN('10000000-0000-0000-0000-000000000003', 1),
        UUID_TO_BIN('30000000-0000-0000-0000-000000000004', 1),
        NULL,
        'Medical leave',
        'APPROVED',
        UUID_TO_BIN('10000000-0000-0000-0000-000000000001', 1),
        'approved for test',
        '2026-03-04 06:00:00.000'
    );

-- =========================================================
-- SESSION ATTENDANCE
-- MEMBER_A1:
--   CLOSED: PRESENT + LATE + ABSENT + EXCUSED
--   OPEN: PRESENT (không được tính)
--   CANCELLED: ABSENT (không được tính)
-- MEMBER_A2:
--   không có row nào -> vẫn phải hiện trong group page với summary = 0
-- GROUP_ZERO:
--   có row nhưng chỉ ở OPEN/CANCELLED -> summary vẫn phải = 0
-- =========================================================
INSERT INTO session_attendance (
    session_id,
    user_id,
    attendance_status,
    check_in_at,
    check_in_method,
    qr_token_id,
    device_id,
    ip_address,
    user_agent,
    suspicious_flag,
    suspicious_reason,
    excused_by_request_id
) VALUES
      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000001', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000003', 1),
          'PRESENT',
          '2026-03-01 07:02:00.000',
          'QR',
          NULL,
          'device-member-a1',
          '10.0.0.3',
          'JUnit',
          0,
          NULL,
          NULL
      ),
      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000002', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000003', 1),
          'LATE',
          '2026-03-02 07:08:00.000',
          'QR',
          NULL,
          'device-member-a1',
          '10.0.0.3',
          'JUnit',
          0,
          NULL,
          NULL
      ),
      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000003', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000003', 1),
          'ABSENT',
          NULL,
          'QR',
          NULL,
          NULL,
          NULL,
          NULL,
          0,
          NULL,
          NULL
      ),
      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000004', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000003', 1),
          'EXCUSED',
          NULL,
          'MANUAL',
          NULL,
          NULL,
          NULL,
          NULL,
          0,
          NULL,
          UUID_TO_BIN('40000000-0000-0000-0000-000000000001', 1)
      ),
      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000005', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000003', 1),
          'PRESENT',
          '2026-03-05 07:03:00.000',
          'QR',
          NULL,
          'device-member-a1',
          '10.0.0.3',
          'JUnit',
          0,
          NULL,
          NULL
      ),
      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000006', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000003', 1),
          'ABSENT',
          NULL,
          'QR',
          NULL,
          NULL,
          NULL,
          NULL,
          0,
          NULL,
          NULL
      ),

      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000007', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000007', 1),
          'PRESENT',
          '2026-03-07 07:01:00.000',
          'QR',
          NULL,
          'device-zero-1',
          '10.0.0.7',
          'JUnit',
          0,
          NULL,
          NULL
      ),
      (
          UUID_TO_BIN('30000000-0000-0000-0000-000000000008', 1),
          UUID_TO_BIN('10000000-0000-0000-0000-000000000008', 1),
          'ABSENT',
          NULL,
          'QR',
          NULL,
          NULL,
          NULL,
          NULL,
          0,
          NULL,
          NULL
      );