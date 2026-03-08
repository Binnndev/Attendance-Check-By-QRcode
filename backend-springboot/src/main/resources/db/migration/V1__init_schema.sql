CREATE TABLE users (
  id                 BINARY(16) NOT NULL,
  platform_role      ENUM('ADMIN','USER') NOT NULL DEFAULT 'USER',
  email              VARCHAR(190) NOT NULL,
  email_norm         VARCHAR(190) AS (LOWER(TRIM(email))) STORED,
  password_hash      VARCHAR(255) NOT NULL,
  full_name          VARCHAR(120) NOT NULL,
  avatar_url         VARCHAR(500) NULL,
  user_code          VARCHAR(40)  CHARACTER SET ascii COLLATE ascii_bin NULL,
  primary_device_id  VARCHAR(120) CHARACTER SET ascii COLLATE ascii_bin NULL,
  status             ENUM('ACTIVE','INACTIVE','BANNED') NOT NULL DEFAULT 'ACTIVE',
  created_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at         DATETIME(3) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email_norm (email_norm),
  UNIQUE KEY uk_users_user_code (user_code),
  KEY idx_users_role_status (platform_role, status),
  KEY idx_users_name (full_name),
  CONSTRAINT chk_users_email_len CHECK (CHAR_LENGTH(email) >= 5),
  CONSTRAINT chk_users_full_name_len CHECK (CHAR_LENGTH(full_name) >= 2)
) ENGINE=InnoDB;

CREATE TABLE class_groups (
  id                 BINARY(16) NOT NULL,
  owner_user_id      BINARY(16) NOT NULL,
  name               VARCHAR(150) NOT NULL,
  code               VARCHAR(20)  NOT NULL,
  join_code          VARCHAR(16)  NOT NULL,
  description        VARCHAR(1000) NULL,
  semester           VARCHAR(30) NULL,
  room               VARCHAR(80) NULL,
  approval_mode      ENUM('AUTO','MANUAL') NOT NULL DEFAULT 'AUTO',
  allow_auto_join_on_checkin TINYINT NOT NULL DEFAULT 0,
  status             ENUM('ACTIVE','ARCHIVED') NOT NULL DEFAULT 'ACTIVE',
  created_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  deleted_at         DATETIME(3) NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_groups_code (code),
  UNIQUE KEY uk_groups_join_code (join_code),
  KEY idx_groups_owner (owner_user_id),
  KEY idx_groups_status_created (status, created_at),
  CONSTRAINT fk_groups_owner
    FOREIGN KEY (owner_user_id) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_groups_name_len CHECK (CHAR_LENGTH(name) >= 3),
  CONSTRAINT chk_groups_join_code_len CHECK (CHAR_LENGTH(join_code) BETWEEN 6 AND 16)
) ENGINE=InnoDB;

