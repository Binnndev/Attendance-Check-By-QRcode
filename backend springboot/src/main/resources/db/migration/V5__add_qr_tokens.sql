CREATE TABLE qr_tokens (
                           token_id            VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
                           session_id          BINARY(16) NOT NULL,

                           token_hash          VARBINARY(32) NOT NULL, -- SHA-256 = 32 bytes

                           issued_at           DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                           expires_at          DATETIME(3) NOT NULL,
                           revoked_at          DATETIME(3) NULL,
                           revoked_reason      VARCHAR(255) NULL,

                           rotated_from_token_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
                           issued_by_user_id   BINARY(16) NULL,

                           note                VARCHAR(255) NULL,

                           PRIMARY KEY (token_id),
                           UNIQUE KEY uk_qt_token_hash (token_hash),
                           KEY idx_qt_session_issued (session_id, issued_at),
                           KEY idx_qt_session_active (session_id, revoked_at, expires_at),

                           CONSTRAINT fk_qt_session
                               FOREIGN KEY (session_id) REFERENCES attendance_sessions(id)
                                   ON UPDATE CASCADE ON DELETE CASCADE,

                           CONSTRAINT fk_qt_rotated_from
                               FOREIGN KEY (rotated_from_token_id) REFERENCES qr_tokens(token_id)
                                   ON UPDATE CASCADE ON DELETE SET NULL,

                           CONSTRAINT fk_qt_issuer
                               FOREIGN KEY (issued_by_user_id) REFERENCES users(id)
                                   ON UPDATE CASCADE ON DELETE SET NULL,

                           CONSTRAINT chk_qt_exp_after_issued
                               CHECK (expires_at > issued_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE session_attendance
    MODIFY COLUMN qr_token_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL;

ALTER TABLE session_attendance
    ADD CONSTRAINT fk_sa_qr_token
        FOREIGN KEY (qr_token_id) REFERENCES qr_tokens(token_id)
            ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE attendance_events
    ADD COLUMN qr_token_id VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NULL,
    ADD KEY idx_ae_qr_token (qr_token_id),
    ADD CONSTRAINT fk_ae_qr_token
        FOREIGN KEY (qr_token_id) REFERENCES qr_tokens(token_id)
            ON UPDATE CASCADE ON DELETE SET NULL;
