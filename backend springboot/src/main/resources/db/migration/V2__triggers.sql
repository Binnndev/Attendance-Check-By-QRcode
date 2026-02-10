-- V2__triggers.sql (MySQL 8.0 + Flyway)
-- Business constraints that are hard/impossible with CHECK in MySQL 8 are enforced here.

DELIMITER $$

-- 1) Prevent removing/rejecting OWNER directly
CREATE TRIGGER trg_gm_prevent_remove_owner
BEFORE UPDATE ON group_members
FOR EACH ROW
BEGIN
  IF OLD.role = 'OWNER' AND NEW.member_status IN ('REMOVED','REJECTED') THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Cannot remove/reject OWNER directly. Transfer ownership first.';
  END IF;
END$$

-- 2) Absence request must target exactly one of: linked_session_id OR requested_date
CREATE TRIGGER trg_ar_target_xor_ins
BEFORE INSERT ON absence_requests
FOR EACH ROW
BEGIN
  IF (NEW.linked_session_id IS NULL AND NEW.requested_date IS NULL)
     OR (NEW.linked_session_id IS NOT NULL AND NEW.requested_date IS NOT NULL) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Absence request must set exactly one of linked_session_id or requested_date.';
  END IF;
END$$

CREATE TRIGGER trg_ar_target_xor_upd
BEFORE UPDATE ON absence_requests
FOR EACH ROW
BEGIN
  IF (NEW.linked_session_id IS NULL AND NEW.requested_date IS NULL)
     OR (NEW.linked_session_id IS NOT NULL AND NEW.requested_date IS NOT NULL) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Absence request must set exactly one of linked_session_id or requested_date.';
  END IF;
END$$

-- 3) Absence status flow: APPROVED cannot go back to PENDING
CREATE TRIGGER trg_ar_status_flow
BEFORE UPDATE ON absence_requests
FOR EACH ROW
BEGIN
  IF OLD.request_status = 'APPROVED' AND NEW.request_status = 'PENDING' THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Invalid status transition APPROVED -> PENDING';
  END IF;
END$$

-- 4) Attendance events minimal validation:
-- For SESSION_OPENED / SESSION_CLOSED: require actor_user_id present and user_id == actor_user_id
CREATE TRIGGER trg_ae_validate_ins
BEFORE INSERT ON attendance_events
FOR EACH ROW
BEGIN
  IF NEW.event_type IN ('SESSION_OPENED','SESSION_CLOSED') THEN
    IF NEW.actor_user_id IS NULL THEN
      SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'SESSION_* events require actor_user_id';
    END IF;
    IF NEW.user_id <> NEW.actor_user_id THEN
      SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'For SESSION_* events, set user_id = actor_user_id';
    END IF;
  END IF;
END$$

-- Optional: prevent updates to audit log (immutable)
CREATE TRIGGER trg_ae_immutable_upd
BEFORE UPDATE ON attendance_events
FOR EACH ROW
BEGIN
  SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT = 'attendance_events are immutable (updates not allowed)';
END$$

DELIMITER ;
