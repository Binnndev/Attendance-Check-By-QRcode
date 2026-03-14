SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE login_attempts;
TRUNCATE TABLE email_outbox;
TRUNCATE TABLE password_reset_attempts;
TRUNCATE TABLE password_reset_tokens;
TRUNCATE TABLE user_sessions;
TRUNCATE TABLE qr_tokens;

TRUNCATE TABLE attendance_events;
TRUNCATE TABLE session_attendance;
TRUNCATE TABLE absence_requests;
TRUNCATE TABLE attendance_sessions;
TRUNCATE TABLE group_members;
TRUNCATE TABLE class_groups;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;