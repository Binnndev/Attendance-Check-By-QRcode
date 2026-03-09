ALTER TABLE attendance_sessions
    ADD COLUMN checkin_open_at  DATETIME(3) NULL AFTER start_at,
    ADD COLUMN checkin_close_at DATETIME(3) NULL AFTER checkin_open_at;

UPDATE attendance_sessions
SET
    checkin_open_at  = COALESCE(checkin_open_at, start_at),
    checkin_close_at = COALESCE(
            checkin_close_at,
            CASE
                WHEN start_at IS NULL THEN NULL
                WHEN time_window_minutes IS NULL THEN NULL
                ELSE DATE_ADD(start_at, INTERVAL time_window_minutes MINUTE)
                END
                       );

ALTER TABLE attendance_sessions
    ADD CONSTRAINT chk_as_checkin_window
        CHECK (
            checkin_open_at IS NULL
                OR checkin_close_at IS NULL
                OR checkin_close_at >= checkin_open_at
            );
