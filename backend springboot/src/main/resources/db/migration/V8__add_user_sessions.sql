CREATE TABLE user_sessions (
                               id                 BINARY(16) NOT NULL,
                               user_id            BINARY(16) NOT NULL,

                               refresh_token_hash VARBINARY(32) NOT NULL,

                               device_id          VARCHAR(120) CHARACTER SET ascii COLLATE ascii_bin NULL,
                               ip_address         VARCHAR(45) NULL,
                               user_agent         VARCHAR(255) NULL,

                               issued_at          DATETIME(3) NOT NULL,
                               expires_at         DATETIME(3) NOT NULL,
                               last_used_at       DATETIME(3) NULL,

                               revoked_at         DATETIME(3) NULL,
                               revoked_reason     VARCHAR(50) NULL,

                               created_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                               updated_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                                   ON UPDATE CURRENT_TIMESTAMP(3),

                               PRIMARY KEY (id),

                               UNIQUE KEY uk_user_sessions_refresh_token_hash (refresh_token_hash),

                               KEY idx_user_sessions_user_created (user_id, created_at),
                               KEY idx_user_sessions_user_active (user_id, revoked_at, expires_at),
                               KEY idx_user_sessions_expires_at (expires_at),
                               KEY idx_user_sessions_device_id (device_id),

                               CONSTRAINT fk_user_sessions_user
                                   FOREIGN KEY (user_id) REFERENCES users(id)
                                       ON UPDATE CASCADE
                                       ON DELETE RESTRICT,

                               CONSTRAINT chk_user_sessions_expires_after_issued
                                   CHECK (expires_at > issued_at)
) ENGINE=InnoDB;