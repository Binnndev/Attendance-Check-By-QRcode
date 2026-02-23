package com.attendance.backend.common.db;

import com.attendance.backend.common.exception.ApiException;
import org.springframework.stereotype.Component;

@Component
public class DbErrorTranslator {

    private static final String UK_AS_GROUP_SINGLE_OPEN = "uk_as_group_single_open";
    private static final String UK_QT_TOKEN_HASH = "uk_qt_token_hash";
    private static final String UK_QT_SESSION_TOKEN = "uk_qt_session_token";

    private static final String CHK_AS_CHECKIN_WINDOW = "chk_as_checkin_window";
    private static final String CHK_AS_LATE_LE_WINDOW = "chk_as_late_le_window";
    private static final String CHK_AS_WINDOW = "chk_as_window";
    private static final String CHK_AS_LATE = "chk_as_late";
    private static final String CHK_AS_ROTATE = "chk_as_rotate";
    private static final String CHK_QT_EXP_AFTER_ISSUED = "chk_qt_exp_after_issued";
    private static final String CHK_AR_REASON_LEN = "chk_ar_reason_len";

    private static final String FK_SA_SESSION_QR_TOKEN = "fk_sa_session_qr_token";
    private static final String FK_SA_QR_TOKEN = "fk_sa_qr_token";

    private static final String TRG_OWNER_REMOVE =
            "Cannot remove/reject OWNER directly. Transfer ownership first.";
    private static final String TRG_AR_TARGET_XOR =
            "Absence request must set exactly one of linked_session_id or requested_date.";
    private static final String TRG_AR_STATUS_FLOW =
            "Invalid status transition APPROVED -> PENDING";
    private static final String TRG_AE_SESSION_ACTOR_REQUIRED =
            "SESSION_* events require actor_user_id";
    private static final String TRG_AE_SESSION_USER_MUST_EQUAL_ACTOR =
            "For SESSION_* events, set user_id = actor_user_id";
    private static final String TRG_AE_IMMUTABLE =
            "attendance_events are immutable (updates not allowed)";

    public ApiException translate(Throwable ex) {
        String root = rootMessage(ex);

        if (root.contains(TRG_OWNER_REMOVE)) {
            return ApiException.conflict("OWNER_TRANSFER_REQUIRED", TRG_OWNER_REMOVE);
        }
        if (root.contains(TRG_AR_TARGET_XOR)) {
            return ApiException.unprocessable("ABSENCE_TARGET_XOR", TRG_AR_TARGET_XOR);
        }
        if (root.contains(TRG_AR_STATUS_FLOW)) {
            return ApiException.conflict("ABSENCE_STATUS_INVALID_TRANSITION", TRG_AR_STATUS_FLOW);
        }
        if (root.contains(TRG_AE_SESSION_ACTOR_REQUIRED)) {
            return ApiException.unprocessable("EVENT_ACTOR_REQUIRED", TRG_AE_SESSION_ACTOR_REQUIRED);
        }
        if (root.contains(TRG_AE_SESSION_USER_MUST_EQUAL_ACTOR)) {
            return ApiException.unprocessable("EVENT_USER_MUST_EQUAL_ACTOR", TRG_AE_SESSION_USER_MUST_EQUAL_ACTOR);
        }
        if (root.contains(TRG_AE_IMMUTABLE)) {
            return ApiException.conflict("EVENT_IMMUTABLE", TRG_AE_IMMUTABLE);
        }

        if (root.contains(FK_SA_SESSION_QR_TOKEN)) {
            return ApiException.conflict("QR_TOKEN_NOT_FOR_SESSION", "QR token does not belong to this session");
        }
        if (root.contains(FK_SA_QR_TOKEN)) {
            return ApiException.conflict("QR_TOKEN_INVALID", "QR token is invalid");
        }

        if (root.contains(UK_AS_GROUP_SINGLE_OPEN)) {
            return ApiException.conflict("SESSION_ALREADY_OPEN", "This group already has an OPEN session");
        }
        if (root.contains(UK_QT_TOKEN_HASH) || root.contains(UK_QT_SESSION_TOKEN)) {
            return ApiException.unprocessable("QR_TOKEN_COLLISION", "Cannot generate unique QR token, please retry");
        }

        if (root.contains(CHK_AS_CHECKIN_WINDOW)) {
            return ApiException.unprocessable("SESSION_TIME_INVALID", "checkin_close_at must be >= checkin_open_at");
        }
        if (root.contains(CHK_AS_LATE_LE_WINDOW)) {
            return ApiException.unprocessable("SESSION_TIME_INVALID", "late_after_minutes must be <= time_window_minutes");
        }
        if (root.contains(CHK_AS_WINDOW) || root.contains(CHK_AS_LATE) || root.contains(CHK_AS_ROTATE)) {
            return ApiException.unprocessable("SESSION_CONFIG_INVALID", "Session config is out of allowed range");
        }
        if (root.contains(CHK_QT_EXP_AFTER_ISSUED)) {
            return ApiException.unprocessable("TOKEN_EXP_INVALID", "expires_at must be after issued_at");
        }
        if (root.contains(CHK_AR_REASON_LEN)) {
            return ApiException.unprocessable("ABSENCE_REASON_INVALID", "reason length must be between 3 and 500");
        }

        return ApiException.conflict("DB_CONSTRAINT_VIOLATION", "Database constraint violated");
    }

    private static String rootMessage(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() == null ? "" : t.getMessage();
    }
}
