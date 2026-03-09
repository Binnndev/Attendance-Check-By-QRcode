
SET @idx := (
    SELECT 1
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'qr_tokens'
      AND INDEX_NAME = 'uk_qt_session_token'
    LIMIT 1
);

SET @sql := IF(
        @idx IS NULL,
        'ALTER TABLE qr_tokens ADD UNIQUE KEY uk_qt_session_token (session_id, token_id)',
        'SELECT 1'
            );

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk := (
    SELECT kcu.CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE kcu
    WHERE kcu.TABLE_SCHEMA = DATABASE()
      AND kcu.TABLE_NAME = 'session_attendance'
      AND kcu.COLUMN_NAME = 'qr_token_id'
      AND kcu.REFERENCED_TABLE_NAME = 'qr_tokens'
    LIMIT 1
);

SET @sql := IF(
        @fk IS NULL,
        'SELECT 1',
        CONCAT('ALTER TABLE session_attendance DROP FOREIGN KEY `', @fk, '`')
            );

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx := (
    SELECT 1
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'session_attendance'
      AND INDEX_NAME = 'idx_sa_session_qr_token'
    LIMIT 1
);

SET @sql := IF(
        @idx IS NULL,
        'ALTER TABLE session_attendance ADD INDEX idx_sa_session_qr_token (session_id, qr_token_id)',
        'SELECT 1'
            );

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @fk2 := (
    SELECT 1
    FROM information_schema.TABLE_CONSTRAINTS tc
    WHERE tc.TABLE_SCHEMA = DATABASE()
      AND tc.TABLE_NAME = 'session_attendance'
      AND tc.CONSTRAINT_TYPE = 'FOREIGN KEY'
      AND tc.CONSTRAINT_NAME = 'fk_sa_session_qr_token'
    LIMIT 1
);

SET @sql := IF(
        @fk2 IS NULL,
        'ALTER TABLE session_attendance
           ADD CONSTRAINT fk_sa_session_qr_token
           FOREIGN KEY (session_id, qr_token_id)
           REFERENCES qr_tokens (session_id, token_id)
           ON UPDATE CASCADE
           ON DELETE RESTRICT',
        'SELECT 1'
            );

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
