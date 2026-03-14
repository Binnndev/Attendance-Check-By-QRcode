CREATE TABLE password_reset_attempts (
                                         id              BINARY(16) NOT NULL,
                                         email_hash      VARBINARY(32) NOT NULL,
                                         user_id         BINARY(16) NULL,
                                         requested_ip    VARCHAR(45) NULL,
                                         user_agent      VARCHAR(255) NULL,
                                         outcome         VARCHAR(32) NOT NULL,
                                         created_at      DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

                                         PRIMARY KEY (id),

                                         KEY idx_pra_email_created (email_hash, created_at),
                                         KEY idx_pra_ip_created (requested_ip, created_at),
                                         KEY idx_pra_user_created (user_id, created_at),
                                         KEY idx_pra_outcome_created (outcome, created_at),

                                         CONSTRAINT fk_pra_user
                                             FOREIGN KEY (user_id) REFERENCES users(id)
                                                 ON UPDATE CASCADE
                                                 ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;