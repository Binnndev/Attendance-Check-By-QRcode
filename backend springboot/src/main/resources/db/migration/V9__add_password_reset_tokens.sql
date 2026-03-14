CREATE TABLE password_reset_tokens (
                                       id             BINARY(16)  NOT NULL,
                                       user_id        BINARY(16)  NOT NULL,
                                       token_hash     VARBINARY(32) NOT NULL,

                                       requested_ip   VARCHAR(45) NULL,
                                       user_agent     VARCHAR(255) NULL,

                                       expires_at     DATETIME(3) NOT NULL,
                                       used_at        DATETIME(3) NULL,

                                       revoked_at     DATETIME(3) NULL,
                                       revoked_reason VARCHAR(50) NULL,

                                       created_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                                       updated_at     DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                   ON UPDATE CURRENT_TIMESTAMP(3),

                                       PRIMARY KEY (id),

                                       UNIQUE KEY uk_password_reset_tokens_token_hash (token_hash),

                                       KEY idx_prt_user_created (user_id, created_at),
                                       KEY idx_prt_user_active (user_id, revoked_at, used_at, expires_at),
                                       KEY idx_prt_expires_at (expires_at),

                                       CONSTRAINT fk_password_reset_tokens_user
                                           FOREIGN KEY (user_id) REFERENCES users(id)
                                               ON UPDATE CASCADE
                                               ON DELETE RESTRICT,

                                       CONSTRAINT chk_password_reset_tokens_expires_after_created
                                           CHECK (expires_at > created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;