CREATE TABLE group_members (
  group_id           BINARY(16) NOT NULL,
  user_id            BINARY(16) NOT NULL,
  role               ENUM('OWNER','CO_HOST','MEMBER') NOT NULL DEFAULT 'MEMBER',
  member_status      ENUM('PENDING','APPROVED','REJECTED','REMOVED') NOT NULL DEFAULT 'APPROVED',
  joined_at          DATETIME(3) NULL,
  invited_by         BINARY(16) NULL,
  created_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  removed_at         DATETIME(3) NULL,
  PRIMARY KEY (group_id, user_id),
  KEY idx_gm_user (user_id),
  KEY idx_gm_group_status_role (group_id, member_status, role),
  KEY idx_gm_group_joined (group_id, joined_at),
  CONSTRAINT fk_gm_group
    FOREIGN KEY (group_id) REFERENCES class_groups(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_gm_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_gm_invited_by
    FOREIGN KEY (invited_by) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE attendance_sessions (
  id                 BINARY(16) NOT NULL,
  group_id           BINARY(16) NOT NULL,
  created_by_user_id BINARY(16) NOT NULL,
  title              VARCHAR(150) NULL,
  session_date       DATE NOT NULL,
  status             ENUM('OPEN','CLOSED','CANCELLED') NOT NULL DEFAULT 'OPEN',
  is_open            TINYINT AS (CASE WHEN status = 'OPEN' THEN 1 ELSE NULL END) STORED,
  start_at           DATETIME(3) NOT NULL,
  end_at             DATETIME(3) NULL,
  time_window_minutes INT NOT NULL DEFAULT 15,
  late_after_minutes  INT NOT NULL DEFAULT 5,
  qr_rotate_seconds   INT NOT NULL DEFAULT 15,
  session_secret     VARCHAR(255) NOT NULL,
  allow_manual_override TINYINT NOT NULL DEFAULT 1,
  note               VARCHAR(500) NULL,
  created_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_as_group_date (group_id, session_date),
  KEY idx_as_group_created (group_id, created_at),
  UNIQUE KEY uk_as_group_single_open (group_id, is_open),
  CONSTRAINT fk_as_group
    FOREIGN KEY (group_id) REFERENCES class_groups(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_as_creator
    FOREIGN KEY (created_by_user_id) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_as_window CHECK (time_window_minutes BETWEEN 1 AND 300),
  CONSTRAINT chk_as_late CHECK (late_after_minutes BETWEEN 1 AND 120),
  CONSTRAINT chk_as_rotate CHECK (qr_rotate_seconds BETWEEN 5 AND 120),
  CONSTRAINT chk_as_late_le_window CHECK (late_after_minutes <= time_window_minutes)
) ENGINE=InnoDB;

CREATE TABLE session_attendance (
  session_id         BINARY(16) NOT NULL,
  user_id            BINARY(16) NOT NULL,
  attendance_status  ENUM('ABSENT','PRESENT','LATE','EXCUSED') NOT NULL DEFAULT 'ABSENT',
  check_in_at        DATETIME(3) NULL,
  check_in_method    ENUM('QR','MANUAL') NOT NULL DEFAULT 'QR',
  qr_token_id        VARCHAR(64) NULL,
  device_id          VARCHAR(120) NULL,
  ip_address         VARCHAR(45) NULL,
  user_agent         VARCHAR(255) NULL,
  geo_lat            DECIMAL(10,7) NULL,
  geo_lng            DECIMAL(10,7) NULL,
  distance_meter     INT NULL,
  suspicious_flag    TINYINT NOT NULL DEFAULT 0,
  suspicious_reason  VARCHAR(500) NULL,
  excused_by_request_id BINARY(16) NULL,
  created_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (session_id, user_id),
  KEY idx_sa_user_created (user_id, created_at),
  KEY idx_sa_session_status (session_id, attendance_status),
  KEY idx_sa_checkin (session_id, check_in_at),
  CONSTRAINT fk_sa_session
    FOREIGN KEY (session_id) REFERENCES attendance_sessions(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_sa_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT chk_sa_geo_lat CHECK (geo_lat IS NULL OR (geo_lat >= -90 AND geo_lat <= 90)),
  CONSTRAINT chk_sa_geo_lng CHECK (geo_lng IS NULL OR (geo_lng >= -180 AND geo_lng <= 180)),
  CONSTRAINT chk_sa_distance CHECK (distance_meter IS NULL OR distance_meter >= 0)
) ENGINE=InnoDB;

CREATE TABLE absence_requests (
  id                 BINARY(16) NOT NULL,
  group_id           BINARY(16) NOT NULL,
  requester_user_id  BINARY(16) NOT NULL,
  linked_session_id  BINARY(16) NULL,
  requested_date     DATE NULL,
  reason             VARCHAR(500) NOT NULL,
  evidence_url       VARCHAR(500) NULL,
  request_status     ENUM('PENDING','APPROVED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  reviewer_user_id   BINARY(16) NULL,
  reviewer_note      VARCHAR(500) NULL,
  reviewed_at        DATETIME(3) NULL,
  created_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  cancelled_at       DATETIME(3) NULL,
  PRIMARY KEY (id),
  KEY idx_ar_group_status_created (group_id, request_status, created_at),
  KEY idx_ar_requester_created (requester_user_id, created_at),
  KEY idx_ar_session (linked_session_id),
  CONSTRAINT fk_ar_group
    FOREIGN KEY (group_id) REFERENCES class_groups(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_ar_requester
    FOREIGN KEY (requester_user_id) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_ar_session
    FOREIGN KEY (linked_session_id) REFERENCES attendance_sessions(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_ar_reviewer
    FOREIGN KEY (reviewer_user_id) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT chk_ar_reason_len CHECK (CHAR_LENGTH(reason) BETWEEN 3 AND 500)
) ENGINE=InnoDB;

ALTER TABLE session_attendance
  ADD CONSTRAINT fk_sa_excused_request
  FOREIGN KEY (excused_by_request_id) REFERENCES absence_requests(id)
  ON UPDATE CASCADE ON DELETE SET NULL;

CREATE TABLE attendance_events (
  id                 BINARY(16) NOT NULL,
  session_id         BINARY(16) NOT NULL,
  user_id            BINARY(16) NOT NULL,
  actor_user_id      BINARY(16) NULL,
  event_type         ENUM(
                       'CHECKIN_QR',
                       'MARK_MANUAL_PRESENT',
                       'MARK_MANUAL_LATE',
                       'MARK_MANUAL_ABSENT',
                       'MARK_EXCUSED',
                       'REVERT_FROM_EXCUSED',
                       'SESSION_OPENED',
                       'SESSION_CLOSED'
                     ) NOT NULL,
  old_status         ENUM('ABSENT','PRESENT','LATE','EXCUSED') NULL,
  new_status         ENUM('ABSENT','PRESENT','LATE','EXCUSED') NULL,
  event_payload      JSON NULL,
  created_at         DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (id),
  KEY idx_ae_session_user_time (session_id, user_id, created_at),
  KEY idx_ae_actor_time (actor_user_id, created_at),
  KEY idx_ae_type_time (event_type, created_at),
  CONSTRAINT fk_ae_session
    FOREIGN KEY (session_id) REFERENCES attendance_sessions(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_ae_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_ae_actor
    FOREIGN KEY (actor_user_id) REFERENCES users(id)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;
