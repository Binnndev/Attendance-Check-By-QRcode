CREATE TABLE email_outbox (
                              id                  BINARY(16) NOT NULL,
                              aggregate_type      VARCHAR(50) NOT NULL,
                              aggregate_id        BINARY(16) NULL,

                              to_email            VARCHAR(190) NOT NULL,
                              subject             VARCHAR(200) NOT NULL,

                              payload_nonce       VARBINARY(12) NOT NULL,
                              payload_ciphertext  VARBINARY(4096) NOT NULL,

                              status              VARCHAR(20) NOT NULL,
                              retry_count         INT NOT NULL DEFAULT 0,
                              next_attempt_at     DATETIME(3) NOT NULL,
                              locked_at           DATETIME(3) NULL,
                              processed_at        DATETIME(3) NULL,

                              last_error_code     VARCHAR(50) NULL,
                              last_error_message  VARCHAR(500) NULL,

                              created_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
                              updated_at          DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

                              PRIMARY KEY (id),

                              KEY idx_eo_status_next (status, next_attempt_at),
                              KEY idx_eo_aggregate (aggregate_type, aggregate_id),
                              KEY idx_eo_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;